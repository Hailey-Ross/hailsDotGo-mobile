package live.hails.hailsdotgo.capture

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import live.hails.hailsdotgo.ocr.OCRProcessor
import live.hails.hailsdotgo.overlay.OverlayService
import live.hails.hailsdotgo.util.buildScannerNotification
import java.io.File
import java.io.FileOutputStream

class ScreenCaptureService : Service() {

    companion object {
        const val ACTION_CAPTURE   = "live.hails.hailsdotgo.CAPTURE"
        private const val NOTIF_ID = 1002
        private const val TAG      = "ScreenCaptureSvc"
    }

    private var projection    : MediaProjection? = null
    private var virtualDisplay: VirtualDisplay?  = null
    private var imageReader   : ImageReader?     = null
    private val mainHandler   = Handler(Looper.getMainLooper())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_CAPTURE) {
            scheduleCapture(0)
            return START_NOT_STICKY
        }

        // Init — must call startForeground before doing anything else.
        // Projection data comes from CaptureState (same-process singleton, no parceling).
        val resultCode = CaptureState.projectionResultCode  // will be Activity.RESULT_OK = -1
        val data       = CaptureState.projectionData

        // startForeground MUST be called before anything else on API 29+.
        // Always include the mediaProjection type when the manifest declares it — omitting the
        // type throws MissingForegroundServiceTypeException on targetSdk >= 34.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIF_ID,
                buildScannerNotification(this),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION,
            )
        } else {
            startForeground(NOTIF_ID, buildScannerNotification(this))
        }

        if (data == null) {
            Log.e(TAG, "No projection data in CaptureState — aborting")
            stopSelf()
            return START_NOT_STICKY
        }

        val manager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        projection = manager.getMediaProjection(resultCode, data)

        if (projection == null) {
            Log.e(TAG, "getMediaProjection returned null")
            stopSelf()
            return START_NOT_STICKY
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            projection?.registerCallback(object : MediaProjection.Callback() {
                override fun onStop() { cleanup() }
            }, mainHandler)
        }

        val metrics = resources.displayMetrics
        imageReader = ImageReader.newInstance(
            metrics.widthPixels, metrics.heightPixels, PixelFormat.RGBA_8888, 2,
        )
        virtualDisplay = projection?.createVirtualDisplay(
            "HailsDotGoCapture",
            metrics.widthPixels, metrics.heightPixels, metrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface, null, mainHandler,
        )

        Log.d(TAG, "MediaProjection active — virtual display ready")
        return START_NOT_STICKY
    }

    private fun scheduleCapture(retry: Int) {
        val image = imageReader?.acquireLatestImage()
        if (image == null) {
            if (retry < 10) mainHandler.postDelayed({ scheduleCapture(retry + 1) }, 150)
            else Log.e(TAG, "No frame available after retries")
            return
        }
        try {
            val bitmap = imageToBitmap(image)
            Log.d(TAG, "Frame acquired: ${bitmap.width}×${bitmap.height}")
            saveBitmapDebug(bitmap)
            OCRProcessor.process(bitmap) { result ->
                CaptureState.setOCRResult(result)
                openMainActivity()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Capture error", e)
        } finally {
            image.close()
        }
    }

    private fun imageToBitmap(image: Image): Bitmap {
        val plane      = image.planes[0]
        val pixelStride = plane.pixelStride
        val rowPadding  = plane.rowStride - pixelStride * image.width
        val bmp = Bitmap.createBitmap(
            image.width + rowPadding / pixelStride,
            image.height,
            Bitmap.Config.ARGB_8888,
        )
        bmp.copyPixelsFromBuffer(plane.buffer)
        return Bitmap.createBitmap(bmp, 0, 0, image.width, image.height)
    }

    private fun saveBitmapDebug(bitmap: Bitmap) {
        try {
            val dir  = getExternalFilesDir(null) ?: cacheDir
            val file = File(dir, "capture_debug.png")
            FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 90, it) }
            Log.d(TAG, "Debug bitmap saved: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.w(TAG, "Could not save debug bitmap", e)
        }
    }

    private fun openMainActivity() {
        startService(Intent(this, OverlayService::class.java).apply {
            action = OverlayService.ACTION_SHOW_IV_CARD
        })
    }

    private fun cleanup() {
        try { imageReader?.close() }    catch (_: Exception) {}
        try { virtualDisplay?.release() } catch (_: Exception) {}
        try { projection?.stop() }      catch (_: Exception) {}
        imageReader    = null
        virtualDisplay = null
        projection     = null
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopForeground(STOP_FOREGROUND_REMOVE)
        cleanup()
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        cleanup()
        super.onDestroy()
    }
}
