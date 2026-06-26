package live.hails.hailsdotgo.ocr

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log

object AppraisalBarDetector {
    private const val TAG = "AppraisalBarDetector"

    // Returns (starCount, isHundo).
    // Scan zone 58-70%: badge confirmed at ~60-66% depending on Pokémon.
    // Uses gap-based star counting instead of a total-hit threshold:
    //   - A zero-hit row INSIDE the orange span means badge ring only (≤2 stars).
    //   - A continuous orange band means badge interior is filled (3 stars).
    // This distinguishes 1-star (46 hits but with a gap) from 3-star (34 hits, no gap).
    fun detect(bitmap: Bitmap, appraisalVisible: Boolean = false): Pair<Int?, Boolean> {
        if (!appraisalVisible) return Pair(null, false)

        val w = bitmap.width
        val h = bitmap.height
        val xEnd = (w * 0.25f).toInt()

        val rows = listOf(
            0.58f, 0.59f, 0.60f, 0.61f, 0.62f, 0.63f, 0.64f,
            0.65f, 0.66f, 0.67f, 0.68f, 0.69f, 0.70f,
        )
        val samplesPerRow = 50

        val rowOrange  = IntArray(rows.size)
        val rowRainbow = IntArray(rows.size)

        for ((i, rowFrac) in rows.withIndex()) {
            val y = (h * rowFrac).toInt().coerceIn(0, h - 1)
            var orange  = 0
            var rainbow = 0
            for (step in 0 until samplesPerRow) {
                val x = (xEnd * (step.toDouble() / samplesPerRow)).toInt().coerceIn(0, w - 1)
                val px = bitmap.getPixel(x, y)
                if (isOrange(px))   orange++
                if (isRainbow(px)) rainbow++
            }
            rowOrange[i]  = orange
            rowRainbow[i] = rainbow
            Log.d(TAG, "row ${(rowFrac * 100).toInt()}% (y=$y): +$orange orange, +$rainbow rainbow")
        }

        for (frac in listOf(0.60f, 0.63f, 0.66f)) {
            val cx = (w * 0.08f).toInt().coerceIn(0, w - 1)
            val cy = (h * frac).toInt().coerceIn(0, h - 1)
            val px = bitmap.getPixel(cx, cy)
            Log.d(TAG, "pixel @${(frac * 100).toInt()}% ($cx,$cy): R=${Color.red(px)} G=${Color.green(px)} B=${Color.blue(px)}")
        }

        val total   = rowOrange.sum()
        val rainbow = rowRainbow.sum()

        val firstHit = rowOrange.indexOfFirst { it > 0 }
        val lastHit  = rowOrange.indexOfLast  { it > 0 }

        if (firstHit < 0 || total < 2) {
            Log.d(TAG, "total=$total → stars=0 isHundo=false")
            return Pair(0, false)
        }

        // Gap = zero-hit row inside the orange span. Indicates badge ring only (low stars).
        val hasGap = (firstHit..lastHit).any { rowOrange[it] == 0 }

        val innerTotal = if (lastHit > firstHit + 1)
            rowOrange.slice((firstHit + 1) until lastHit).sum() else 0

        val stars = when {
            !hasGap && total >= 10 -> 3
            hasGap -> if (innerTotal >= 10) 2 else 1
            total >= 3             -> 1
            else                   -> 0
        }

        val isHundo = stars == 3 && rainbow >= 3

        Log.d(TAG, "total=$total hasGap=$hasGap innerTotal=$innerTotal rainbow=$rainbow → stars=$stars isHundo=$isHundo")
        return Pair(stars, isHundo)
    }

    // Gold/orange star badge: high red, moderate green, low-ish blue.
    private fun isOrange(color: Int): Boolean {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        return r > 180 && g > 80 && b < 120 && r > g && (r - b) > 100
    }

    // Rainbow/hundo ring colours — not present on the plain gold 3-star badge.
    // Device data: ring pixel at 63% was R=255 G=140 B=160 (magenta/pink).
    private fun isRainbow(color: Int): Boolean {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        val isMagenta = r > 200 && b > 120 && g < 190 && (r - g) > 60 && b > g
        val isPurple  = b > 150 && b >= r && g < 150
        val isTeal    = g > 160 && b > 140 && r < 160
        return isMagenta || isPurple || isTeal
    }
}
