package live.hails.hailsdotgo.ui.events

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import live.hails.hailsdotgo.data.model.PogoEvent
import live.hails.hailsdotgo.util.formatEventDateRange
import live.hails.hailsdotgo.util.parseEventDate
import java.time.ZonedDateTime

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EventsScreen(vm: EventsViewModel = viewModel()) {
    val events  by vm.events.collectAsState()
    val loading by vm.loading.collectAsState()
    val error   by vm.error.collectAsState()

    val now      = remember { ZonedDateTime.now() }
    val active   = events.filter { e ->
        val s = parseEventDate(e.start); val end = parseEventDate(e.end)
        s != null && end != null && s.isBefore(now) && end.isAfter(now)
    }
    val upcoming = events.filter { e ->
        val s = parseEventDate(e.start)
        s != null && s.isAfter(now)
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Events", style = MaterialTheme.typography.headlineMedium)
            if (loading) CircularProgressIndicator(Modifier.size(24.dp))
        }

        if (error != null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                Button(onClick = { vm.clearError(); vm.load() }) { Text("Retry") }
            }
            return
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (active.isNotEmpty()) {
                stickyHeader { SectionHeader("Active Now") }
                items(active) { EventCard(it) }
            }
            if (upcoming.isNotEmpty()) {
                stickyHeader { SectionHeader("Upcoming") }
                items(upcoming) { EventCard(it) }
            }
            if (!loading && active.isEmpty() && upcoming.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 64.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "No events right now.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        )
    }
}

@Composable
private fun EventCard(event: PogoEvent) {
    Card(Modifier.fillMaxWidth()) {
        Column {
            if (!event.image.isNullOrBlank()) {
                AsyncImage(
                    model = event.image,
                    contentDescription = event.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentScale = ContentScale.Crop,
                )
            }
            Column(Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = event.name,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f),
                    )
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                event.eventType.toEventTypeLabel(),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                    )
                }
                val dateRange = formatEventDateRange(event.start, event.end)
                if (dateRange.isNotEmpty()) {
                    Text(
                        text = dateRange,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (!event.heading.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(event.heading, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

private fun String.toEventTypeLabel(): String =
    split("-").joinToString(" ") { it.replaceFirstChar { c -> c.uppercaseChar() } }
