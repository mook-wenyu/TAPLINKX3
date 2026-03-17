package dev.wenyu.semanticcontrol.feature.gesture

import dev.wenyu.semanticcontrol.core.contracts.GestureSignal

class PinchConfirmationStateMachine {

    private var state: PinchState = PinchState.Idle

    fun transition(signal: GestureSignal): PinchState {
        state = when (state) {
            PinchState.Idle -> when (signal) {
                GestureSignal.PinchStarted -> PinchState.Armed
                GestureSignal.PinchConfirmed,
                GestureSignal.PinchCancelled,
                -> PinchState.Idle
            }

            PinchState.Armed -> when (signal) {
                GestureSignal.PinchConfirmed -> PinchState.Confirmed
                GestureSignal.PinchCancelled -> PinchState.Idle
                GestureSignal.PinchStarted -> PinchState.Armed
            }

            PinchState.Confirmed -> when (signal) {
                GestureSignal.PinchCancelled,
                GestureSignal.PinchConfirmed,
                GestureSignal.PinchStarted,
                -> PinchState.Cooldown
            }

            PinchState.Cooldown -> PinchState.Idle
        }

        return state
    }
}

enum class PinchState {
    Idle,
    Armed,
    Confirmed,
    Cooldown,
}
