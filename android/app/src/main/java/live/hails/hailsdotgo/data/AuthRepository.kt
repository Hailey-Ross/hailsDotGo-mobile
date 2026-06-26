package live.hails.hailsdotgo.data

import live.hails.hailsdotgo.api.ApiClient
import live.hails.hailsdotgo.data.model.LoginRequest
import live.hails.hailsdotgo.data.model.LoginResponse
import live.hails.hailsdotgo.data.model.User

object AuthRepository {
    suspend fun login(username: String, password: String): Result<LoginResponse> = runCatching {
        val resp = ApiClient.auth.login(LoginRequest(username, password))
        ApiClient.setToken(resp.token)
        TokenStore.saveToken(resp.token)
        TokenStore.saveUsername(resp.user.username)
        TokenStore.saveRole(resp.user.role)
        if (resp.user.trainerLevel > 0) TokenStore.saveTrainerLevel(resp.user.trainerLevel)
        resp
    }

    suspend fun logout(): Result<Unit> = runCatching {
        ApiClient.auth.logout()
        ApiClient.setToken(null)
        TokenStore.clear()
    }

    suspend fun me(): Result<User> = runCatching {
        ApiClient.auth.me()
    }

    suspend fun refreshMe(): Result<Unit> = runCatching {
        val user = ApiClient.auth.me()
        if (user.trainerLevel > 0) TokenStore.saveTrainerLevel(user.trainerLevel)
    }

    fun restoreSession() {
        TokenStore.loadToken()?.let { ApiClient.setToken(it) }
    }
}
