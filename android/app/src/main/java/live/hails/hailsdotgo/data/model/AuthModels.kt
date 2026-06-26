package live.hails.hailsdotgo.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val username: String,
    val password: String,
)

data class LoginResponse(
    val token: String,
    @SerializedName("expires_at") val expiresAt: String,
    val user: User,
)

data class User(
    val id: Int,
    val username: String,
    val role: String,
    val lang: String,
    @SerializedName("special_rank") val specialRank: String,
    @SerializedName("trainer_level") val trainerLevel: Int = 0,
)

data class PushTokenRequest(
    val platform: String,
    @SerializedName("push_token") val pushToken: String,
    @SerializedName("device_name") val deviceName: String,
)

data class PushTokenDeleteRequest(
    @SerializedName("push_token") val pushToken: String,
)
