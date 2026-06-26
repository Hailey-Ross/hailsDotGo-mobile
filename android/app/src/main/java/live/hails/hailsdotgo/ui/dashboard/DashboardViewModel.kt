package live.hails.hailsdotgo.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import live.hails.hailsdotgo.data.AuthRepository
import live.hails.hailsdotgo.data.PokemonDataRepository
import live.hails.hailsdotgo.data.RaidRepository
import live.hails.hailsdotgo.data.model.RaidOverview
import live.hails.hailsdotgo.data.model.RaidState

class DashboardViewModel : ViewModel() {
    private val _raidState   = MutableStateFlow<RaidState?>(null)
    val raidState = _raidState.asStateFlow()

    private val _overview    = MutableStateFlow<RaidOverview?>(null)
    val overview = _overview.asStateFlow()

    private val _error       = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        viewModelScope.launch { AuthRepository.refreshMe() }
        viewModelScope.launch { PokemonDataRepository.refresh() }
        startPolling()
    }

    private fun startPolling() {
        viewModelScope.launch {
            while (true) {
                refresh()
                delay(30_000)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            RaidRepository.state().onSuccess { _raidState.value = it }
            RaidRepository.overview().onSuccess { _overview.value = it }
                .onFailure { _error.value = it.message }
        }
    }
}
