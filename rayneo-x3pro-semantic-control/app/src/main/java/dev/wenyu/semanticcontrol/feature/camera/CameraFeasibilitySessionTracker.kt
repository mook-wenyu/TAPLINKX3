package dev.wenyu.semanticcontrol.feature.camera

enum class CameraFeasibilityStatus {
    Idle,
    PermissionRequired,
    Opening,
    Streaming,
    Error,
    Closed,
}

data class CameraProbeResolution(
    val width: Int,
    val height: Int,
)

data class CameraFeasibilitySnapshot(
    val status: CameraFeasibilityStatus = CameraFeasibilityStatus.Idle,
    val cameraId: String? = null,
    val resolution: CameraProbeResolution? = null,
    val analyzedFrames: Int = 0,
    val firstFrameLatencyMs: Long? = null,
    val lastFrameTimestampMs: Long? = null,
    val errorMessage: String? = null,
)

class CameraFeasibilitySessionTracker(
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private var openingStartedAtMs: Long? = null
    private var snapshot = CameraFeasibilitySnapshot()

    fun current(): CameraFeasibilitySnapshot = snapshot

    fun markPermissionRequired(): CameraFeasibilitySnapshot {
        openingStartedAtMs = null
        snapshot = CameraFeasibilitySnapshot(
            status = CameraFeasibilityStatus.PermissionRequired,
            errorMessage = "camera-permission-required",
        )
        return snapshot
    }

    fun markOpening(
        cameraId: String,
        resolution: CameraProbeResolution,
    ): CameraFeasibilitySnapshot {
        openingStartedAtMs = clock()
        snapshot = CameraFeasibilitySnapshot(
            status = CameraFeasibilityStatus.Opening,
            cameraId = cameraId,
            resolution = resolution,
        )
        return snapshot
    }

    fun markFrameAnalyzed(): CameraFeasibilitySnapshot {
        val now = clock()
        val nextFrameCount = snapshot.analyzedFrames + 1
        snapshot = snapshot.copy(
            status = CameraFeasibilityStatus.Streaming,
            analyzedFrames = nextFrameCount,
            firstFrameLatencyMs = snapshot.firstFrameLatencyMs ?: openingStartedAtMs?.let { now - it },
            lastFrameTimestampMs = now,
            errorMessage = null,
        )
        return snapshot
    }

    fun markError(message: String): CameraFeasibilitySnapshot {
        openingStartedAtMs = null
        snapshot = snapshot.copy(
            status = CameraFeasibilityStatus.Error,
            errorMessage = message,
        )
        return snapshot
    }

    fun markClosed(): CameraFeasibilitySnapshot {
        openingStartedAtMs = null
        snapshot = snapshot.copy(status = CameraFeasibilityStatus.Closed)
        return snapshot
    }
}
