# Firebase Setup

Firebase is required only for push notifications. All other app features (IV scanning, raid finder, events, Pokemon box) work without a Firebase project.

---

## Step 1: Create a Firebase project

Go to [console.firebase.google.com](https://console.firebase.google.com) and create a new project, or use an existing one.

---

## Step 2: Add the Android app

Inside your Firebase project, click "Add app" and select Android.

Set the Android package name to:

```
live.hails.hailsdotgo
```

Complete the registration wizard.

---

## Step 3: Download google-services.json

Download the `google-services.json` file from the Firebase Console and place it at:

```
android/app/google-services.json
```

This path is gitignored. Do not commit the real file.

---

## Step 4: Enable Cloud Messaging

In the Firebase Console, go to "Cloud Messaging" under the Build section. Ensure the Firebase Cloud Messaging API (V1) is enabled.

---

## Step 5: Un-comment the Gradle plugin

The `google-services` Gradle plugin is commented out by default so the project builds without a real `google-services.json`. Once your real file is in place, un-comment both occurrences.

In `android/build.gradle.kts`:
```kotlin
// Before:
// id("com.google.gms.google-services") version "4.4.2" apply false

// After:
id("com.google.gms.google-services") version "4.4.2" apply false
```

In `android/app/build.gradle.kts`:
```kotlin
// Before:
// id("com.google.gms.google-services")

// After:
id("com.google.gms.google-services")
```

---

## Step 6: Sync Gradle

In Android Studio, click "Sync Now" or run:

```
./gradlew dependencies
```

---

## Step 7: Wire up the push token

`PushService.onNewToken()` is currently a stub. See [Push-Notifications.md](Push-Notifications.md) for the implementation guide. The backend endpoints and request models are already defined in `AuthService.kt` and `AuthModels.kt`.

---

## Notes

- The app compiles and all features run without a real `google-services.json` as long as the Gradle plugin remains commented out.
- If you add a real `google-services.json` without un-commenting the plugin, the build will succeed but FCM will not initialise at runtime.
- The app does not yet register the FCM token with the backend automatically. That wiring is part of the push notification stub completion described in [Push-Notifications.md](Push-Notifications.md).
