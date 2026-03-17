package dev.wenyu.semanticcontrol.feature.semantic

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.accessibility.AccessibilityEvent
import dev.wenyu.semanticcontrol.core.contracts.GestureSignal
import dev.wenyu.semanticcontrol.core.contracts.SemanticAction
import dev.wenyu.semanticcontrol.feature.gesture.PinchConfirmController
import dev.wenyu.semanticcontrol.feature.overlay.FocusHudOverlayController
import dev.wenyu.semanticcontrol.feature.overlay.FocusHudState
import dev.wenyu.semanticcontrol.vendor.rayneo.RayNeoVendorAdapter
import dev.wenyu.semanticcontrol.vendor.rayneo.VendorInputAdapter

class SemanticAccessibilityService : AccessibilityService() {

    private lateinit var navigator: SemanticNavigator
    private lateinit var overlayController: FocusHudOverlayController
    private lateinit var pinchConfirmController: PinchConfirmController
    private lateinit var vendorInputAdapter: VendorInputAdapter

    override fun onServiceConnected() {
        super.onServiceConnected()
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

    override fun onDestroy() {
        overlayController.detach()
        super.onDestroy()
    }

    private companion object {
        const val TAG = "SemanticA11yService"
    }
}
