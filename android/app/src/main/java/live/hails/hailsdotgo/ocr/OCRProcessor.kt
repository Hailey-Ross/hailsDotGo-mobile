package live.hails.hailsdotgo.ocr

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

data class OCRResult(
    val pokemonName  : String  = "",
    val nameSource   : String  = "",   // "footer" | "mega" | "card" | ""
    val cp           : Int?    = null,
    val hp           : Int?    = null,
    val dustCost     : Int?    = null,
    val appraisalBars: Int?    = null,
    val isLucky      : Boolean = false,
    val isShadow     : Boolean = false,
    val isPurified   : Boolean = false,
    val isHundo      : Boolean = false,
)

object OCRProcessor {
    private const val TAG = "OCRProcessor"

    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    private val NOT_A_NAME = setOf(
        "normal", "fire", "water", "electric", "grass", "ice", "fighting",
        "poison", "ground", "flying", "psychic", "bug", "rock", "ghost",
        "dragon", "dark", "steel", "fairy",
        "attack", "defense", "hp", "cp", "candy", "stardust", "mega",
        "energy", "lucky", "weather", "boosted", "purified", "shadow",
        "height", "weight", "evolve", "power",
    )

    private val VALID_DUST = setOf(
        200, 400, 800, 1000, 1300, 1600, 1900, 2200, 2500,
        3000, 3500, 4000, 4500, 5000, 6000, 7000, 8000, 9000,
        10000, 12000, 15000, 20000,
    )

    private val DISPLAYABLE_DUST: Set<Int> by lazy {
        VALID_DUST.flatMap { d -> listOf(d, d / 2, d * 9 / 10, d * 6) }
            .filter { it > 0 }.toSet()
    }

    fun process(bitmap: Bitmap, onResult: (OCRResult) -> Unit) {
        recognizer.process(InputImage.fromBitmap(bitmap, 0))
            .addOnSuccessListener { visionText ->
                Log.d(TAG, "=== RAW OCR ===\n${visionText.text}\n===============")
                val topZone = bitmap.height * 0.25f
                visionText.textBlocks.flatMap { it.lines }
                    .filter { line ->
                        val b = line.boundingBox ?: return@filter false
                        (b.top + b.bottom) / 2f < topZone
                    }
                    .forEach { line ->
                        Log.d(TAG, "TOP line='${line.text}' elems=${line.elements.map { it.text }}")
                    }
                val result = parse(visionText, bitmap)
                Log.d(TAG, "Parsed → name='${result.pokemonName}' cp=${result.cp} hp=${result.hp} dust=${result.dustCost} bars=${result.appraisalBars}")

                val arcCP = CPArcDetector.detect(bitmap, result.pokemonName, live.hails.hailsdotgo.data.TokenStore.loadTrainerLevel())
                val textCP = result.cp
                // Always run high-contrast retry: large Pokémon models partially occlude
                // the CP digits and the first pass underreads (e.g. "2127" for "2197").
                // Taking max(textCP, retryCP) recovers the correct higher value.
                retryCP(bitmap) { retryCp ->
                    val bestTextCP = listOfNotNull(textCP, retryCp).filter { it >= 10 }.maxOrNull()
                    val bestCP     = bestTextCP ?: arcCP?.takeIf { it >= 10 }
                    Log.d(TAG, "textCP=$textCP retryCp=$retryCp arcCP=$arcCP → bestCP=$bestCP")
                    onResult(result.copy(cp = bestCP))
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "ML Kit error", e)
                onResult(OCRResult())
            }
    }

