package dev.wenyu.semanticcontrol.vendor.rayneo

import dev.wenyu.semanticcontrol.core.contracts.SemanticAction

class MercuryTouchSemanticBridge(
    private val onSemanticAction: (SemanticAction) -> Boolean,
) {

    fun onPinch(): Boolean {
        return onSemanticAction(SemanticAction.ActivateFocused)
    }

    fun onSlideForward(): Boolean {
        return onSemanticAction(SemanticAction.FocusNext)
    }

    fun onSlideBackward(): Boolean {
        return onSemanticAction(SemanticAction.FocusPrevious)
    }

    fun onLongPinch(): Boolean = false

    fun onClick(): Boolean = false
}
