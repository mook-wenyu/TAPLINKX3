package dev.wenyu.semanticcontrol.app

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ffalcon.mercury.android.sdk.core.make3DEffectForSide
import com.ffalcon.mercury.android.sdk.focus.reqFocus
import com.ffalcon.mercury.android.sdk.touch.TempleAction
import com.ffalcon.mercury.android.sdk.ui.activity.BaseMirrorActivity
import com.ffalcon.mercury.android.sdk.ui.util.FixPosFocusTracker
import com.ffalcon.mercury.android.sdk.ui.util.FocusHolder
import com.ffalcon.mercury.android.sdk.ui.util.FocusInfo
import dev.wenyu.semanticcontrol.app.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class MainActivity : BaseMirrorActivity<ActivityMainBinding>() {

    private val stateResolver = HomepageSetupCardStateResolver()
    private val actionRouter = HomepageTempleActionRouter(
        performCurrentAction = { performCurrentAction() },
        onExit = { finish() },
    )

    private var focusTracker: FixPosFocusTracker? = null
    private lateinit var modeStore: AccessibilityModeStore
    private var currentCtaAction = HomepageCtaAction.OpenAccessibilitySettings
    private lateinit var currentCardState: HomepageSetupCardState
    private var isReadyForDebugCommands = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activeInstance = this
        modeStore = SharedPreferencesAccessibilityModeStore(
            getSharedPreferences(SharedPreferencesAccessibilityModeStore.PREFS_NAME, MODE_PRIVATE),
        )
        initHomepageFocus()
        initTempleActions()
        renderHomepage()
    }

    override fun onResume() {
        super.onResume()
        isReadyForDebugCommands = true
        renderHomepage()
    }

    override fun onPause() {
        isReadyForDebugCommands = false
        super.onPause()
    }

    private fun initHomepageFocus() {
        val focusHolder = FocusHolder(false)
        mBindingPair.setLeft {
            focusHolder.addFocusTarget(
                FocusInfo(
                    primaryButton,
                    eventHandler = { action ->
                        handleTempleAction(action)
                    },
                    focusChangeHandler = { hasFocus ->
                        mBindingPair.updateView {
                            renderButtonFocus(hasFocus, primaryButton, mBindingPair.checkIsLeft(this))
                        }
                    },
                ),
            )
            focusHolder.currentFocus(mBindingPair.left.primaryButton)
            mBindingPair.left.primaryButton.setOnClickListener {
                performCurrentAction()
            }
        }

        focusTracker = FixPosFocusTracker(focusHolder).apply {
            focusObj.reqFocus()
        }
    }

    private fun initTempleActions() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                templeActionViewModel.state.collect { action ->
                    when (action) {
                        is TempleAction.DoubleClick,
                        is TempleAction.Click,
                        -> handleTempleAction(action)

                        else -> focusTracker?.handleFocusTargetEvent(action)
                    }
                }
            }
        }
    }

    private fun handleTempleAction(action: TempleAction): Boolean {
        val mapped = when (action) {
            is TempleAction.Click -> HomepageTempleAction.Click
            is TempleAction.DoubleClick -> HomepageTempleAction.DoubleClick
            is TempleAction.LongClick -> HomepageTempleAction.LongClick
            is TempleAction.SlideForward -> HomepageTempleAction.SlideForward
            is TempleAction.SlideBackward -> HomepageTempleAction.SlideBackward
            else -> return false
        }
        return actionRouter.handle(mapped)
    }

    fun handleDebugCommand(command: HomepageDebugCommand): HomepageDebugResult {
        val router = HomepageDebugCommandRouter(
            handleTempleAction = ::handleHomepageTempleAction,
            openCameraProbe = ::openCameraProbe,
            dumpState = ::buildHomepageSnapshot,
        )
        var result = HomepageDebugResult(success = false, message = "ui-timeout")
        val latch = CountDownLatch(1)
        runOnUiThread {
            result = router.handle(command)
            Log.i(TAG, "Homepage debug command ${command.wireValue} -> ${result.message}")
            latch.countDown()
        }
        val completed = latch.await(2, TimeUnit.SECONDS)
        return if (completed) result else HomepageDebugResult(false, "ui-timeout")
    }

    private fun handleHomepageTempleAction(action: HomepageTempleAction): Boolean {
        return actionRouter.handle(action)
    }

    private fun buildHomepageSnapshot(): String {
        return buildString {
            append("mode=")
            append(currentCardState.resolvedMode.name)
            append(";status=")
            append(currentCardState.status.name)
            append(";cta=")
            append(currentCtaAction.name)
        }
    }

    private fun openCameraProbe(): Boolean {
        if (CameraFeasibilityActivity.activeInstance != null) {
            return true
        }
        startActivity(Intent(this, CameraFeasibilityActivity::class.java))
        return true
    }

    internal fun canHandleDebugCommands(): Boolean = isReadyForDebugCommands

    private fun performCurrentAction(): Boolean {
        currentCardState.preferredModeOnPrimaryAction?.let(modeStore::write)
        return when (currentCtaAction) {
            HomepageCtaAction.OpenAccessibilitySettings -> {
                Log.i(TAG, "Opening accessibility settings from homepage")
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                true
            }
        }
    }

    private fun renderHomepage() {
        val preferredMode = modeStore.read()
        val state = stateResolver.resolve(
            preferredMode = preferredMode,
            isAccessibilityServiceEnabled = isAccessibilityServiceEnabled(),
            isAccessibilityServiceConnected = isAccessibilityServiceConnected(),
        )
        if (state.resolvedMode != preferredMode) {
            modeStore.write(state.resolvedMode)
        }

        mBindingPair.updateView {
            titleText.setText(R.string.homepage_title)
            statusText.setText(state.statusLineResId)
            helperText.setText(state.helperLineResId)
            primaryButton.setText(state.ctaLabelResId)
        }
        Log.i(
            TAG,
            "Homepage state mode=${state.resolvedMode} status=${state.status} cta=${state.ctaAction}",
        )
        currentCardState = state
        currentCtaAction = state.ctaAction
    }

    private fun renderButtonFocus(hasFocus: Boolean, view: View, isLeft: Boolean) {
        view.alpha = if (hasFocus) 1f else 0.86f
        make3DEffectForSide(view, isLeft, hasFocus)
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ) ?: return false

        val targetService = ComponentName(packageName, ACCESSIBILITY_SERVICE_CLASS_NAME).flattenToString()

        return enabledServices.split(':').any { it == targetService }
    }

    private fun isAccessibilityServiceConnected(): Boolean {
        return AccessibilityServiceConnectionState.isConnected
    }

    companion object {
        const val TAG = "HomepageMainActivity"
        const val ACCESSIBILITY_SERVICE_CLASS_NAME =
            "dev.wenyu.semanticcontrol.feature.semantic.SemanticAccessibilityService"

        @Volatile
        var activeInstance: MainActivity? = null
            private set
    }

    override fun onDestroy() {
        isReadyForDebugCommands = false
        if (activeInstance === this) {
            activeInstance = null
        }
        super.onDestroy()
    }
}
