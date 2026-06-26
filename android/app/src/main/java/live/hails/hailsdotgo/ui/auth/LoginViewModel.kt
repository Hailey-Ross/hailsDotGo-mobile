package live.hails.hailsdotgo.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import live.hails.hailsdotgo.data.AuthRepository

class LoginViewModel : ViewModel() {
    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>()
    val events = _events.asSharedFlow()

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _error.value = "Username and password are required."
            return
        }
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            AuthRepository.login(username, password)
                .onSuccess { _events.emit(LoginEvent.Success) }
                .onFailure { _error.value = it.message ?: "Login failed" }
            _loading.value = false
        }
    }

    fun clearError() { _error.value = null }
}

sealed class LoginEvent {
    object Success : LoginEvent()
}
