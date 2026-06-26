package live.hails.hailsdotgo.ocr

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import live.hails.hailsdotgo.data.PokemonDataRepository
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

object CPArcDetector {
    private const val TAG = "CPArcDetector"

    // Arc geometry calibrated empirically from device data (1080×2340).
    // Altaria CP=1790/maxCP=2133 → fill=0.839, endpoint found at 54°
    // Back-solved: EMPTY=195°, FULL=96°, SPAN=261°
    // Centre ~25.9% height, radius ~45.2% width (unchanged from screenshot calibration).
    private const val CX_FRAC       = 0.500f
    private const val CY_FRAC       = 0.259f
    private const val R_FRAC        = 0.452f
    private const val EMPTY_DEG     = 195
    private const val FULL_DEG      = 96
    private const val SPAN_DEG      = 261
    private const val BRIGHT_THRESH = 580

    fun detect(bitmap: Bitmap, pokemonName: String, trainerLevel: Int): Int? {
        if (pokemonName.isBlank()) return null
        val maxCP = PokemonDataRepository.maxCP(pokemonName, trainerLevel) ?: run {
            Log.d(TAG, "no data for '$pokemonName'"); return null
        }

        val w  = bitmap.width.toFloat()
        val h  = bitmap.height.toFloat()
        val cx = w * CX_FRAC
        val cy = h * CY_FRAC
        val r  = w * R_FRAC

        // Scan counterclockwise from FULL. Skip the camera icon (a white UI circle
        // in the top-right corner at ~x>83%, y<18%) which sits on the scan path at
        // ~54° and would otherwise create a false endpoint for low-fill Pokémon.
        var endpointDeg = EMPTY_DEG
        for (step in 0..SPAN_DEG) {
            val deg = ((FULL_DEG - step) % 360 + 360) % 360
            val rad = Math.toRadians((deg - 90).toDouble())
            val x   = (cx + r * cos(rad)).toInt().coerceIn(0, bitmap.width - 1)
            val y   = (cy + r * sin(rad)).toInt().coerceIn(0, bitmap.height - 1)
            val xFrac = x.toFloat() / bitmap.width
            val yFrac = y.toFloat() / bitmap.height
            if (xFrac > 0.83f && yFrac < 0.18f) continue  // camera icon zone
            val px  = bitmap.getPixel(x, y)
            val b   = Color.red(px) + Color.green(px) + Color.blue(px)
            if (b > BRIGHT_THRESH) { endpointDeg = deg; break }
        }

        val clockwiseDist = ((endpointDeg - EMPTY_DEG) + 360) % 360
        val fillPct       = (clockwiseDist / SPAN_DEG.toFloat()).coerceIn(0f, 1f)
        val arcCP         = (fillPct * maxCP).roundToInt()
        Log.d(TAG, "endpoint=${endpointDeg}° fill=${"%.3f".format(fillPct)} maxCP=$maxCP → arcCP=$arcCP")
        return if (arcCP > 10) arcCP else null
    }
}
