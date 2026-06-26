package live.hails.hailsdotgo.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import live.hails.hailsdotgo.data.TokenStore

@Composable
fun DashboardScreen(
    onStartScanner: () -> Unit,
    vm: DashboardViewModel = viewModel(),
) {
    val raidState by vm.raidState.collectAsState()
    val overview  by vm.overview.collectAsState()
    val username  = TokenStore.loadUsername() ?: "Trainer"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Welcome, $username", style = MaterialTheme.typography.headlineMedium)

        // IV Scanner card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("IV Scanner", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Open Pokémon GO, navigate to any Pokémon, then tap the scanner bubble.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                Button(onClick = onStartScanner, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null)
                    Text("  Start Scanner", modifier = Modifier.padding(start = 4.dp))
                }
            }
        }

        // Raid status card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Raid Status", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                val state = raidState?.state ?: "—"
                val boss  = raidState?.boss
                Text(
                    text  = if (boss != null) "$state — $boss" else state,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        // Active bosses card
        if (overview != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Active Bosses", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    overview!!.bosses.take(5).forEach { boss ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(boss.name, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "T${boss.tier} · ${boss.queueDepth} queued",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    if (overview!!.bosses.isEmpty()) {
                        Text(
                            "No active bosses right now.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
