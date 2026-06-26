package live.hails.hailsdotgo.capture

import android.content.Intent
import live.hails.hailsdotgo.ocr.OCRResult

object CaptureState {
    var projectionResultCode: Int = 0
    var projectionData: Intent? = null

    fun hasProjection(): Boolean = projectionData != null

    // ── OCR accumulation ────────────────────────────────────────────────────────
    // Merges successive scans of the same Pokémon so the ViewModel always reads
    // the best combined result. Confidence-aware: higher-priority name sources
    // (footer > mega > card) won't be overwritten by weaker reads; appraisal and
    // CP always keep the highest value seen.

    var accumulatedOCR: OCRResult? = null
        private set

    private var nameSourcePriority: Int = 0

    fun setOCRResult(new: OCRResult) {
        val prev = accumulatedOCR
        if (prev == null) {
            accumulatedOCR = new
            nameSourcePriority = sourcePriority(new.nameSource)
            return
        }

        val nameChanged = new.pokemonName.isNotBlank() && prev.pokemonName.isNotBlank() &&
            !new.pokemonName.equals(prev.pokemonName, ignoreCase = true)
        if (nameChanged) {
            accumulatedOCR = new
            nameSourcePriority = sourcePriority(new.nameSource)
            return
        }

        val newPriority = sourcePriority(new.nameSource)
        val acceptName = new.pokemonName.isNotBlank() && newPriority >= nameSourcePriority
        if (acceptName) nameSourcePriority = newPriority

        accumulatedOCR = OCRResult(
            pokemonName   = if (acceptName) new.pokemonName else prev.pokemonName,
            nameSource    = if (acceptName) new.nameSource  else prev.nameSource,
            cp            = listOfNotNull(new.cp, prev.cp).maxOrNull(),
            hp            = new.hp       ?: prev.hp,
            dustCost      = new.dustCost ?: prev.dustCost,
            appraisalBars = listOfNotNull(new.appraisalBars, prev.appraisalBars).maxOrNull(),
            isLucky       = new.isLucky    || prev.isLucky,
            isShadow      = new.isShadow   || prev.isShadow,
            isPurified    = new.isPurified || prev.isPurified,
            isHundo       = new.isHundo    || prev.isHundo,
        )
    }

    fun clearOCR() {
        accumulatedOCR = null
        nameSourcePriority = 0
    }

    private fun sourcePriority(source: String) = when (source) {
        "footer" -> 3
        "mega"   -> 2
        "card"   -> 1
        else     -> 0
    }
}
