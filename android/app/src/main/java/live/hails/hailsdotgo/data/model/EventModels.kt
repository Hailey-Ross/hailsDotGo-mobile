package live.hails.hailsdotgo.data.model

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class PogoEvent(
    val name: String,
    @SerializedName("eventType") val eventType: String,
    val heading: String?,
    val link: String?,
    val image: String?,
    val start: String?,
    val end: String?,
    val extraData: JsonObject?,
)
