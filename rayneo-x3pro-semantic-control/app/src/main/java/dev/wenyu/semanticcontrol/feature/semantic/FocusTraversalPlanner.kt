package dev.wenyu.semanticcontrol.feature.semantic

data class TraversalCandidateState(
    val hasAccessibilityFocus: Boolean = false,
    val hasInputFocus: Boolean = false,
    val supportsAccessibilityFocus: Boolean = false,
    val supportsInputFocus: Boolean = false,
)

class FocusTraversalPlanner {

    fun isTraversalCandidate(candidate: TraversalCandidateState): Boolean {
        return candidate.hasAccessibilityFocus ||
            candidate.hasInputFocus ||
            candidate.supportsAccessibilityFocus ||
            candidate.supportsInputFocus
    }

    fun selectNextIndex(candidates: List<TraversalCandidateState>): Int? {
        if (candidates.isEmpty()) {
            return null
        }

        val currentIndex = currentIndex(candidates)
        if (currentIndex == null) {
            return 0
        }

        val nextIndex = currentIndex + 1
        return nextIndex.takeIf { it in candidates.indices }
    }

    fun selectPreviousIndex(candidates: List<TraversalCandidateState>): Int? {
        if (candidates.isEmpty()) {
            return null
        }

        val currentIndex = currentIndex(candidates)
        if (currentIndex == null) {
            return 0
        }

        val previousIndex = currentIndex - 1
        return previousIndex.takeIf { it in candidates.indices }
    }

    private fun currentIndex(candidates: List<TraversalCandidateState>): Int? {
        val currentIndex = candidates.indexOfFirst { candidate ->
            candidate.hasAccessibilityFocus || candidate.hasInputFocus
        }
        return currentIndex.takeIf { it >= 0 }
    }
}
