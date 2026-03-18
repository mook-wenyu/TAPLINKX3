package dev.wenyu.semanticcontrol.feature.semantic.debug

import dev.wenyu.semanticcontrol.core.contracts.SemanticAction

enum class SemanticDebugCommand(val wireValue: String) {
    DumpRoot("dump-root"),
    FocusNext("focus-next"),
    FocusPrevious("focus-previous"),
    ActivateFocused("activate-focused");

    companion object {
        fun fromWireValue(value: String?): SemanticDebugCommand? {
            return entries.firstOrNull { command -> command.wireValue == value }
        }
    }
}

data class SemanticDebugResult(
    val success: Boolean,
    val message: String,
)

class SemanticDebugCommandRouter(
    private val onSemanticAction: (SemanticAction) -> Boolean,
    private val dumpSnapshot: () -> String,
) {
    fun handle(command: SemanticDebugCommand): SemanticDebugResult {
        return when (command) {
            SemanticDebugCommand.DumpRoot -> {
                SemanticDebugResult(success = true, message = dumpSnapshot())
            }

            SemanticDebugCommand.FocusNext -> commandResult(command, SemanticAction.FocusNext)
            SemanticDebugCommand.FocusPrevious -> commandResult(command, SemanticAction.FocusPrevious)
            SemanticDebugCommand.ActivateFocused -> commandResult(command, SemanticAction.ActivateFocused)
        }
    }

    private fun commandResult(
        command: SemanticDebugCommand,
        action: SemanticAction,
    ): SemanticDebugResult {
        val success = onSemanticAction(action)
        return SemanticDebugResult(
            success = success,
            message = if (success) {
                "${command.wireValue}:ok"
            } else {
                "${command.wireValue}:failed"
            },
        )
    }
}
