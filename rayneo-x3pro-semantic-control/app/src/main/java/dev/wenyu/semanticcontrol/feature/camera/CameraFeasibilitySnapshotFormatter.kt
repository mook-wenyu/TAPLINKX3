package dev.wenyu.semanticcontrol.feature.camera

data class CameraFeasibilityUiCopy(
    val statusLine: String,
    val detailLine: String,
    val hintLine: String,
)

class CameraFeasibilitySnapshotFormatter {
    fun format(snapshot: CameraFeasibilitySnapshot): CameraFeasibilityUiCopy {
        val statusLine = when (snapshot.status) {
            CameraFeasibilityStatus.Idle -> "等待前台相机探针"
            CameraFeasibilityStatus.PermissionRequired -> "需要授予相机权限"
            CameraFeasibilityStatus.Opening -> "正在打开前台相机"
            CameraFeasibilityStatus.Streaming -> "前台取流中"
            CameraFeasibilityStatus.Error -> "前台取流失败"
            CameraFeasibilityStatus.Closed -> "相机会话已关闭"
        }

        val detailParts = mutableListOf<String>()
        snapshot.cameraId?.let { detailParts += "camera=$it" }
        snapshot.resolution?.let { detailParts += "size=${it.width}x${it.height}" }
        if (snapshot.analyzedFrames > 0) {
            detailParts += "frames=${snapshot.analyzedFrames}"
        }
        snapshot.firstFrameLatencyMs?.let { detailParts += "firstFrame=${it}ms" }
        snapshot.errorMessage?.let { detailParts += "reason=$it" }

        val hintLine = when (snapshot.status) {
            CameraFeasibilityStatus.PermissionRequired -> "请先允许前台相机权限，再继续探针。"
            CameraFeasibilityStatus.Streaming -> "观察手是否稳定进入画面；双击右镜腿退出。"
            CameraFeasibilityStatus.Error -> "记录错误后退出本页，回到计划文档收敛结论。"
            else -> "当前只验证前台取流，不承诺后台常驻或手势识别。"
        }

        return CameraFeasibilityUiCopy(
            statusLine = statusLine,
            detailLine = detailParts.joinToString(" | ").ifEmpty { "waiting-for-device-evidence" },
            hintLine = hintLine,
        )
    }
}
