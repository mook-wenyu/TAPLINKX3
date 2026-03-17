package dev.wenyu.semanticcontrol.feature.semantic

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityNodeInfo
import dev.wenyu.semanticcontrol.core.contracts.SemanticAction

class SemanticNavigator(
    private val service: AccessibilityService,
) {

    fun perform(action: SemanticAction): Boolean = when (action) {
        SemanticAction.Back -> service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
        SemanticAction.Home -> service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
        SemanticAction.ActivateFocused -> activateFocusedNode()
        SemanticAction.FocusNext,
        SemanticAction.FocusPrevious,
        SemanticAction.ScrollForward,
        SemanticAction.ScrollBackward,
        -> false
    }

    private fun activateFocusedNode(): Boolean {
        val root = service.rootInActiveWindow ?: return false
        val node = root.findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY)
            ?: root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
            ?: return false

        return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }
}
