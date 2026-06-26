package live.hails.hailsdotgo.ui.box

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import live.hails.hailsdotgo.data.IVRepository
import live.hails.hailsdotgo.data.model.PokemonBoxEntry

class PokemonBoxViewModel : ViewModel() {
    private val _entries = MutableStateFlow<List<PokemonBoxEntry>>(emptyList())
    val entries = _entries.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _loading.value = true
            IVRepository.listBox()
                .onSuccess { _entries.value = it.pokemon }
                .onFailure { _error.value = it.message }
            _loading.value = false
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            IVRepository.deletePokemon(id)
                .onSuccess { _entries.value = _entries.value.filter { it.id != id } }
                .onFailure { _error.value = it.message }
        }
    }
}
