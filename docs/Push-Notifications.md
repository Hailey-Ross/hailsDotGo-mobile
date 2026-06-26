# Push Notifications

## Current state

`PushService.kt` extends `FirebaseMessagingService` and overrides both required methods. Both are currently stubs. The backend endpoints and all required model classes already exist.

---

## Completing onNewToken

`onNewToken(token: String)` is called by FCM whenever a new registration token is assigned to the device (on first install, on token refresh, or after token revocation).

The implementation should call `AuthService.registerPushToken()` with the following payload (model: `PushTokenRequest` in `AuthModels.kt`):

```kotlin
PushTokenRequest(
    platform = "android",
    push_token = token,
    device_name = android.os.Build.MODEL
)
```

This call should be made on a background coroutine and should be guarded: only attempt registration if the user is authenticated (`ApiClient.hasToken()` returns `true`). If the user is not yet logged in, the token should be registered as part of the login flow once authentication completes.

The backend endpoint is `POST /api/mobile/v1/push/token`. See [API-Reference.md](API-Reference.md).

---

## Completing onMessageReceived

`onMessageReceived(message: RemoteMessage)` is called when a data message arrives while the app is in the foreground (background messages are handled by the FCM SDK automatically).

The two keys already extracted from `message.data` are:

| Key | Description |
|---|---|
| `lobby_id` | The raid lobby this notification concerns |
| `type` | One of: `raid_matched`, `lobby_full`, `confirm_warning`, `lobby_cancelled`, `feedback_requested` |

The implementation should:

1. Build a `NotificationCompat.Builder` using the `CHANNEL_RAIDS` channel (defined in `NotificationHelper.kt`).
2. Set the content intent to a `PendingIntent` that opens `MainActivity` with the deep link `hailsdotgo://raid/lobby/<lobbyId>`.
3. Call `NotificationManagerCompat.from(context).notify(...)`.

The `CHANNEL_RAIDS` notification channel is created at importance HIGH in `HailsDotGoApp.onCreate()` via `createNotificationChannels()`.

---

## Token cleanup on logout

`AuthService.unregisterPushToken()` exists and accepts a `PushTokenRequest` with the token to remove. It maps to `DELETE /api/mobile/v1/push/token`.

This call should be made from `AuthRepository.logout()` before the local token is cleared, so the Bearer token is still available to authenticate the request.

---

## Notification channels

Both channels are created in `HailsDotGoApp.onCreate()`:

| Channel ID | Importance | Used for |
|---|---|---|
| `CHANNEL_SCANNER` | LOW | Persistent foreground service notification shown while screen capture is active |
| `CHANNEL_RAIDS` | HIGH | Raid match alerts, confirm warnings, and lobby updates |

Constants for both channel IDs are defined in `NotificationHelper.kt`.
