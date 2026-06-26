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

@Composable
fun RaidLobbyScreen(
    lobbyId: String,
    onBack: () -> Unit,
    vm: RaidLobbyViewModel = viewModel(),
) {
    val raidState by vm.state.collectAsState()
    val error     by vm.error.collectAsState()

    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                is LobbyEvent.LeftLobby, is LobbyEvent.LobbyCancelled -> onBack()
                is LobbyEvent.Reported -> onBack()
            }
        }
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
            Text("Raid Lobby", style = MaterialTheme.typography.headlineMedium)
            Text("#$lobbyId", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        if (error != null) {
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }

        raidState?.let { state ->
            // State banner
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("State:", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(state.state.uppercase(), style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary)
                    }
                    if (state.boss != null) {
                        Text(state.boss, style = MaterialTheme.typography.headlineSmall)
                    }
                    if (state.confirmDeadline != null) {
                        Text("Confirm by: ${state.confirmDeadline}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Members list
            val members = state.members ?: emptyList()
            if (members.isNotEmpty()) {
                HorizontalDivider()
                Text("Members (${members.size})", style = MaterialTheme.typography.titleSmall)
                members.forEach { member ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(member.username)
                        Text(
                            member.state,
                            color = when (member.state) {
                                "confirmed" -> MaterialTheme.colorScheme.primary
                                "left", "removed" -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            // Action buttons
            HorizontalDivider()
            val lobbyLong = state.lobbyId ?: lobbyId.toLongOrNull() ?: 0L
            val isHost    = state.role == "host"

            when (state.state) {
                "matched" -> {
                    Button(onClick = { vm.confirm(lobbyLong) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Confirm Attendance")
                    }
                }
                "confirmed", "raiding" -> {
                    if (isHost) {
                        Button(onClick = { vm.markInvited(lobbyLong) }, modifier = Modifier.fillMaxWidth()) {
                            Text("Mark All Invited")
                        }
                        val attendedIds = members.filter { it.state != "left" && it.state != "removed" }.map { it.id }
                        OutlinedButton(
                            onClick  = { vm.reportOutcome(lobbyLong, attendedIds, emptyList()) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Report Outcome")
                        }
                    }
                    OutlinedButton(onClick = { vm.leave(lobbyLong) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Leave Lobby")
                    }
                }
                else -> {
                    if (isHost) {
                        OutlinedButton(
                            onClick  = { vm.cancelLobby(lobbyLong) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Cancel Lobby")
                        }
                    } else {
                        OutlinedButton(
                            onClick  = { vm.leave(lobbyLong) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Leave Lobby")
                        }
                    }
                }
            }
        } ?: run {
            Text("Loading lobby state…",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(Modifier.height(8.dp))
    }
}
