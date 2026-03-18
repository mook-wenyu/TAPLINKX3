package dev.wenyu.semanticcontrol.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomepageTempleActionRouterTest {

    @Test
    fun `click triggers current CTA`() {
        var invoked = false
        val router = HomepageTempleActionRouter(
            performCurrentAction = {
                invoked = true
                true
            },
            onExit = { },
        )

        val result = router.handle(HomepageTempleAction.Click)

        assertTrue(result)
        assertTrue(invoked)
    }

    @Test
    fun `double click exits page`() {
        var exited = false
        val router = HomepageTempleActionRouter(
            performCurrentAction = { false },
            onExit = { exited = true },
        )

        val result = router.handle(HomepageTempleAction.DoubleClick)

        assertTrue(result)
        assertTrue(exited)
    }

    @Test
    fun `forward and backward gestures are ignored for single target homepage`() {
        val router = HomepageTempleActionRouter(
            performCurrentAction = { true },
            onExit = { },
        )

        assertFalse(router.handle(HomepageTempleAction.SlideForward))
        assertFalse(router.handle(HomepageTempleAction.SlideBackward))
    }

    @Test
    fun `failed current action is surfaced`() {
        val router = HomepageTempleActionRouter(
            performCurrentAction = { false },
            onExit = { },
        )

        val result = router.handle(HomepageTempleAction.Click)

        assertFalse(result)
    }

    @Test
    fun `unsupported action is ignored`() {
        val router = HomepageTempleActionRouter(
            performCurrentAction = { true },
            onExit = { },
        )

        assertFalse(router.handle(HomepageTempleAction.LongClick))
    }
}
