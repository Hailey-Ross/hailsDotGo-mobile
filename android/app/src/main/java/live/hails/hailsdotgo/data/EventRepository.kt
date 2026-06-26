package live.hails.hailsdotgo.data

import live.hails.hailsdotgo.api.ApiClient
import live.hails.hailsdotgo.data.model.PogoEvent

object EventRepository {
    suspend fun getEvents(): Result<List<PogoEvent>> = runCatching { ApiClient.events.getEvents() }
}
