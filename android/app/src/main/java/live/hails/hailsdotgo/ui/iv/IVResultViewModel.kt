package live.hails.hailsdotgo.ui.iv

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import live.hails.hailsdotgo.capture.CaptureState
import live.hails.hailsdotgo.data.IVRepository
import live.hails.hailsdotgo.data.TokenStore
import live.hails.hailsdotgo.data.model.IVRequest
import live.hails.hailsdotgo.data.model.IVResponse

data class IVFormState(
    val pokemonName  : String  = "",
    val cp           : String  = "",
    val hp           : String  = "",
    val dustCost     : String  = "",
    val trainerLevel : String  = "40",
    val topStat      : String  = "",      // "" = any, "atk", "def", "sta"
    val appraisalBars: Int?    = null,    // null = skip filter
    val isLucky      : Boolean = false,
    val isShadow     : Boolean = false,
    val isPurified   : Boolean = false,
    val isHundo      : Boolean = false,
)

sealed class IVResultState {
    object Idle    : IVResultState()
    object Loading : IVResultState()
    data class Success(val response: IVResponse) : IVResultState()
    data class Error(val message: String) : IVResultState()
}

val DUST_COSTS = listOf(200, 400, 800, 1000, 1300, 1600, 1900, 2200, 2500,
    3000, 3500, 4000, 4500, 5000, 6000, 7000, 8000, 9000, 10000, 12000, 15000, 20000)

class IVResultViewModel : ViewModel() {
    private val _form   = MutableStateFlow(IVFormState())
    val form = _form.asStateFlow()

    private val _result = MutableStateFlow<IVResultState>(IVResultState.Idle)
    val result = _result.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess = _saveSuccess.asStateFlow()

    init { loadOCRIfAvailable() }

    private fun loadOCRIfAvailable() {
        val ocr = CaptureState.accumulatedOCR
        val hundo = ocr?.isHundo ?: false
        _form.value = IVFormState(
            trainerLevel  = TokenStore.loadTrainerLevel().toString(),
            pokemonName   = ocr?.pokemonName          ?: "",
            cp            = ocr?.cp?.toString()       ?: "",
            hp            = ocr?.hp?.toString()       ?: "",
            dustCost      = ocr?.dustCost?.toString() ?: "",
            appraisalBars = if (hundo) 3 else ocr?.appraisalBars,
            isLucky       = ocr?.isLucky              ?: false,
            isShadow      = ocr?.isShadow             ?: false,
            isPurified    = ocr?.isPurified            ?: false,
            isHundo       = hundo,
        )
    }

    fun updateForm(update: IVFormState.() -> IVFormState) { _form.value = _form.value.update() }

    fun calculate() {
        val f = _form.value
        val cp    = f.cp.toIntOrNull() ?: return setError(
            if (f.cp.isBlank()) "CP is missing. Scan again when the glow has cleared."
            else "CP must be a number"
        )
        val hp    = f.hp.toIntOrNull() ?: return setError("HP must be a number")
        val dustRaw = f.dustCost.toIntOrNull() ?: return setError("Dust cost must be a number")
        // Normalize displayed dust to standard cost the server uses for level mapping.
        // Lucky shows ½ standard; Purified shows 90% of standard; Shadow shows 6× standard.
        val dust = when {
            f.isLucky    -> dustRaw * 2
            f.isPurified -> dustRaw * 10 / 9
            f.isShadow   -> dustRaw / 6
            else         -> dustRaw
        }
        val level = f.trainerLevel.toIntOrNull() ?: return setError("Trainer level must be a number")
        if (f.pokemonName.isBlank()) return setError("Pokémon name is required")

        viewModelScope.launch {
            _result.value = IVResultState.Loading
            IVRepository.calculate(
                IVRequest(
                    pokemonName   = f.pokemonName.trim().lowercase(),
                    cp            = cp,
                    hp            = hp,
                    dustCost      = dust,
                    trainerLevel  = level,
                    topStat       = f.topStat.ifBlank { null },
                    appraisalBars = f.appraisalBars,
                )
            ).onSuccess { _result.value = IVResultState.Success(it) }
             .onFailure { _result.value = IVResultState.Error(it.message ?: "Calculation failed") }
        }
    }

    private fun setError(msg: String) { _result.value = IVResultState.Error(msg) }

    fun clearResult() { _result.value = IVResultState.Idle }
}
