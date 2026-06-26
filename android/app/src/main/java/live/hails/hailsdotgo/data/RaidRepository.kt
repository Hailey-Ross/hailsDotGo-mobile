package live.hails.hailsdotgo.data

import live.hails.hailsdotgo.api.ApiClient
import live.hails.hailsdotgo.data.model.CreateLobbyRequest
import live.hails.hailsdotgo.data.model.FeedbackRequest
import live.hails.hailsdotgo.data.model.KickRequest
import live.hails.hailsdotgo.data.model.QueueRequest
import live.hails.hailsdotgo.data.model.RaidOverview
import live.hails.hailsdotgo.data.model.RaidState
import live.hails.hailsdotgo.data.model.ReportRequest

object RaidRepository {
    suspend fun overview(): Result<RaidOverview>  = runCatching { ApiClient.raid.overview() }
    suspend fun state(): Result<RaidState>         = runCatching { ApiClient.raid.state() }

    suspend fun joinQueue(bossName: String, tier: Int): Result<Unit> = runCatching {
        ApiClient.raid.joinQueue(QueueRequest(bossName, tier)); Unit
    }
    suspend fun leaveQueue(): Result<Unit>        = runCatching { ApiClient.raid.leaveQueue(); Unit }

    suspend fun createLobby(req: CreateLobbyRequest): Result<Unit> = runCatching {
        ApiClient.raid.createLobby(req); Unit
    }
    suspend fun cancelLobby(id: Long): Result<Unit>  = runCatching { ApiClient.raid.cancelLobby(id); Unit }
    suspend fun confirm(id: Long): Result<Unit>       = runCatching { ApiClient.raid.confirm(id); Unit }
    suspend fun leave(id: Long): Result<Unit>         = runCatching { ApiClient.raid.leave(id); Unit }
    suspend fun kick(id: Long, userId: Long): Result<Unit> = runCatching {
        ApiClient.raid.kick(id, KickRequest(userId)); Unit
    }
    suspend fun markInvited(id: Long): Result<Unit>  = runCatching { ApiClient.raid.markInvited(id); Unit }
    suspend fun report(id: Long, attended: List<Long>, leftEarly: List<Long>): Result<Unit> = runCatching {
        ApiClient.raid.report(id, ReportRequest(attended, leftEarly)); Unit
    }
    suspend fun feedback(id: Long, optionId: Int): Result<Unit> = runCatching {
        ApiClient.raid.feedback(id, FeedbackRequest(optionId)); Unit
    }
}
