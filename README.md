# hailsDotGo-mobile

An Android companion app for Pokemon GO. Scans Pokemon stats using on-device OCR and coordinates raid lobbies through a shared backend.

**Version:** 0.0.1-<img src="alpha.svg" height="18" style="vertical-align:middle"/>  
**Platform:** Android only, minSdk 26 (Android 8.0 Oreo)

---

## What it does

**IV Scanner.** Tap the floating bubble while viewing any Pokemon in Pokemon GO. The app captures one screen frame, runs text and pixel recognition on-device using Google ML Kit, and sends the extracted stats to the backend IV calculator. Results appear in an overlay card without leaving Pokemon GO.

**Raid Finder.** Browse active raid bosses, join the matchmaking queue, or host a lobby. Lobby state refreshes every ten seconds.

**Events.** Lists active and upcoming Pokemon GO events sourced from the backend.

**Pokemon Box.** Stores confirmed IV results locally, linked to your account.

---

## Technical overview

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| OCR | Google ML Kit Text Recognition (on-device) |
| Pixel detectors | Custom bitmap analysis for CP arc, appraisal bars, and purified icon |
| Screen capture | MediaProjection API, single frame, bitmap dropped immediately after processing |
| Overlay | SYSTEM_ALERT_WINDOW floating bubble |
| Networking | Retrofit 2, OkHttp 4 |
| Auth token storage | EncryptedSharedPreferences (AES-256-GCM via Android Keystore) |
| Push notifications | Firebase Cloud Messaging (FCM) |

See [Architecture](https://github.com/Hailey-Ross/hailsDotGo-mobile/wiki/Architecture) for a package-by-package breakdown and full data flow.

---

## Prerequisites

- Android Studio Jellyfish (2023.3.1) or newer
- Android SDK platform 35 (device or emulator running SDK 26 or higher)
- A Firebase project with Cloud Messaging enabled
- An account on `pogo.hails.live` (or access to a self-hosted backend instance)

---

## Setup

### 1. Clone

```
git clone https://github.com/hails-cc/hailsDotGo-mobile.git
cd hailsDotGo-mobile
```

### 2. Set up .gitignore

The `.gitignore` file is not committed to this repository. Copy the example:

```
cp .gitignore.example .gitignore
```

### 3. Configure the Android SDK path

```
cp android/local.properties.example android/local.properties
```

Edit `android/local.properties` and set `sdk.dir` to your local Android SDK path.

### 4. Configure Firebase

```
cp android/app/google-services.json.example android/app/google-services.json
```

Replace the placeholder values with your real Firebase project credentials. See [Firebase Setup](https://github.com/Hailey-Ross/hailsDotGo-mobile/wiki/Firebase-Setup) for a step-by-step walkthrough.

Once your real `google-services.json` is in place, un-comment the `google-services` plugin in both Gradle files.

In `android/build.gradle.kts`:
```kotlin
id("com.google.gms.google-services") version "4.4.2" apply false
```

In `android/app/build.gradle.kts`:
```kotlin
id("com.google.gms.google-services")
```

### 5. Open and build

Open the `android/` subdirectory in Android Studio (not the repository root). Sync Gradle, then build and run on a physical device or emulator running Android 8.0 or higher.

A physical device is recommended for the overlay and MediaProjection features.

See [Build Guide](https://github.com/Hailey-Ross/hailsDotGo-mobile/wiki/Build-Guide) for a complete walkthrough including release builds and ProGuard configuration.

---

## Permissions

| Permission | Why it is required |
|---|---|
| `INTERNET` | Backend API calls |
| `FOREGROUND_SERVICE` | Keeps the screen capture service alive while scanning |
| `FOREGROUND_SERVICE_MEDIA_PROJECTION` | Required by Android 10+ when using MediaProjection in a foreground service |
| `SYSTEM_ALERT_WINDOW` | The floating bubble that overlays Pokemon GO |
| `POST_NOTIFICATIONS` | Raid match alerts on Android 13 and higher |

---

## Documentation

| Page | Contents |
|---|---|
| [Architecture](https://github.com/Hailey-Ross/hailsDotGo-mobile/wiki/Architecture) | Package structure and data flow |
| [OCR System](https://github.com/Hailey-Ross/hailsDotGo-mobile/wiki/OCR-System) | Screen capture pipeline, ML Kit OCR, and pixel detectors |
| [API Reference](https://github.com/Hailey-Ross/hailsDotGo-mobile/wiki/API-Reference) | All backend endpoints with request and response shapes |
| [Firebase Setup](https://github.com/Hailey-Ross/hailsDotGo-mobile/wiki/Firebase-Setup) | Firebase project setup and FCM configuration |
| [Build Guide](https://github.com/Hailey-Ross/hailsDotGo-mobile/wiki/Build-Guide) | Debug and release build instructions |
| [Push Notifications](https://github.com/Hailey-Ross/hailsDotGo-mobile/wiki/Push-Notifications) | FCM implementation notes and stub completion guide |

---

## Contributing

Pull requests are welcome. Please open an issue before starting large changes.

To point the app at a different backend, change `BASE_URL` in `android/app/src/main/java/live/hails/hailsdotgo/api/ApiClient.kt`.

This project is not affiliated with Niantic or The Pokemon Company.
