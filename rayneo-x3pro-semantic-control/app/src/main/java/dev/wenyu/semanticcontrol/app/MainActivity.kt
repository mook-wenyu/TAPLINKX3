package dev.wenyu.semanticcontrol.app

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
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

class MainActivity : BaseMirrorActivity<ActivityMainBinding>() {

    private val stateResolver = HomepageSetupCardStateResolver()
    private val actionRouter = HomepageTempleActionRouter(
        performCurrentAction = { performCurrentAction() },
        onExit = { finish() },
    )

    private var focusTracker: FixPosFocusTracker? = null
    private var currentCtaAction = HomepageCtaAction.OpenAccessibilitySettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initHomepageFocus()
        initTempleActions()
        renderHomepage()
    }

    override fun onResume() {
        super.onResume()
        renderHomepage()
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

    private fun performCurrentAction(): Boolean {
        return when (currentCtaAction) {
            HomepageCtaAction.OpenAccessibilitySettings -> {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                true
            }
        }
    }

    private fun renderHomepage() {
        val state = stateResolver.resolve(
            isAccessibilityServiceEnabled = isAccessibilityServiceEnabled(),
            isAccessibilityServiceConnected = isAccessibilityServiceConnected(),
        )

        mBindingPair.updateView {
            titleText.setText(R.string.homepage_title)
            statusText.setText(state.statusLineResId)
            helperText.setText(state.helperLineResId)
            primaryButton.setText(state.ctaLabelResId)
        }
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
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ) ?: return false

        val targetService = ComponentName(packageName, ACCESSIBILITY_SERVICE_CLASS_NAME).flattenToString()
        return enabledServices.split(':').any { it == targetService }
    }

    private companion object {
        const val ACCESSIBILITY_SERVICE_CLASS_NAME =
            "dev.wenyu.semanticcontrol.feature.semantic.SemanticAccessibilityService"
    }
}
