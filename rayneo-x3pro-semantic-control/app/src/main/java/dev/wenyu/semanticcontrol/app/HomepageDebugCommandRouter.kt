package dev.wenyu.semanticcontrol.app

enum class HomepageDebugCommand(val wireValue: String) {
    Click("click"),
    DoubleClick("double-click");

    companion object {
        fun fromWireValue(value: String?): HomepageDebugCommand? {
            return entries.firstOrNull { it.wireValue == value }
        }
    }
}

data class HomepageDebugResult(
    val success: Boolean,
    val message: String,
)

class HomepageDebugCommandRouter(
    private val handleTempleAction: (HomepageTempleAction) -> Boolean,
) {
    fun handle(command: HomepageDebugCommand): HomepageDebugResult {
        val action = when (command) {
            HomepageDebugCommand.Click -> HomepageTempleAction.Click
            HomepageDebugCommand.DoubleClick -> HomepageTempleAction.DoubleClick
        }

        val success = handleTempleAction(action)
        return HomepageDebugResult(
            success = success,
            message = if (success) {
                "${command.wireValue}:ok"
            } else {
                "${command.wireValue}:failed"
            },
        )
    }
}
