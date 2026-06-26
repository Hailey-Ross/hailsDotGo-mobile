package live.hails.hailsdotgo.ui.box

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import live.hails.hailsdotgo.data.model.PokemonBoxEntry
import live.hails.hailsdotgo.ui.theme.IVGold
import live.hails.hailsdotgo.ui.theme.IVGreen
import live.hails.hailsdotgo.ui.theme.IVTeal

@Composable
fun PokemonBoxScreen(vm: PokemonBoxViewModel = viewModel()) {
    val entries by vm.entries.collectAsState()
    val loading by vm.loading.collectAsState()
    val error   by vm.error.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Pokémon Box",
            style    = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp),
        )

        if (error != null) {
            Text(error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp))
        }

        if (loading && entries.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (entries.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "No saved Pokémon yet.\nScan one and tap Save to add it here.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
                items(entries, key = { it.id }) { entry ->
                    PokemonBoxCard(entry = entry, onDelete = { vm.delete(entry.id) })
                }
            }
        }
    }
}

@Composable
private fun PokemonBoxCard(entry: PokemonBoxEntry, onDelete: () -> Unit) {
    val ivColor: Color = when {
        (entry.ivPct ?: 0.0) >= 100.0 -> IVGold
        (entry.ivPct ?: 0.0) >= 90.0  -> IVGreen
        (entry.ivPct ?: 0.0) >= 80.0  -> IVTeal
        else                           -> Color.Unspecified
    }

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier            = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment   = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        entry.pokemonName.replaceFirstChar { it.uppercase() },
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text("CP ${entry.cp}", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    "${entry.atkIv}/${entry.defIv}/${entry.staIv}  L${entry.level}",
                    style = MaterialTheme.typography.bodySmall,
                )
                if (entry.note.isNotBlank()) {
                    Text(entry.note, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                if (entry.ivPct != null) {
                    Text(
                        "${"%.1f".format(entry.ivPct)}%",
                        color      = ivColor,
                        fontWeight = FontWeight.Bold,
                        style      = MaterialTheme.typography.bodyMedium,
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
