package dev.wenyu.semanticcontrol.feature.semantic

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction
import android.view.accessibility.AccessibilityNodeInfo
import dev.wenyu.semanticcontrol.core.contracts.SemanticAction
import java.util.ArrayDeque

class SemanticNavigator(
    private val service: AccessibilityService,
    private val focusTraversalPlanner: FocusTraversalPlanner = FocusTraversalPlanner(),
) {

    private data class TraversalCandidate(
        val node: AccessibilityNodeInfo,
        val state: TraversalCandidateState,
    )

    fun perform(action: SemanticAction): Boolean = when (action) {
        SemanticAction.Back -> service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
        SemanticAction.Home -> service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
        SemanticAction.ActivateFocused -> activateFocusedNode()
        SemanticAction.FocusNext -> moveFocusForward()
        SemanticAction.FocusPrevious -> moveFocusBackward()
        SemanticAction.ScrollForward,
        SemanticAction.ScrollBackward,
        -> false
    }

    private fun moveFocusForward(): Boolean {
        return moveFocus { candidates ->
            focusTraversalPlanner.selectNextIndex(candidates)
        }
    }

    private fun moveFocusBackward(): Boolean {
        return moveFocus { candidates ->
            focusTraversalPlanner.selectPreviousIndex(candidates)
        }
    }

    private fun moveFocus(
        selectTargetIndex: (candidates: List<TraversalCandidateState>) -> Int?,
    ): Boolean {
        val root = service.rootInActiveWindow ?: return false
        val candidates = collectTraversalCandidates(root)
        if (candidates.isEmpty()) {
            return false
        }

        val targetIndex = selectTargetIndex(candidates.map { candidate -> candidate.state }) ?: return false
        return requestFocus(candidates[targetIndex])
    }

    private fun activateFocusedNode(): Boolean {
        val root = service.rootInActiveWindow ?: return false
        val node = root.findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY)
            ?: root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
            ?: return false

        val clickableTarget = findClickableTarget(node) ?: return false
        return clickableTarget.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    private fun collectTraversalCandidates(root: AccessibilityNodeInfo): List<TraversalCandidate> {
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        val candidates = mutableListOf<TraversalCandidate>()

        queue.add(root)
        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            val state = node.toTraversalCandidateState()
            if (node.isVisibleToUser && node.isEnabled && focusTraversalPlanner.isTraversalCandidate(state)) {
                candidates.add(TraversalCandidate(node = node, state = state))
            }

            for (index in 0 until node.childCount) {
                val child = node.getChild(index) ?: continue
                queue.add(child)
            }
        }

        return candidates
    }

    private fun requestFocus(candidate: TraversalCandidate): Boolean {
        return when {
            candidate.state.supportsAccessibilityFocus -> {
                candidate.node.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS)
            }

            candidate.state.supportsInputFocusAction -> {
                candidate.node.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
            }

            else -> false
        }
    }

    private fun findClickableTarget(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        var currentNode: AccessibilityNodeInfo? = node
        while (currentNode != null) {
            if (supportsClick(currentNode)) {
                return currentNode
            }
            currentNode = currentNode.parent
        }

        return null
    }

    private fun supportsAccessibilityFocus(node: AccessibilityNodeInfo): Boolean {
        return node.actionList.any { action -> action.id == AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS }
    }

    private fun supportsInputFocusAction(node: AccessibilityNodeInfo): Boolean {
        return node.actionList.any { action -> action.id == AccessibilityNodeInfo.ACTION_FOCUS }
    }

    private fun AccessibilityNodeInfo.toTraversalCandidateState(): TraversalCandidateState {
        return TraversalCandidateState(
            hasAccessibilityFocus = isAccessibilityFocused,
            hasInputFocus = isFocused,
            supportsAccessibilityFocus = supportsAccessibilityFocus(this),
            supportsInputFocusAction = supportsInputFocusAction(this),
            isFocusable = isFocusable,
        )
    }

    private fun supportsClick(node: AccessibilityNodeInfo): Boolean {
        return node.isClickable || node.actionList.any { action: AccessibilityAction ->
            action.id == AccessibilityNodeInfo.ACTION_CLICK
        }
    }
}
