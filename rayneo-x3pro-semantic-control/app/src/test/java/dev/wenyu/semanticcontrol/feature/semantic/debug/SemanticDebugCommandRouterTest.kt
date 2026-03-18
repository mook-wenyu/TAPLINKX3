package dev.wenyu.semanticcontrol.feature.semantic.debug

import dev.wenyu.semanticcontrol.core.contracts.SemanticAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SemanticDebugCommandRouterTest {

    @Test
    fun `wire values map to known commands`() {
        assertEquals(SemanticDebugCommand.DumpRoot, SemanticDebugCommand.fromWireValue("dump-root"))
        assertEquals(SemanticDebugCommand.FocusNext, SemanticDebugCommand.fromWireValue("focus-next"))
        assertEquals(SemanticDebugCommand.FocusPrevious, SemanticDebugCommand.fromWireValue("focus-previous"))
        assertEquals(SemanticDebugCommand.ActivateFocused, SemanticDebugCommand.fromWireValue("activate-focused"))
    }

    @Test
    fun `unknown wire value returns null`() {
        assertEquals(null, SemanticDebugCommand.fromWireValue("nope"))
        assertEquals(null, SemanticDebugCommand.fromWireValue(null))
    }

    @Test
    fun `dump command returns snapshot without dispatching semantic action`() {
        val actions = mutableListOf<SemanticAction>()
        val router = SemanticDebugCommandRouter(
            onSemanticAction = {
                actions += it
                true
            },
            dumpSnapshot = { "snapshot:settings" },
        )

        val result = router.handle(SemanticDebugCommand.DumpRoot)

        assertTrue(result.success)
        assertEquals("snapshot:settings", result.message)
        assertTrue(actions.isEmpty())
    }

    @Test
    fun `focus next delegates to semantic action`() {
        val actions = mutableListOf<SemanticAction>()
        val router = SemanticDebugCommandRouter(
            onSemanticAction = {
                actions += it
                true
            },
            dumpSnapshot = { "unused" },
        )

        val result = router.handle(SemanticDebugCommand.FocusNext)

        assertTrue(result.success)
        assertEquals("focus-next:ok", result.message)
        assertEquals(listOf(SemanticAction.FocusNext), actions)
    }

    @Test
    fun `failed semantic action is surfaced in result`() {
        val router = SemanticDebugCommandRouter(
            onSemanticAction = { false },
            dumpSnapshot = { "unused" },
        )

        val result = router.handle(SemanticDebugCommand.ActivateFocused)

        assertFalse(result.success)
        assertEquals("activate-focused:failed", result.message)
    }
}
