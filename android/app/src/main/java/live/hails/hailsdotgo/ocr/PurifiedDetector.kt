package live.hails.hailsdotgo.ocr

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log

object PurifiedDetector {
    private const val TAG = "PurifiedDetector"

    // The purified crystal icon is a small silver/white 6-pointed star with a blue-purple glow.
    // It appears in the Pokémon card area at ~50-63% height, 5-45% width.
    // Plain card background: R ≈ G ≈ B (neutral white) → filtered out by (b - g) > 15.
    fun detect(bitmap: Bitmap): Boolean {
        val w = bitmap.width
        val h = bitmap.height
        val yStart = (h * 0.50f).toInt()
        val yEnd   = (h * 0.63f).toInt()
        val xEnd   = (w * 0.45f).toInt()
        val step   = 4
        var hits   = 0

        for (y in yStart until yEnd step step) {
            for (x in 0 until xEnd step step) {
                val px = bitmap.getPixel(x, y)
                if (isCrystal(px)) hits++
            }
        }
        Log.d(TAG, "purifiedHits=$hits (zone 50-63%, 0-45%)")
        return hits >= 8
    }

    // Bright pixel with noticeable blue/purple tint. Pure card white (R≈G≈B≈255) fails (b-g)>15.
    private fun isCrystal(color: Int): Boolean {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        return r > 170 && g > 150 && b > 200 && b >= r && (b - g) > 15
    }
}
