package dev.wenyu.semanticcontrol.vendor.rayneo

sealed interface BootstrapResult {
    data object Initialized : BootstrapResult
    data object Skipped : BootstrapResult
    data class Failed(val reason: String) : BootstrapResult
}
