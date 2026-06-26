package live.hails.hailsdotgo.data.model

import com.google.gson.annotations.SerializedName

data class RaidBoss(
    val name: String,
    val tier: Int,
    @SerializedName("queue_depth") val queueDepth: Int = 0,
)

data class LobbyPreview(
    val id: Long,
    val boss: String,
    val tier: Int,
    val host: String,
    @SerializedName("member_count") val memberCount: Int,
    @SerializedName("max_members") val maxMembers: Int,
    val state: String,
)

data class RaidOverview(
    val bosses: List<RaidBoss>,
    val lobbies: List<LobbyPreview>,
)

data class LobbyMember(
    val id: Long,
    val username: String,
    val state: String,
)

data class RaidState(
    val state: String, // idle | queued | matched | confirmed | raiding | reported
    @SerializedName("lobby_id") val lobbyId: Long? = null,
    val boss: String? = null,
    @SerializedName("confirm_deadline") val confirmDeadline: String? = null,
    val members: List<LobbyMember>? = null,
    val role: String? = null, // "host" | "member"
)

data class QueueRequest(
    @SerializedName("boss_name") val bossName: String,
    @SerializedName("boss_tier") val bossTier: Int,
)

data class CreateLobbyRequest(
    @SerializedName("boss_name") val bossName: String,
    @SerializedName("boss_tier") val bossTier: Int,
    val note: String = "",
    @SerializedName("max_members") val maxMembers: Int = 5,
    @SerializedName("weather_boosted") val weatherBoosted: Boolean = false,
)

data class KickRequest(
    @SerializedName("user_id") val userId: Long,
)

data class ReportRequest(
    val attended: List<Long>,
    @SerializedName("left_early") val leftEarly: List<Long>,
)

data class FeedbackRequest(
    @SerializedName("option_id") val optionId: Int,
)
