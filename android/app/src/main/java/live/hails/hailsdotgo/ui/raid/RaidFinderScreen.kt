package live.hails.hailsdotgo.ui.raid

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import live.hails.hailsdotgo.data.model.LobbyPreview
import live.hails.hailsdotgo.data.model.RaidBoss

@Composable
fun RaidFinderScreen(
    onLobbySelected: (String) -> Unit,
    vm: RaidFinderViewModel = viewModel(),
) {
    val overview      by vm.overview.collectAsState()
    val loading       by vm.loading.collectAsState()
    val error         by vm.error.collectAsState()
    val actionSuccess by vm.actionSuccess.collectAsState()

    LaunchedEffect(actionSuccess) {
        if (actionSuccess != null) vm.clearAction()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text("Raid Finder", style = MaterialTheme.typography.headlineMedium)
            if (loading) CircularProgressIndicator(modifier = Modifier.padding(end = 4.dp))
        }

        if (actionSuccess != null) {
            Text(actionSuccess!!, color = MaterialTheme.colorScheme.primary)
        }
        if (error != null) {
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }

        // Active bosses
        val bosses = overview?.bosses ?: emptyList()
        if (bosses.isNotEmpty()) {
            Text("Active Bosses", style = MaterialTheme.typography.titleMedium)
            bosses.forEach { boss ->
                BossCard(boss, onQueue = { vm.joinQueue(boss.name, boss.tier) },
                    onCreateLobby = { vm.createLobby(boss.name, boss.tier) })
            }
        }

        // Open lobbies
        val lobbies = overview?.lobbies ?: emptyList()
        if (lobbies.isNotEmpty()) {
            HorizontalDivider()
            Text("Open Lobbies", style = MaterialTheme.typography.titleMedium)
            lobbies.forEach { lobby ->
                LobbyCard(lobby, onJoin = { onLobbySelected(lobby.id.toString()) })
            }
        }

        if (!loading && overview == null) {
            Text(
                "No active raids right now.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun BossCard(boss: RaidBoss, onQueue: () -> Unit, onCreateLobby: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(boss.name, style = MaterialTheme.typography.titleSmall)
                    Text("Tier ${boss.tier} · ${boss.queueDepth} in queue",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onQueue) { Text("Queue") }
                    Button(onClick = onCreateLobby) { Text("Host") }
                }
            }
        }
    }
}

@Composable
private fun LobbyCard(lobby: LobbyPreview, onJoin: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(lobby.boss, style = MaterialTheme.typography.titleSmall)
                Text("T${lobby.tier} · ${lobby.memberCount}/${lobby.maxMembers} · by ${lobby.host}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = onJoin) { Text("Join") }
        }
    }
}
