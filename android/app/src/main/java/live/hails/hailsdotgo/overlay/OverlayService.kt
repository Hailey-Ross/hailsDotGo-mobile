package live.hails.hailsdotgo.overlay

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import live.hails.hailsdotgo.capture.CaptureState
import live.hails.hailsdotgo.capture.ScreenCaptureService
import live.hails.hailsdotgo.ui.iv.IVResultScreen
import live.hails.hailsdotgo.ui.theme.HailsDotGoTheme
import kotlin.math.abs
import kotlin.math.roundToInt

class OverlayService : Service() {

    companion object {
        const val ACTION_SHOW_IV_CARD = "live.hails.hailsdotgo.SHOW_IV_CARD"
    }

    private var windowManager : WindowManager? = null
    private var bubbleView    : ImageView?     = null
    private var menuView      : View?          = null
    private var ivCardView    : View?          = null
    private var ivOwner       : OverlayLifecycleOwner? = null

    private var bubbleParams  : WindowManager.LayoutParams? = null
    private var bubbleSizePx  : Int = 0

    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        if (intent?.action == ACTION_SHOW_IV_CARD) {
            showIVCard()
            return START_NOT_STICKY
        }

        // First start or re-start: set up the bubble
        bubbleView?.let { safely { windowManager?.removeView(it) } }
        dismissMenu()

        val density  = resources.displayMetrics.density
        val sizePx   = (56 * density).roundToInt()
        bubbleSizePx = sizePx

        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(0xFF7C4DFF.toInt())
            setSize(sizePx, sizePx)
        }
        val imageView = ImageView(this).apply {
            setImageDrawable(drawable)
            contentDescription = "Scan Pokémon — long press for options"
        }

        val params = WindowManager.LayoutParams(
            sizePx, sizePx,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 24; y = 300
        }
        bubbleParams = params

        var startX = 0f; var startY = 0f
        var startRawX = 0f; var startRawY = 0f
        var moved = false
        // Track whether the menu was already visible BEFORE this touch started —
        // so ACTION_UP can distinguish "close existing menu" from "just opened by long press".
        var menuWasOpenAtDown = false

        val longPressRunnable = Runnable { showMenu() }

        imageView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = params.x.toFloat(); startY = params.y.toFloat()
                    startRawX = event.rawX;      startRawY = event.rawY
                    moved = false
                    menuWasOpenAtDown = menuView != null
                    imageView.postDelayed(longPressRunnable, 600)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - startRawX
                    val dy = event.rawY - startRawY
                    if (abs(dx) > 8 || abs(dy) > 8) {
                        moved = true
                        imageView.removeCallbacks(longPressRunnable)
                        params.x = (startX + dx).toInt()
                        params.y = (startY + dy).toInt()
                        windowManager?.updateViewLayout(imageView, params)
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    imageView.removeCallbacks(longPressRunnable)
                    if (!moved && event.action == MotionEvent.ACTION_UP) {
                        when {
                            // Menu was open BEFORE this touch → tap closes it
                            menuWasOpenAtDown -> dismissMenu()
                            // Menu is open now (just opened by long press this touch) → leave it open
                            menuView != null  -> { /* long press fired, keep menu */ }
                            // Normal tap → scan
                            else              -> onBubbleTapped()
                        }
                    }
                    true
                }
                else -> false
            }
        }

        windowManager?.addView(imageView, params)
        bubbleView = imageView
        return START_NOT_STICKY
    }

    // ── IV overlay card ───────────────────────────────────────────────────────

    private fun showIVCard() {
        dismissIVCard(clearAccumulated = false)  // keep accumulated OCR for the new ViewModel

        val owner = OverlayLifecycleOwner()
        ivOwner = owner
        owner.start()

        val metrics = resources.displayMetrics
        val cardHeight = (metrics.heightPixels * 0.45f).roundToInt()

        val composeView = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setViewTreeLifecycleOwner(owner)
            setViewTreeViewModelStoreOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setContent {
                CompositionLocalProvider(
                    LocalLifecycleOwner provides owner,
                    LocalViewModelStoreOwner provides owner,
                ) {
                    HailsDotGoTheme(darkTheme = true) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape    = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                        ) {
                            IVResultScreen(onDismiss = { dismissIVCard(clearAccumulated = true) })
                        }
                    }
                }
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            cardHeight,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.BOTTOM
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        }

        windowManager?.addView(composeView, params)
        ivCardView = composeView
    }

    private fun dismissIVCard(clearAccumulated: Boolean = false) {
        ivCardView?.let { safely { windowManager?.removeView(it) } }
        ivCardView = null
        ivOwner?.stop()
        ivOwner = null
        if (clearAccumulated) CaptureState.clearOCR()
    }

    // ── Bubble long-press menu ────────────────────────────────────────────────

    private fun showMenu() {
        if (menuView != null) return
        val density = resources.displayMetrics.density
        val bp      = bubbleParams ?: return
        val screenW = resources.displayMetrics.widthPixels

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background  = GradientDrawable().apply {
                shape        = GradientDrawable.RECTANGLE
                setColor(0xFF1E0C4B.toInt())
                cornerRadius = 14 * density
            }
            elevation = 12 * density
        }

        fun item(label: String, color: Int = 0xFFE0D7FF.toInt(), action: () -> Unit) {
            layout.addView(TextView(this).apply {
                text     = label
                textSize = 16f
                setTextColor(color)
                val hp = (20 * density).roundToInt()
                val vp = (14 * density).roundToInt()
                setPadding(hp, vp, hp * 2, vp)
                setOnClickListener { action() }
            })
            layout.addView(View(this).apply {
                setBackgroundColor(0x22E0D7FF)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1)
            })
        }

        item("Scan Pokémon") { dismissMenu(); onBubbleTapped() }
        item("Remove Overlay", 0xFFEF5350.toInt()) { dismissMenu(); dismiss() }
        item("Cancel") { dismissMenu() }

        // Remove trailing divider
        if (layout.childCount > 0) layout.removeViewAt(layout.childCount - 1)

        val menuW  = (200 * density).roundToInt()
        val xRight = bp.x + bubbleSizePx + (8 * density).roundToInt()
        val menuX  = if (xRight + menuW < screenW) xRight
                     else bp.x - menuW - (8 * density).roundToInt()

        val menuParams = WindowManager.LayoutParams(
            menuW,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = menuX; y = bp.y
        }

        windowManager?.addView(layout, menuParams)
        menuView = layout
    }

    private fun dismissMenu() {
        menuView?.let { safely { windowManager?.removeView(it) } }
        menuView = null
    }

    // ── Core actions ──────────────────────────────────────────────────────────

    private fun onBubbleTapped() {
        startService(Intent(this, ScreenCaptureService::class.java).apply {
            action = ScreenCaptureService.ACTION_CAPTURE
        })
    }

    private fun dismiss() {
        dismissIVCard(clearAccumulated = true)
        stopService(Intent(this, ScreenCaptureService::class.java))
        CaptureState.projectionResultCode = 0
        CaptureState.projectionData = null
        stopSelf()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        dismiss()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        dismissMenu()
        dismissIVCard()
        bubbleView?.let { safely { windowManager?.removeView(it) } }
        super.onDestroy()
    }

    private inline fun safely(block: () -> Unit) { try { block() } catch (_: Exception) {} }
}
