package dev.wenyu.semanticcontrol.feature.gesture

import dev.wenyu.semanticcontrol.core.contracts.GestureSignal

class PinchConfirmController(
    private val gestureEngine: GestureEngine = GestureEngine(),
    private val onConfirm: () -> Boolean,
) {

    fun onSignal(signal: GestureSignal): Boolean {
        val pinchState = gestureEngine.onSignal(signal)
        if (pinchState != PinchState.Confirmed) {
            return false
        }

        return onConfirm()
    }
}
