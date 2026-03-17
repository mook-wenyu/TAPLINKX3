package dev.wenyu.semanticcontrol.feature.semantic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FocusTraversalPlannerTest {

    private val planner = FocusTraversalPlanner()

    @Test
    fun `selectNextIndex returns first item when nothing is focused`() {
        val targetIndex = planner.selectNextIndex(
            listOf(
                TraversalCandidateState(supportsAccessibilityFocus = true),
                TraversalCandidateState(supportsAccessibilityFocus = true),
                TraversalCandidateState(supportsAccessibilityFocus = true),
            ),
        )

        assertEquals(0, targetIndex)
    }

    @Test
    fun `selectNextIndex advances to the next candidate`() {
        val targetIndex = planner.selectNextIndex(
            listOf(
                TraversalCandidateState(hasAccessibilityFocus = true),
                TraversalCandidateState(supportsAccessibilityFocus = true),
                TraversalCandidateState(supportsAccessibilityFocus = true),
            ),
        )

        assertEquals(1, targetIndex)
    }

    @Test
    fun `selectNextIndex returns null at the end of the list`() {
        val targetIndex = planner.selectNextIndex(
            listOf(
                TraversalCandidateState(supportsAccessibilityFocus = true),
                TraversalCandidateState(supportsAccessibilityFocus = true),
                TraversalCandidateState(hasAccessibilityFocus = true),
            ),
        )

        assertNull(targetIndex)
    }

    @Test
    fun `selectPreviousIndex returns first item when nothing is focused`() {
        val targetIndex = planner.selectPreviousIndex(
            listOf(
                TraversalCandidateState(supportsInputFocus = true),
                TraversalCandidateState(supportsInputFocus = true),
                TraversalCandidateState(supportsInputFocus = true),
            ),
        )

        assertEquals(0, targetIndex)
    }

    @Test
    fun `selectPreviousIndex moves backward when a previous candidate exists`() {
        val targetIndex = planner.selectPreviousIndex(
            listOf(
                TraversalCandidateState(supportsInputFocus = true),
                TraversalCandidateState(supportsInputFocus = true),
                TraversalCandidateState(hasInputFocus = true),
            ),
        )

        assertEquals(1, targetIndex)
    }

    @Test
    fun `selectPreviousIndex returns null at the beginning of the list`() {
        val targetIndex = planner.selectPreviousIndex(
            listOf(
                TraversalCandidateState(hasInputFocus = true),
                TraversalCandidateState(supportsInputFocus = true),
                TraversalCandidateState(supportsInputFocus = true),
            ),
        )

        assertNull(targetIndex)
    }

    @Test
    fun `selection returns null when there are no candidates`() {
        val nextTarget = planner.selectNextIndex(emptyList())
        val previousTarget = planner.selectPreviousIndex(emptyList())

        assertNull(nextTarget)
        assertNull(previousTarget)
    }

    @Test
    fun `focused node remains a traversal candidate even without explicit focus action`() {
        val focusedCandidate = TraversalCandidateState(hasAccessibilityFocus = true)

        val result = planner.isTraversalCandidate(focusedCandidate)

        assertEquals(true, result)
    }

    @Test
    fun `click-only candidate is excluded from traversal`() {
        val clickOnlyCandidate = TraversalCandidateState()

        val result = planner.isTraversalCandidate(clickOnlyCandidate)

        assertEquals(false, result)
    }
}
