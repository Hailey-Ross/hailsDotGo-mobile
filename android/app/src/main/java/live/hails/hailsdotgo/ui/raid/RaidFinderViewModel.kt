package live.hails.hailsdotgo.ui.raid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import live.hails.hailsdotgo.data.RaidRepository
import live.hails.hailsdotgo.data.model.CreateLobbyRequest
import live.hails.hailsdotgo.data.model.RaidOverview

class RaidFinderViewModel : ViewModel() {
    private val _overview = MutableStateFlow<RaidOverview?>(null)
    val overview = _overview.asStateFlow()

    private val _loading  = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _error    = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _actionSuccess = MutableStateFlow<String?>(null)
    val actionSuccess = _actionSuccess.asStateFlow()

    init { startPolling() }

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
            _loading.value = true
            RaidRepository.overview()
                .onSuccess { _overview.value = it }
                .onFailure { _error.value = it.message }
            _loading.value = false
        }
    }

    fun joinQueue(bossName: String, tier: Int) {
        viewModelScope.launch {
            RaidRepository.joinQueue(bossName, tier)
                .onSuccess { _actionSuccess.value = "Joined queue for $bossName!" }
                .onFailure { _error.value = it.message }
        }
    }

    fun createLobby(bossName: String, tier: Int) {
        viewModelScope.launch {
            RaidRepository.createLobby(CreateLobbyRequest(bossName, tier))
                .onSuccess { _actionSuccess.value = "Lobby created for $bossName!" }
                .onFailure { _error.value = it.message }
        }
    }

    fun clearError()  { _error.value = null }
    fun clearAction() { _actionSuccess.value = null }
}
