package dev.wenyu.semanticcontrol.feature.semantic

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityEvent
import dev.wenyu.semanticcontrol.app.AccessibilityServiceConnectionState
import dev.wenyu.semanticcontrol.core.contracts.GestureSignal
import dev.wenyu.semanticcontrol.core.contracts.SemanticAction
import dev.wenyu.semanticcontrol.feature.gesture.PinchConfirmController
import dev.wenyu.semanticcontrol.feature.overlay.FocusHudOverlayController
import dev.wenyu.semanticcontrol.feature.overlay.FocusHudState
import dev.wenyu.semanticcontrol.feature.semantic.debug.SemanticDebugCommand
import dev.wenyu.semanticcontrol.feature.semantic.debug.SemanticDebugCommandRouter
import dev.wenyu.semanticcontrol.feature.semantic.debug.DebugNodeSummary
import dev.wenyu.semanticcontrol.feature.semantic.debug.SemanticDebugResult
import dev.wenyu.semanticcontrol.feature.semantic.debug.SemanticDebugSnapshot
import dev.wenyu.semanticcontrol.vendor.rayneo.RayNeoVendorAdapter
import dev.wenyu.semanticcontrol.vendor.rayneo.VendorInputAdapter

class SemanticAccessibilityService : AccessibilityService() {

    private lateinit var navigator: SemanticNavigator
    private lateinit var overlayController: FocusHudOverlayController
    private lateinit var pinchConfirmController: PinchConfirmController
    private lateinit var vendorInputAdapter: VendorInputAdapter

    override fun onServiceConnected() {
        super.onServiceConnected()
        activeInstance = this
        AccessibilityServiceConnectionState.isConnected = true
        navigator = SemanticNavigator(this)
        overlayController = FocusHudOverlayController(this)
        pinchConfirmController = PinchConfirmController(
            onConfirm = {
                navigator.perform(SemanticAction.ActivateFocused)
            },
        )
        vendorInputAdapter = RayNeoVendorAdapter().createInputAdapter(navigator::perform)
        overlayController.attach(FocusHudState())
        Log.i(TAG, "Semantic accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        Log.d(
            TAG,
            "event=${event.eventType} package=${event.packageName} class=${event.className}",
        )
    }

    override fun onInterrupt() {
        Log.i(TAG, "Semantic accessibility service interrupted")
    }

    internal fun handleGestureSignal(signal: GestureSignal): Boolean {
        return pinchConfirmController.onSignal(signal)
    }

    internal fun handleVendorMotionEvent(event: MotionEvent) {
        vendorInputAdapter.onMotionEvent(event)
    }

    internal fun handleVendorKeyEvent(event: KeyEvent) {
        vendorInputAdapter.onKeyEvent(event)
    }

    internal fun handleDebugCommand(command: SemanticDebugCommand): SemanticDebugResult {
        val router = SemanticDebugCommandRouter(
            onSemanticAction = navigator::perform,
            dumpSnapshot = ::buildDebugSnapshot,
        )
        return router.handle(command)
    }

    private fun buildDebugSnapshot(): String {
        val root = rootInActiveWindow ?: return "root=null"
        val accessibilityFocus = root.findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY)
        val inputFocus = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        val snapshot = SemanticDebugSnapshot(
            rootPackage = root.packageName?.toString(),
            rootClass = root.className?.toString(),
            accessibilityFocus = accessibilityFocus?.toSummary(),
            inputFocus = inputFocus?.toSummary(),
            actionableCandidates = collectActionableCandidates(root),
        )
        return snapshot.formatForLog(candidateLimit = 5)
    }

    private fun collectActionableCandidates(root: AccessibilityNodeInfo): List<DebugNodeSummary> {
        val results = mutableListOf<DebugNodeSummary>()
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)

        while (queue.isNotEmpty() && results.size < 8) {
            val node = queue.removeFirst()
            if (node.isVisibleToUser && (node.isClickable || node.isFocusable || supportsActionFocus(node))) {
                results += node.toSummary()
            }
            for (index in 0 until node.childCount) {
                node.getChild(index)?.let(queue::addLast)
            }
        }

        return results
    }

    private fun AccessibilityNodeInfo.toSummary(): DebugNodeSummary {
        return DebugNodeSummary(
            className = className?.toString(),
            label = text?.toString() ?: contentDescription?.toString(),
            viewId = viewIdResourceName,
        )
    }

    private fun supportsActionFocus(node: AccessibilityNodeInfo): Boolean {
        return node.actionList.any { action -> action.id == AccessibilityNodeInfo.ACTION_FOCUS }
    }

    override fun onDestroy() {
        if (activeInstance === this) {
            activeInstance = null
        }
        AccessibilityServiceConnectionState.isConnected = false
        overlayController.detach()
        super.onDestroy()
    }

    companion object {
        const val TAG = "SemanticA11yService"

        @Volatile
        var activeInstance: SemanticAccessibilityService? = null
            private set
    }
}
