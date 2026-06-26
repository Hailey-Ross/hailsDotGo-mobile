# Build Guide

## Requirements

- Android Studio Jellyfish (2023.3.1) or newer
- JDK 11 (bundled with Android Studio)
- Android SDK with platform 35 installed (via SDK Manager)
- A device or emulator running Android 8.0 (API 26) or higher

---

## First-time setup

Follow the setup steps in the root [README](../README.md):

1. Copy `.gitignore.example` to `.gitignore`
2. Copy `android/local.properties.example` to `android/local.properties` and set `sdk.dir`
3. Copy `android/app/google-services.json.example` to `android/app/google-services.json`

---

## Opening the project

Open the `android/` subdirectory in Android Studio, not the repository root. The root directory contains documentation only and is not a Gradle project.

After opening, Android Studio will prompt you to sync Gradle. Click "Sync Now".

---

## Debug build

Run the app directly from Android Studio using the green Run button, or from the command line:

```
cd android
./gradlew assembleDebug
```

The debug APK is output to `android/app/build/outputs/apk/debug/app-debug.apk`.

**Recommendation:** Use a physical device for testing. The overlay (`SYSTEM_ALERT_WINDOW`) and MediaProjection screen capture features do not behave the same way on emulators.

---

## Granting SYSTEM_ALERT_WINDOW

`SYSTEM_ALERT_WINDOW` (Display over other apps) cannot be granted at install time. The app detects whether the permission is granted on startup and redirects the user to the system settings screen if it is not. You must grant this manually before the floating bubble will appear.

---

## Release build

```
cd android
./gradlew assembleRelease
```

ProGuard is enabled for release builds. The rules in `android/app/proguard-rules.pro` preserve:

- Gson model classes (data package)
- Retrofit service interfaces
- OkHttp internals
- Kotlin coroutines
- Google ML Kit classes

A signing configuration is required for a distributable release APK. Configure it in `android/app/build.gradle.kts` under the `signingConfigs` block and reference your `.jks` keystore file. Do not commit the keystore or its credentials.

---

## Firebase plugin for release

The `com.google.gms.google-services` plugin is commented out in both Gradle files by default. For a push-notification-enabled release build, un-comment both occurrences and ensure a real `google-services.json` is in place. See [Firebase-Setup.md](Firebase-Setup.md).

---

## Known quirks

**Duplicate include in settings.gradle.kts.** An earlier version of the file contained a duplicate `include(":app")` line. This has been corrected. If you see a warning about it in older checkouts, it is harmless in Gradle 8+ but can be removed.

**Firebase plugin commented out.** The build system will log a warning that google-services is not applied. This is expected and intentional until Firebase is configured.
