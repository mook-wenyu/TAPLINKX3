package dev.wenyu.semanticcontrol.feature.gesture

import dev.wenyu.semanticcontrol.core.contracts.GestureSignal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GestureEngine(
    private val stateMachine: PinchConfirmationStateMachine = PinchConfirmationStateMachine(),
) {
    private val _pinchState = MutableStateFlow(PinchState.Idle)
    val pinchState: StateFlow<PinchState> = _pinchState

    fun onSignal(signal: GestureSignal): PinchState {
        val nextState = stateMachine.transition(signal)
        _pinchState.value = nextState
        return nextState
    }
}
