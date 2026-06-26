package live.hails.hailsdotgo.ui.raid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import live.hails.hailsdotgo.data.RaidRepository
import live.hails.hailsdotgo.data.TokenStore
import live.hails.hailsdotgo.data.model.RaidState

class RaidLobbyViewModel : ViewModel() {
    private val _state   = MutableStateFlow<RaidState?>(null)
    val state = _state.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _error   = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _events  = MutableSharedFlow<LobbyEvent>()
    val events = _events.asSharedFlow()

    val username: String get() = TokenStore.loadUsername() ?: ""

    init { startPolling() }

    private fun startPolling() {
        viewModelScope.launch {
            while (true) {
                refresh()
                delay(10_000)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            RaidRepository.state()
                .onSuccess { _state.value = it }
                .onFailure { _error.value = it.message }
        }
    }

    fun confirm(lobbyId: Long) {
        viewModelScope.launch {
            RaidRepository.confirm(lobbyId)
                .onSuccess { refresh() }
                .onFailure { _error.value = it.message }
        }
    }

    fun leave(lobbyId: Long) {
        viewModelScope.launch {
            RaidRepository.leave(lobbyId)
                .onSuccess { _events.emit(LobbyEvent.LeftLobby) }
                .onFailure { _error.value = it.message }
        }
    }

    fun markInvited(lobbyId: Long) {
        viewModelScope.launch {
            RaidRepository.markInvited(lobbyId)
                .onSuccess { refresh() }
                .onFailure { _error.value = it.message }
        }
    }

    fun cancelLobby(lobbyId: Long) {
        viewModelScope.launch {
            RaidRepository.cancelLobby(lobbyId)
                .onSuccess { _events.emit(LobbyEvent.LobbyCancelled) }
                .onFailure { _error.value = it.message }
        }
    }

    fun reportOutcome(lobbyId: Long, attended: List<Long>, leftEarly: List<Long>) {
        viewModelScope.launch {
            RaidRepository.report(lobbyId, attended, leftEarly)
                .onSuccess { _events.emit(LobbyEvent.Reported) }
                .onFailure { _error.value = it.message }
        }
    }

    fun clearError() { _error.value = null }
}

sealed class LobbyEvent {
    object LeftLobby      : LobbyEvent()
    object LobbyCancelled : LobbyEvent()
    object Reported       : LobbyEvent()
}