    // Runs a second OCR pass on a greyscale + contrast-boosted crop of the top 18% of
    // the screen. Removing colour kills glow interference; contrast makes digit edges pop.
    private fun retryCP(bitmap: Bitmap, onCp: (Int?) -> Unit) {
        val cropH = (bitmap.height * 0.18f).toInt().coerceAtLeast(1)
        val crop  = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, cropH)
        val processed = grayscaleContrast(crop, contrast = 1.5f)
        recognizer.process(InputImage.fromBitmap(processed, 0))
            .addOnSuccessListener { text ->
                Log.d(TAG, "CP retry raw: '${text.text.replace('\n', ' ')}'")
                val cp = Regex("""\b(\d{2,5})\b""").findAll(text.text)
                    .mapNotNull { it.groupValues[1].toIntOrNull() }
                    .filter { it in 10..50000 }
                    .maxOrNull()
                onCp(cp)
            }
            .addOnFailureListener { onCp(null) }
    }

    private fun grayscaleContrast(src: Bitmap, contrast: Float): Bitmap {
        val dst    = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(dst)
        val paint  = Paint()
        val grey   = ColorMatrix().also { it.setSaturation(0f) }
        val t      = 128f * (1f - contrast)
        val cont   = ColorMatrix(floatArrayOf(
            contrast, 0f, 0f, 0f, t,
            0f, contrast, 0f, 0f, t,
            0f, 0f, contrast, 0f, t,
            0f, 0f, 0f, 1f, 0f,
        ))
        cont.preConcat(grey)
        paint.colorFilter = ColorMatrixColorFilter(cont)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return dst
    }

    private fun parse(visionText: Text, bitmap: Bitmap): OCRResult {
        val singleLine = visionText.text.replace('\n', ' ')
        val normalized = singleLine.replace(Regex("""(\d),(\d{3})(?!\d)"""), "$1$2")

        val isLucky          = Regex("""LUCKY\s+POK[EÉ]MON""",    RegexOption.IGNORE_CASE).containsMatchIn(singleLine)
        val isShadowByText   = Regex("""SHADOW\s+POK[EÉ]MON""",   RegexOption.IGNORE_CASE).containsMatchIn(singleLine)
        val isPurifiedByText = Regex("""PURIFIED\s+POK[EÉ]MON""", RegexOption.IGNORE_CASE).containsMatchIn(singleLine)

        val rawDust = extractDust(normalized)
        Log.d(TAG, "rawDust=$rawDust")

        val isPurifiedByDust = !isLucky && !isShadowByText && rawDust != null
            && rawDust !in VALID_DUST && VALID_DUST.contains(rawDust * 10 / 9)
        val isShadowByDust   = !isLucky && !isPurifiedByText && rawDust != null
            && rawDust !in VALID_DUST && rawDust % 6 == 0 && VALID_DUST.contains(rawDust / 6)
        val isPurifiedByIcon = !isLucky && !isShadowByText && PurifiedDetector.detect(bitmap)

        val isShadow   = isShadowByText   || isShadowByDust
        val isPurified = isPurifiedByText || isPurifiedByDust || isPurifiedByIcon

        Log.d(TAG, "lucky=$isLucky shadow=$isShadow purified=$isPurified(byDust=$isPurifiedByDust byIcon=$isPurifiedByIcon)")

        val (pokemonName, nameSource) = extractName(visionText, singleLine, bitmap.height)
        val textAppraisal = extractAppraisal(visionText.text)
        val (detectedBars, isHundo) = AppraisalBarDetector.detect(bitmap, appraisalVisible(singleLine))
        return OCRResult(
            pokemonName   = pokemonName,
            nameSource    = nameSource,
            cp            = extractCP(visionText, singleLine, bitmap.height),
            hp            = extractHP(singleLine),
            dustCost      = rawDust,
            appraisalBars = textAppraisal ?: detectedBars,
            isLucky    = isLucky,
            isShadow   = isShadow,
            isPurified = isPurified,
            isHundo    = isHundo,
        )
    }

    // ── Name ────────────────────────────────────────────────────────────────────

    private fun extractName(visionText: Text, fullText: String, screenHeight: Int): Pair<String, String> {
        val footer = Regex(
            """This ([A-Z][a-z]+(?:[- ][A-Z][a-z]+)?)\s+was\s+caught""",
            RegexOption.IGNORE_CASE,
        ).find(fullText)
        if (footer != null) {
            Log.d(TAG, "Strategy 1 (footer): matched")
            val name = footer.groupValues[1].trim()
                .split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercaseChar() } }
            return Pair(name, "footer")
        }
        Log.d(TAG, "Strategy 1 (footer): no match")

        val mega = Regex("""([A-Za-z][A-Za-z-]{2,})\s+MEGA\s+ENERGY""", RegexOption.IGNORE_CASE).find(fullText)
        if (mega != null) {
            val raw = mega.groupValues[1].trim()
            Log.d(TAG, "Strategy 2 (mega energy): matched '$raw'")
            val name = raw.split("-").joinToString("-") { part ->
                part.replaceFirstChar { it.uppercaseChar() }
            }
            return Pair(name, "mega")
        }
        Log.d(TAG, "Strategy 2 (mega energy): no match")

        val cardTop = screenHeight * 0.35f
        val cardBot = screenHeight * 0.65f
        val cardLines = visionText.textBlocks
            .flatMap { it.lines }
            .filter { line ->
                val box = line.boundingBox ?: return@filter false
                val midY = (box.top + box.bottom) / 2f
                midY in cardTop..cardBot
            }
        Log.d(TAG, "Card zone lines: ${cardLines.map { "'${it.text}' h=${it.boundingBox?.height()}" }}")
        val largest = cardLines
            .filter { line ->
                val t     = line.text.trim()
                val words = t.lowercase().split(Regex("""\s+"""))
                t.length in 2..20 &&
                t.all { it.isLetter() || it.isWhitespace() || it == '-' || it == '\'' } &&
                t.any { it.isLetter() } &&
                t.lowercase() !in NOT_A_NAME &&
                words.none { it in NOT_A_NAME }
            }
            .maxByOrNull { it.boundingBox?.height() ?: 0 }
            ?.text?.trim()
        Log.d(TAG, "Strategy 3 (card zone): result='$largest'")
        if (!largest.isNullOrBlank()) return Pair(largest, "card")

        return Pair("", "")
    }

    // ── CP ──────────────────────────────────────────────────────────────────────

    private fun extractCP(visionText: Text, fallbackText: String, screenHeight: Int): Int? {
        val topZone  = screenHeight * 0.25f
        val topLines = visionText.textBlocks
            .flatMap { it.lines }
            .filter { line ->
                val box = line.boundingBox ?: return@filter false
                (box.top + box.bottom) / 2f < topZone
            }

        fun numsFromLine(line: Text.Line): List<Int> {
            val fromLine  = line.text.replace(Regex("""[^0-9]"""), "").toIntOrNull()
            val fromElems = line.elements
                .joinToString("") { it.text.replace(Regex("""[^0-9]"""), "") }
                .toIntOrNull()
            return listOfNotNull(fromLine, fromElems).filter { it in 10..50000 }
        }

        val cpPrefixed = topLines
            .filter { it.text.uppercase().contains("CP") }
            .flatMap(::numsFromLine)
            .maxOrNull()
        if (cpPrefixed != null) return cpPrefixed

        val cpFromBox = topLines.flatMap(::numsFromLine).maxOrNull()
        if (cpFromBox != null) return cpFromBox

        return Regex("""[CcOo0][PpHh]\s*(\d{1,5})""").find(fallbackText)
            ?.groupValues?.get(1)?.toIntOrNull()
    }

    // ── HP ──────────────────────────────────────────────────────────────────────

    private fun extractHP(text: String): Int? {
        val cleaned = text
            .replace(Regex("""(?<=\d)[Oo](?=\d)"""), "0")
            .replace(Regex("""(?<=\s|/)[Oo](?=\d)"""), "0")
            .replace(Regex("""(\d)[Oo](?=\s*HP)"""), "${"\$1"}0")
        return Regex("""(\d{1,4})\s*/\s*\d{1,4}\s*HP""", RegexOption.IGNORE_CASE)
            .find(cleaned)?.groupValues?.get(1)?.toIntOrNull()
    }

    // ── Dust ────────────────────────────────────────────────────────────────────

    private fun extractDust(normalizedText: String): Int? =
        DISPLAYABLE_DUST.sortedDescending().firstOrNull { dust ->
            Regex("""(?<!\d)$dust(?!\d)""").containsMatchIn(normalizedText)
        }

    // ── Appraisal ───────────────────────────────────────────────────────────────

    private fun appraisalVisible(singleLine: String): Boolean =
        "Attack" in singleLine && "Defense" in singleLine && "HP" in singleLine

    private fun extractAppraisal(rawText: String): Int? {
        val filled = rawText.count { it == '★' }
        val empty  = rawText.count { it == '☆' }
        Log.d(TAG, "Appraisal text: filled=$filled empty=$empty")
        if (filled + empty != 3) return null
        return filled.coerceIn(0, 3)
    }
}
