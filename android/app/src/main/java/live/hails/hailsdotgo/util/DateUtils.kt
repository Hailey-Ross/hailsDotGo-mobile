package live.hails.hailsdotgo.util

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun parseEventDate(iso: String?): ZonedDateTime? = iso?.let {
    runCatching { ZonedDateTime.parse(it) }.getOrNull()
}

private val displayFmt = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())

fun formatEventDateRange(start: String?, end: String?): String {
    val s = parseEventDate(start)
    val e = parseEventDate(end)
    return when {
        s != null && e != null -> "${s.format(displayFmt)} – ${e.format(displayFmt)}"
        s != null              -> "From ${s.format(displayFmt)}"
        e != null              -> "Until ${e.format(displayFmt)}"
        else                   -> ""
    }
}
