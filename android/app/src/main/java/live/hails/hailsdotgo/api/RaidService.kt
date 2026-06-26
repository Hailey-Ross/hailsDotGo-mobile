package live.hails.hailsdotgo.api

import live.hails.hailsdotgo.data.model.CreateLobbyRequest
import live.hails.hailsdotgo.data.model.FeedbackRequest
import live.hails.hailsdotgo.data.model.KickRequest
import live.hails.hailsdotgo.data.model.QueueRequest
import live.hails.hailsdotgo.data.model.RaidOverview
import live.hails.hailsdotgo.data.model.RaidState
import live.hails.hailsdotgo.data.model.ReportRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface RaidService {
    @GET("/api/mobile/v1/raid/overview")
    suspend fun overview(): RaidOverview

    @GET("/api/mobile/v1/raid/state")
    suspend fun state(): RaidState

    @POST("/api/mobile/v1/raid/queue")
    suspend fun joinQueue(@Body request: QueueRequest): Response<Unit>

    @DELETE("/api/mobile/v1/raid/queue")
    suspend fun leaveQueue(): Response<Unit>

    @POST("/api/mobile/v1/raid/lobbies")
    suspend fun createLobby(@Body request: CreateLobbyRequest): Response<Unit>

    @DELETE("/api/mobile/v1/raid/lobbies/{id}")
    suspend fun cancelLobby(@Path("id") id: Long): Response<Unit>

    @POST("/api/mobile/v1/raid/lobbies/{id}/confirm")
    suspend fun confirm(@Path("id") id: Long): Response<Unit>

    @POST("/api/mobile/v1/raid/lobbies/{id}/leave")
    suspend fun leave(@Path("id") id: Long): Response<Unit>

    @POST("/api/mobile/v1/raid/lobbies/{id}/kick")
    suspend fun kick(@Path("id") id: Long, @Body request: KickRequest): Response<Unit>

    @POST("/api/mobile/v1/raid/lobbies/{id}/invited")
    suspend fun markInvited(@Path("id") id: Long): Response<Unit>

    @POST("/api/mobile/v1/raid/lobbies/{id}/report")
    suspend fun report(@Path("id") id: Long, @Body request: ReportRequest): Response<Unit>

    @POST("/api/mobile/v1/raid/lobbies/{id}/feedback")
    suspend fun feedback(@Path("id") id: Long, @Body request: FeedbackRequest): Response<Unit>
}
