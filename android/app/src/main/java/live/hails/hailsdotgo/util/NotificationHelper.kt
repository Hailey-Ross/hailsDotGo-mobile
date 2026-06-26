package live.hails.hailsdotgo.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat

const val CHANNEL_SCANNER = "scanner"
const val CHANNEL_RAIDS   = "raids"

fun createNotificationChannels(context: Context) {
    val nm = context.getSystemService(NotificationManager::class.java)
    nm.createNotificationChannel(
        NotificationChannel(CHANNEL_SCANNER, "IV Scanner", NotificationManager.IMPORTANCE_LOW).apply {
            description = "Foreground service for IV scanning"
        }
    )
    nm.createNotificationChannel(
        NotificationChannel(CHANNEL_RAIDS, "Raid Notifications", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Raid match alerts and lobby updates"
        }
    )
}

fun buildScannerNotification(context: Context): Notification =
    NotificationCompat.Builder(context, CHANNEL_SCANNER)
        .setContentTitle("HailsDotGo Scanner")
        .setContentText("Tap the bubble to scan a Pokémon")
        .setSmallIcon(android.R.drawable.ic_menu_camera)
        .setOngoing(true)
        .build()
