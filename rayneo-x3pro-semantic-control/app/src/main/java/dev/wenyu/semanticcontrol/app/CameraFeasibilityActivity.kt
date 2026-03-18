package dev.wenyu.semanticcontrol.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.TextureView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ffalcon.mercury.android.sdk.touch.TempleAction
import com.ffalcon.mercury.android.sdk.ui.activity.BaseMirrorActivity
import dev.wenyu.semanticcontrol.app.databinding.ActivityCameraFeasibilityBinding
import dev.wenyu.semanticcontrol.feature.camera.CameraFeasibilitySessionTracker
import dev.wenyu.semanticcontrol.feature.camera.CameraFeasibilitySnapshotFormatter
import dev.wenyu.semanticcontrol.feature.camera.CameraProbeResolution
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFeasibilityActivity : BaseMirrorActivity<ActivityCameraFeasibilityBinding>() {

    private val sessionTracker = CameraFeasibilitySessionTracker()
    private val snapshotFormatter = CameraFeasibilitySnapshotFormatter()
    private val previewSurfaceLock = Any()
    private val previewSurfaces = mutableListOf<Surface>()

    private lateinit var backgroundThread: HandlerThread
    private lateinit var backgroundHandler: Handler
    private lateinit var sessionExecutor: ExecutorService
    private lateinit var cameraManager: CameraManager

    private var selectedCameraId: String? = null
    private var selectedResolution: CameraProbeResolution = CameraProbeResolution(1280, 720)
    private var imageReader: ImageReader? = null
    private var cameraDevice: CameraDevice? = null
    private var cameraCaptureSession: CameraCaptureSession? = null
    private var openRequested = false
    private var activeSessionToken = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activeInstance = this
        backgroundThread = HandlerThread("camera-feasibility")
        backgroundThread.start()
        backgroundHandler = Handler(backgroundThread.looper)
        sessionExecutor = Executors.newSingleThreadExecutor()
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        renderSnapshot(sessionTracker.current())
        installTempleActions()
        prepareCameraConfig()
        installPreviewListeners()
        ensureCameraPermission()
    }

    override fun onResume() {
        super.onResume()
        ensureCameraReadyIfPossible()
    }

    override fun onStop() {
        closeCamera()
        super.onStop()
    }

    override fun onDestroy() {
        closeCamera()
        sessionExecutor.shutdown()
        backgroundThread.quitSafely()
        if (activeInstance === this) {
            activeInstance = null
        }
        super.onDestroy()
    }

    private fun installTempleActions() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                templeActionViewModel.state.collect { action ->
                    when (action) {
                        is TempleAction.DoubleClick -> finish()
                        is TempleAction.Click -> renderSnapshot(sessionTracker.current())
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun installPreviewListeners() {
        mBindingPair.updateView {
            cameraPreview.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                private var previewSurface: Surface? = null

                override fun onSurfaceTextureAvailable(
                    surface: SurfaceTexture,
                    width: Int,
                    height: Int,
                ) {
                    surface.setDefaultBufferSize(selectedResolution.width, selectedResolution.height)
                    val newSurface = Surface(surface)
                    previewSurface = newSurface
                    synchronized(previewSurfaceLock) {
                        previewSurfaces += newSurface
                    }
                    ensureCameraReadyIfPossible()
                }

                override fun onSurfaceTextureSizeChanged(
                    surface: SurfaceTexture,
                    width: Int,
                    height: Int,
                ) = Unit

                override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                    previewSurface?.let {
                        synchronized(previewSurfaceLock) {
                            previewSurfaces.remove(it)
                        }
                        it.release()
                    }
                    previewSurface = null
                    closeCamera()
                    return true
                }

                override fun onSurfaceTextureUpdated(surface: SurfaceTexture) = Unit
            }
        }
    }

    private fun prepareCameraConfig() {
        runCatching {
            val cameraId = cameraManager.cameraIdList.firstOrNull()
                ?: error("camera-not-found")
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val configurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                ?: error("stream-config-unavailable")
            val previewSize = selectPreferredPreviewSize(configurationMap.getOutputSizes(SurfaceTexture::class.java))
            selectedCameraId = cameraId
            selectedResolution = CameraProbeResolution(previewSize.width, previewSize.height)
        }.onFailure { error ->
            renderSnapshot(sessionTracker.markError(error.message ?: "camera-config-failed"))
            Log.e(TAG, "Failed to prepare camera config", error)
        }
    }

    private fun selectPreferredPreviewSize(sizes: Array<Size>): Size {
        return sizes.firstOrNull { it.width == 1280 && it.height == 720 }
            ?: sizes.firstOrNull { it.width == 640 && it.height == 480 }
            ?: sizes
                .sortedByDescending { it.width * it.height }
                .firstOrNull { it.width * 9 == it.height * 16 }
            ?: sizes.maxBy { it.width * it.height }
    }

    private fun ensureCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            ensureCameraReadyIfPossible()
            return
        }

        renderSnapshot(sessionTracker.markPermissionRequired())
        requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != REQUEST_CAMERA_PERMISSION) {
            return
        }

        val granted = grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED
        if (granted) {
            ensureCameraReadyIfPossible()
        } else {
            renderSnapshot(sessionTracker.markPermissionRequired())
        }
    }

    private fun ensureCameraReadyIfPossible() {
        if (openRequested || cameraDevice != null) {
            return
        }
        if (selectedCameraId == null || snapshotPreviewSurfaces().size < 2) {
            return
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        openRequested = true
        openCamera()
    }

    @SuppressLint("MissingPermission")
    private fun openCamera() {
        val cameraId = selectedCameraId
        if (cameraId == null) {
            renderSnapshot(sessionTracker.markError("camera-id-unavailable"))
            openRequested = false
            return
        }

        val sessionToken = ++activeSessionToken

        renderSnapshot(
            sessionTracker.markOpening(
                cameraId = cameraId,
                resolution = selectedResolution,
            ),
        )

        runCatching {
            cameraManager.openCamera(cameraId, createStateCallback(sessionToken), backgroundHandler)
        }.onFailure { error ->
            openRequested = false
            renderSnapshot(sessionTracker.markError(error.message ?: "open-camera-failed"))
            Log.e(TAG, "Failed to open camera", error)
        }
    }

    private fun createStateCallback(sessionToken: Int) = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            if (!isSessionCurrent(sessionToken)) {
                camera.close()
                return
            }
            cameraDevice = camera
            openRequested = false
            setUpSession(camera, sessionToken)
        }

        override fun onDisconnected(camera: CameraDevice) {
            camera.close()
            if (!isSessionCurrent(sessionToken)) {
                return
            }
            closeCamera()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            camera.close()
            if (!isSessionCurrent(sessionToken)) {
                return
            }
            failSession("camera-error-$error")
        }
    }

    private fun setUpSession(camera: CameraDevice, sessionToken: Int) {
        if (!isSessionCurrent(sessionToken)) {
            camera.close()
            return
        }
        val previewSurfaceSnapshot = snapshotPreviewSurfaces()
        if (previewSurfaceSnapshot.size < 2) {
            if (isSessionCurrent(sessionToken)) {
                failSession("preview-surfaces-unavailable")
            }
            camera.close()
            return
        }
        imageReader?.close()
        imageReader = ImageReader.newInstance(
            selectedResolution.width,
            selectedResolution.height,
            ImageFormat.YUV_420_888,
            4,
        ).apply {
            setOnImageAvailableListener({ reader ->
                val image = reader.acquireLatestImage() ?: return@setOnImageAvailableListener
                image.close()
                if (!isSessionCurrent(sessionToken)) {
                    return@setOnImageAvailableListener
                }
                val snapshot = sessionTracker.markFrameAnalyzed()
                if (snapshot.analyzedFrames == 1 || snapshot.analyzedFrames % FRAME_REPORT_INTERVAL == 0) {
                    runOnUiThread {
                        if (!isSessionCurrent(sessionToken)) {
                            return@runOnUiThread
                        }
                        renderSnapshot(snapshot)
                    }
                }
            }, backgroundHandler)
        }

        val captureRequest = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
            addTarget(imageReader!!.surface)
            previewSurfaceSnapshot.forEach(::addTarget)
        }

        val outputs = buildList {
            add(OutputConfiguration(imageReader!!.surface))
            previewSurfaceSnapshot.forEach { add(OutputConfiguration(it)) }
        }

        val configuration = SessionConfiguration(
            SessionConfiguration.SESSION_REGULAR,
            outputs,
            sessionExecutor,
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    if (!isSessionCurrent(sessionToken)) {
                        session.close()
                        return
                    }
                    cameraCaptureSession = session
                    session.setRepeatingRequest(captureRequest.build(), null, backgroundHandler)
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    if (!isSessionCurrent(sessionToken)) {
                        session.close()
                        return
                    }
                    failSession("session-config-failed")
                }
            },
        )

        runCatching {
            camera.createCaptureSession(configuration)
        }.onFailure { error ->
            if (!isSessionCurrent(sessionToken)) {
                return@onFailure
            }
            failSession(error.message ?: "create-session-failed")
            Log.e(TAG, "Failed to create camera session", error)
        }
    }

    private fun isSessionCurrent(sessionToken: Int): Boolean {
        return sessionToken == activeSessionToken
    }

    private fun snapshotPreviewSurfaces(): List<Surface> {
        return synchronized(previewSurfaceLock) {
            previewSurfaces.toList()
        }
    }

    private fun failSession(reason: String) {
        closeCamera(reportClosed = false)
        postSnapshot(sessionTracker.markError(reason))
    }

    private fun closeCamera(reportClosed: Boolean = true) {
        activeSessionToken += 1
        cameraCaptureSession?.close()
        cameraCaptureSession = null
        cameraDevice?.close()
        cameraDevice = null
        imageReader?.close()
        imageReader = null
        openRequested = false
        if (reportClosed) {
            postSnapshot(sessionTracker.markClosed())
        }
    }

    private fun postSnapshot(snapshot: dev.wenyu.semanticcontrol.feature.camera.CameraFeasibilitySnapshot) {
        if (Thread.currentThread() === mainLooper.thread) {
            renderSnapshot(snapshot)
            return
        }
        runOnUiThread {
            renderSnapshot(snapshot)
        }
    }

    private fun renderSnapshot(snapshot: dev.wenyu.semanticcontrol.feature.camera.CameraFeasibilitySnapshot) {
        val copy = snapshotFormatter.format(snapshot)
        mBindingPair.updateView {
            probeTitleText.setText(R.string.camera_probe_title)
            probeStatusText.text = copy.statusLine
            probeDetailText.text = copy.detailLine
            probeHintText.text = copy.hintLine
        }
        Log.i(TAG, "Camera probe snapshot: $snapshot")
    }

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 7001
        private const val FRAME_REPORT_INTERVAL = 30
        private const val TAG = "CameraFeasibility"

        @Volatile
        var activeInstance: CameraFeasibilityActivity? = null
            private set
    }
}
