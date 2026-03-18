package dev.wenyu.semanticcontrol.app

enum class HomepageTempleAction {
    Click,
    DoubleClick,
    LongClick,
    SlideForward,
    SlideBackward,
}

class HomepageTempleActionRouter(
    private val performCurrentAction: () -> Boolean,
    private val onExit: () -> Unit,
) {
    fun handle(action: HomepageTempleAction): Boolean {
        return when (action) {
            HomepageTempleAction.Click -> performCurrentAction()
            HomepageTempleAction.DoubleClick -> {
                onExit()
                true
            }

            HomepageTempleAction.LongClick,
            HomepageTempleAction.SlideForward,
            HomepageTempleAction.SlideBackward,
            -> false
        }
    }
}
