package dev.wenyu.semanticcontrol.feature.overlay

data class FocusHudState(
    val modeLabel: String = "Navigate",
    val candidateLabel: String = "No target",
    val actionHint: String = "Move focus to begin",
    val armed: Boolean = false,
)
