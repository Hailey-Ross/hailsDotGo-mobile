package live.hails.hailsdotgo.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PushService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        // TODO: register token via AuthRepository -> AuthService.registerPushToken
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val lobbyId = message.data["lobby_id"] ?: return
        val type    = message.data["type"] ?: return
        // TODO: build a notification and navigate to RaidLobbyScreen on tap.
        // Supported types: raid_matched, lobby_full, confirm_warning, lobby_cancelled, feedback_requested
    }
}
