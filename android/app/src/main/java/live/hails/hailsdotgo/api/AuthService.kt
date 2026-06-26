package live.hails.hailsdotgo.api

import live.hails.hailsdotgo.data.model.LoginRequest
import live.hails.hailsdotgo.data.model.LoginResponse
import live.hails.hailsdotgo.data.model.PushTokenDeleteRequest
import live.hails.hailsdotgo.data.model.PushTokenRequest
import live.hails.hailsdotgo.data.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthService {
    @POST("/api/mobile/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @DELETE("/api/mobile/v1/auth/session")
    suspend fun logout(): Response<Unit>

    @GET("/api/mobile/v1/auth/me")
    suspend fun me(): User

    @POST("/api/mobile/v1/push/token")
    suspend fun registerPushToken(@Body request: PushTokenRequest): Response<Unit>

    @DELETE("/api/mobile/v1/push/token")
    suspend fun unregisterPushToken(@Body request: PushTokenDeleteRequest): Response<Unit>
}
