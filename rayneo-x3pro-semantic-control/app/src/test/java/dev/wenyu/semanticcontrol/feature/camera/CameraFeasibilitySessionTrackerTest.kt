package dev.wenyu.semanticcontrol.feature.camera

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CameraFeasibilitySessionTrackerTest {

    @Test
    fun `permission required becomes explicit blocker`() {
        val tracker = CameraFeasibilitySessionTracker(clock = FakeClock(0L, 0L)::now)

        val snapshot = tracker.markPermissionRequired()

        assertEquals(CameraFeasibilityStatus.PermissionRequired, snapshot.status)
        assertEquals("camera-permission-required", snapshot.errorMessage)
        assertEquals(0, snapshot.analyzedFrames)
        assertNull(snapshot.cameraId)
    }

    @Test
    fun `first analyzed frame records streaming latency`() {
        val tracker = CameraFeasibilitySessionTracker(clock = FakeClock(1_000L, 1_150L)::now)

        tracker.markOpening(
            cameraId = "0",
            resolution = CameraProbeResolution(width = 1280, height = 720),
        )
        val snapshot = tracker.markFrameAnalyzed()

        assertEquals(CameraFeasibilityStatus.Streaming, snapshot.status)
        assertEquals("0", snapshot.cameraId)
        assertEquals(CameraProbeResolution(1280, 720), snapshot.resolution)
        assertEquals(1, snapshot.analyzedFrames)
        assertEquals(150L, snapshot.firstFrameLatencyMs)
    }

    @Test
    fun `subsequent frames keep first frame latency`() {
        val tracker = CameraFeasibilitySessionTracker(clock = FakeClock(2_000L, 2_130L, 2_260L)::now)

        tracker.markOpening(
            cameraId = "1",
            resolution = CameraProbeResolution(width = 1920, height = 1080),
        )
        tracker.markFrameAnalyzed()
        val snapshot = tracker.markFrameAnalyzed()

        assertEquals(CameraFeasibilityStatus.Streaming, snapshot.status)
        assertEquals(2, snapshot.analyzedFrames)
        assertEquals(130L, snapshot.firstFrameLatencyMs)
        assertEquals(2_260L, snapshot.lastFrameTimestampMs)
    }

    @Test
    fun `error preserves last known camera context`() {
        val tracker = CameraFeasibilitySessionTracker(clock = FakeClock(3_000L)::now)

        tracker.markOpening(
            cameraId = "0",
            resolution = CameraProbeResolution(width = 640, height = 480),
        )
        val snapshot = tracker.markError("open-failed")

        assertEquals(CameraFeasibilityStatus.Error, snapshot.status)
        assertEquals("0", snapshot.cameraId)
        assertEquals(CameraProbeResolution(640, 480), snapshot.resolution)
        assertEquals("open-failed", snapshot.errorMessage)
    }

    @Test
    fun `closed snapshot keeps measured frames for reporting`() {
        val tracker = CameraFeasibilitySessionTracker(clock = FakeClock(4_000L, 4_100L)::now)

        tracker.markOpening(
            cameraId = "0",
            resolution = CameraProbeResolution(width = 1280, height = 720),
        )
        tracker.markFrameAnalyzed()
        val snapshot = tracker.markClosed()

        assertEquals(CameraFeasibilityStatus.Closed, snapshot.status)
        assertEquals(1, snapshot.analyzedFrames)
        assertEquals(100L, snapshot.firstFrameLatencyMs)
    }

    @Test
    fun `new opening resets prior evidence`() {
        val tracker = CameraFeasibilitySessionTracker(clock = FakeClock(5_000L, 5_120L, 6_000L)::now)

        tracker.markOpening(
            cameraId = "0",
            resolution = CameraProbeResolution(width = 1280, height = 720),
        )
        tracker.markFrameAnalyzed()
        val snapshot = tracker.markOpening(
            cameraId = "1",
            resolution = CameraProbeResolution(width = 640, height = 480),
        )

        assertEquals(CameraFeasibilityStatus.Opening, snapshot.status)
        assertEquals("1", snapshot.cameraId)
        assertEquals(CameraProbeResolution(640, 480), snapshot.resolution)
        assertEquals(0, snapshot.analyzedFrames)
        assertNull(snapshot.firstFrameLatencyMs)
        assertNull(snapshot.lastFrameTimestampMs)
        assertNull(snapshot.errorMessage)
    }

    private class FakeClock(vararg values: Long) {
        private val queue = ArrayDeque(values.toList())

        fun now(): Long {
            return queue.removeFirstOrNull() ?: error("Fake clock exhausted")
        }
    }
}
