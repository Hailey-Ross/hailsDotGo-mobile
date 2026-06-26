package live.hails.hailsdotgo.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import live.hails.hailsdotgo.data.AuthRepository
import live.hails.hailsdotgo.data.TokenStore

@Composable
fun SettingsScreen(onLogout: () -> Unit) {
    val scope = rememberCoroutineScope()
    val username = TokenStore.loadUsername() ?: "—"
    val role     = TokenStore.loadRole() ?: "—"

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        Text("Signed in as", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(username, style = MaterialTheme.typography.bodyLarge)
        Text(role, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(Modifier.height(24.dp))

        Text("HailsDotGo v0.0.1-alpha", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("pogo.hails.live", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(32.dp))
        OutlinedButton(
            onClick  = {
                scope.launch {
                    AuthRepository.logout()
                    onLogout()
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Sign Out")
        }
    }
}
