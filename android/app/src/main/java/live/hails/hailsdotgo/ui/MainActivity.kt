package live.hails.hailsdotgo.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import live.hails.hailsdotgo.api.ApiClient
import live.hails.hailsdotgo.capture.CaptureState
import live.hails.hailsdotgo.capture.ScreenCaptureService
import live.hails.hailsdotgo.overlay.OverlayService
import live.hails.hailsdotgo.ui.theme.HailsDotGoTheme

class MainActivity : ComponentActivity() {

    companion object {
        const val ACTION_IV_SCAN_COMPLETE = "live.hails.hailsdotgo.IV_SCAN_COMPLETE"
    }

    private val _scanCompleted = mutableStateOf(false)

    private val projectionLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            CaptureState.projectionResultCode = result.resultCode
            CaptureState.projectionData       = result.data

            // Start ScreenCaptureService FIRST — Android 14 requires MEDIA_PROJECTION foreground
            // service to start within seconds of consent. Reads projection from CaptureState
            // (same process, avoids parceling the Binder token through Intent extras).
            ContextCompat.startForegroundService(this, Intent(this, ScreenCaptureService::class.java))

            // Then show the overlay bubble
            startService(Intent(this, OverlayService::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleScanIntent(intent)
        setContent {
            HailsDotGoTheme {
                NavGraph(
                    isLoggedIn     = ApiClient.hasToken(),
                    onStartScanner = ::requestScannerPermissions,
                    scanCompleted  = _scanCompleted.value,
                    onScanConsumed = { _scanCompleted.value = false },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleScanIntent(intent)
    }

    private fun handleScanIntent(intent: Intent?) {
        if (intent?.action == ACTION_IV_SCAN_COMPLETE) {
            _scanCompleted.value = true
        }
    }

    fun requestScannerPermissions() {
        if (!Settings.canDrawOverlays(this)) {
            startActivity(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName"))
            )
        } else if (CaptureState.hasProjection()) {
            // Already have a live projection — just re-show the bubble
            startService(Intent(this, OverlayService::class.java))
        } else {
            val manager = getSystemService(MEDIA_PROJECTION_SERVICE)
                    as android.media.projection.MediaProjectionManager
            projectionLauncher.launch(manager.createScreenCaptureIntent())
        }
    }
}
