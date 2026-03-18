package dev.wenyu.semanticcontrol.feature.semantic

data class TraversalCandidateState(
    val hasAccessibilityFocus: Boolean = false,
    val hasInputFocus: Boolean = false,
    val supportsAccessibilityFocus: Boolean = false,
    val supportsInputFocusAction: Boolean = false,
    val isFocusable: Boolean = false,
)

class FocusTraversalPlanner {

    fun isTraversalCandidate(candidate: TraversalCandidateState): Boolean {
        return candidate.isCurrentlyFocused() || candidate.canReceiveProgrammaticFocus()
    }

    fun selectNextIndex(candidates: List<TraversalCandidateState>): Int? {
        if (candidates.isEmpty()) {
            return null
        }

        val currentIndex = currentIndex(candidates)
        if (currentIndex == null) {
            return candidates.indexOfFirst { candidate -> candidate.canReceiveProgrammaticFocus() }
                .takeIf { it >= 0 }
        }

        for (index in currentIndex + 1 until candidates.size) {
            if (candidates[index].canReceiveProgrammaticFocus()) {
                return index
            }
        }

        return null
    }

    fun selectPreviousIndex(candidates: List<TraversalCandidateState>): Int? {
        if (candidates.isEmpty()) {
            return null
        }

        val currentIndex = currentIndex(candidates)
        if (currentIndex == null) {
            return candidates.indexOfFirst { candidate -> candidate.canReceiveProgrammaticFocus() }
                .takeIf { it >= 0 }
        }

        for (index in currentIndex - 1 downTo 0) {
            if (candidates[index].canReceiveProgrammaticFocus()) {
                return index
            }
        }

        return null
    }

    private fun currentIndex(candidates: List<TraversalCandidateState>): Int? {
        val currentIndex = candidates.indexOfFirst { candidate ->
            candidate.isCurrentlyFocused()
        }
        return currentIndex.takeIf { it >= 0 }
    }

    private fun TraversalCandidateState.isCurrentlyFocused(): Boolean {
        return hasAccessibilityFocus || hasInputFocus
    }

    private fun TraversalCandidateState.canReceiveProgrammaticFocus(): Boolean {
        return supportsAccessibilityFocus || supportsInputFocusAction
    }
}
