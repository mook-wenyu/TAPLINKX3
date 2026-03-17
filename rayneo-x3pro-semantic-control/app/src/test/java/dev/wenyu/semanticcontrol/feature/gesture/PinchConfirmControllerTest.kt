package dev.wenyu.semanticcontrol.feature.gesture

import dev.wenyu.semanticcontrol.core.contracts.GestureSignal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PinchConfirmControllerTest {

    @Test
    fun `pinch confirm activates focused target after arm state`() {
        var activationCount = 0
        val controller = PinchConfirmController(
            gestureEngine = GestureEngine(),
            onConfirm = {
                activationCount += 1
                true
            },
        )

        val armedResult = controller.onSignal(GestureSignal.PinchStarted)
        val confirmResult = controller.onSignal(GestureSignal.PinchConfirmed)

        assertFalse(armedResult)
        assertTrue(confirmResult)
        assertEquals(1, activationCount)
    }

    @Test
    fun `pinch confirm does nothing when signal sequence never reaches confirmed`() {
        var activationCount = 0
        val controller = PinchConfirmController(
            gestureEngine = GestureEngine(),
            onConfirm = {
                activationCount += 1
                true
            },
        )

        val cancelledResult = controller.onSignal(GestureSignal.PinchCancelled)

        assertFalse(cancelledResult)
        assertEquals(0, activationCount)
    }

    @Test
    fun `pinch confirm only triggers once until cooldown exits`() {
        var activationCount = 0
        val controller = PinchConfirmController(
            gestureEngine = GestureEngine(),
            onConfirm = {
                activationCount += 1
                true
            },
        )

        controller.onSignal(GestureSignal.PinchStarted)
        val firstConfirmResult = controller.onSignal(GestureSignal.PinchConfirmed)
        val secondConfirmResult = controller.onSignal(GestureSignal.PinchConfirmed)

        assertTrue(firstConfirmResult)
        assertFalse(secondConfirmResult)
        assertEquals(1, activationCount)
    }

    @Test
    fun `pinch confirm surfaces activation failure without retrying`() {
        var activationCount = 0
        val controller = PinchConfirmController(
            gestureEngine = GestureEngine(),
            onConfirm = {
                activationCount += 1
                false
            },
        )

        controller.onSignal(GestureSignal.PinchStarted)
        val confirmResult = controller.onSignal(GestureSignal.PinchConfirmed)

        assertFalse(confirmResult)
        assertEquals(1, activationCount)
    }
}
