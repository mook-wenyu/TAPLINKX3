package dev.wenyu.semanticcontrol.core.contracts

sealed interface SemanticAction {
    data object FocusNext : SemanticAction
    data object FocusPrevious : SemanticAction
    data object ActivateFocused : SemanticAction
    data object Back : SemanticAction
    data object Home : SemanticAction
    data object ScrollForward : SemanticAction
    data object ScrollBackward : SemanticAction
}
