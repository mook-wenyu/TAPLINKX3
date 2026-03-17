package dev.wenyu.semanticcontrol.vendor.rayneo

import dev.wenyu.semanticcontrol.core.contracts.SemanticAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MercuryTouchSemanticBridgeTest {

    @Test
    fun `pinch maps to activate focused`() {
        val recorder = SemanticActionRecorder()
        val bridge = MercuryTouchSemanticBridge(recorder::record)

        val result = bridge.onPinch()

        assertTrue(result)
        assertEquals(listOf(SemanticAction.ActivateFocused), recorder.actions)
    }

    @Test
    fun `slide forward maps to focus next`() {
        val recorder = SemanticActionRecorder()
        val bridge = MercuryTouchSemanticBridge(recorder::record)

        val result = bridge.onSlideForward()

        assertTrue(result)
        assertEquals(listOf(SemanticAction.FocusNext), recorder.actions)
    }

    @Test
    fun `slide backward maps to focus previous`() {
        val recorder = SemanticActionRecorder()
        val bridge = MercuryTouchSemanticBridge(recorder::record)

        val result = bridge.onSlideBackward()

        assertTrue(result)
        assertEquals(listOf(SemanticAction.FocusPrevious), recorder.actions)
    }

    @Test
    fun `long pinch is intentionally ignored in current spike`() {
        val recorder = SemanticActionRecorder()
        val bridge = MercuryTouchSemanticBridge(recorder::record)

        val result = bridge.onLongPinch()

        assertFalse(result)
        assertTrue(recorder.actions.isEmpty())
    }

    @Test
    fun `click is intentionally ignored in current spike`() {
        val recorder = SemanticActionRecorder()
        val bridge = MercuryTouchSemanticBridge(recorder::record)

        val result = bridge.onClick()

        assertFalse(result)
        assertTrue(recorder.actions.isEmpty())
    }

    private class SemanticActionRecorder {
        val actions = mutableListOf<SemanticAction>()

        fun record(action: SemanticAction): Boolean {
            actions += action
            return true
        }
    }
}
