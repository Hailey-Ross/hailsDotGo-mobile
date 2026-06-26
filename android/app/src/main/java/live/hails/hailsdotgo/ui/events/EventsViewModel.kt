package live.hails.hailsdotgo.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import live.hails.hailsdotgo.data.EventRepository
import live.hails.hailsdotgo.data.model.PogoEvent

class EventsViewModel : ViewModel() {
    private val _events  = MutableStateFlow<List<PogoEvent>>(emptyList())
    val events = _events.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _error   = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            EventRepository.getEvents()
                .onSuccess { _events.value = it }
                .onFailure { _error.value = it.message }
            _loading.value = false
        }
    }

    fun clearError() { _error.value = null }
}
