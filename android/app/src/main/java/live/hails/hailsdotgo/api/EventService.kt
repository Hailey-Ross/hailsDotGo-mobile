package live.hails.hailsdotgo.api

import live.hails.hailsdotgo.data.model.PogoEvent
import retrofit2.http.GET

interface EventService {
    @GET("/api/mobile/v1/events")
    suspend fun getEvents(): List<PogoEvent>
}
