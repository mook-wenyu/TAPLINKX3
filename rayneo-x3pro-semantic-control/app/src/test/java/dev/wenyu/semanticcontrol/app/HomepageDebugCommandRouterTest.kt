package dev.wenyu.semanticcontrol.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomepageDebugCommandRouterTest {

    @Test
    fun `wire values map to known homepage debug commands`() {
        assertEquals(HomepageDebugCommand.Click, HomepageDebugCommand.fromWireValue("click"))
        assertEquals(HomepageDebugCommand.DoubleClick, HomepageDebugCommand.fromWireValue("double-click"))
        assertEquals(HomepageDebugCommand.OpenCameraProbe, HomepageDebugCommand.fromWireValue("open-camera-probe"))
        assertEquals(HomepageDebugCommand.DumpState, HomepageDebugCommand.fromWireValue("dump-state"))
        assertEquals(null, HomepageDebugCommand.fromWireValue("unknown"))
    }

    @Test
    fun `click delegates to router`() {
        var called = false
        val commandRouter = HomepageDebugCommandRouter(
            handleTempleAction = {
                called = true
                true
            },
            openCameraProbe = { true },
            dumpState = { "unused" },
        )

        val result = commandRouter.handle(HomepageDebugCommand.Click)

        assertTrue(result.success)
        assertTrue(called)
        assertEquals("click:ok", result.message)
    }

    @Test
    fun `double click delegates to router`() {
        var action: HomepageTempleAction? = null
        val commandRouter = HomepageDebugCommandRouter(
            handleTempleAction = {
                action = it
                true
            },
            openCameraProbe = { true },
            dumpState = { "unused" },
        )

        val result = commandRouter.handle(HomepageDebugCommand.DoubleClick)

        assertEquals(HomepageTempleAction.DoubleClick, action)
        assertTrue(result.success)
        assertEquals("double-click:ok", result.message)
    }

    @Test
    fun `router surfaces failed command`() {
        val commandRouter = HomepageDebugCommandRouter(
            handleTempleAction = { false },
            openCameraProbe = { true },
            dumpState = { "unused" },
        )

        val result = commandRouter.handle(HomepageDebugCommand.Click)

        assertFalse(result.success)
        assertEquals("click:failed", result.message)
    }

    @Test
    fun `dump state returns homepage snapshot`() {
        val commandRouter = HomepageDebugCommandRouter(
            handleTempleAction = { true },
            openCameraProbe = { true },
            dumpState = { "mode=NativeOnly;status=NativeOnly" },
        )

        val result = commandRouter.handle(HomepageDebugCommand.DumpState)

        assertTrue(result.success)
        assertEquals("mode=NativeOnly;status=NativeOnly", result.message)
    }

    @Test
    fun `open camera probe delegates to launcher`() {
        var launched = false
        val commandRouter = HomepageDebugCommandRouter(
            handleTempleAction = { true },
            openCameraProbe = {
                launched = true
                true
            },
            dumpState = { "unused" },
        )

        val result = commandRouter.handle(HomepageDebugCommand.OpenCameraProbe)

        assertTrue(launched)
        assertTrue(result.success)
        assertEquals("open-camera-probe:ok", result.message)
    }
}
