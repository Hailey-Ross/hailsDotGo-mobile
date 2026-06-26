# Architecture

## Package structure

```
android/app/src/main/java/live/hails/hailsdotgo/
  api/          Retrofit service interfaces and the OkHttp client singleton
  capture/      MediaProjection foreground service and OCR result accumulation state
  data/         Repositories, data models, and encrypted token storage
  ocr/          ML Kit text recognition pipeline and pixel-based detectors
  overlay/      Floating bubble and IV result overlay card (SYSTEM_ALERT_WINDOW)
  push/         Firebase Cloud Messaging receiver
  ui/           Jetpack Compose screens and ViewModels
    auth/       Login screen and ViewModel
    box/        Pokemon Box screen and ViewModel
    dashboard/  Dashboard screen and ViewModel
    events/     Events screen and ViewModel
    iv/         IV result screen and ViewModel
    raid/       Raid finder and lobby screens and ViewModels
    settings/   Settings screen
    theme/      Material 3 color scheme and typography
  util/         Date formatting utilities and notification channel helpers
```

---

## IV scan data flow

1. User taps the `OverlayService` floating bubble.
2. `OverlayService` sends `ACTION_CAPTURE` to `ScreenCaptureService` via a bound service connection.
3. `ScreenCaptureService` acquires one frame from `ImageReader` (MediaProjection virtual display), converts it to a `Bitmap`, and immediately stops projection.
4. The bitmap is passed to `OCRProcessor.process(bitmap)`.
5. ML Kit runs two passes: a full-image pass and a greyscale contrast-boosted crop of the top 18% of the screen. See [OCR System](OCR-System.md) for details.
6. Pixel detectors run independently on the same bitmap: `CPArcDetector`, `AppraisalBarDetector`, `PurifiedDetector`.
7. Results are merged into `CaptureState.accumulatedOCR` (a process-level singleton).
8. `OverlayService` receives the result callback and sends `ACTION_SHOW_IV_CARD` to itself.
9. The IV card opens; `IVResultViewModel` reads `CaptureState.accumulatedOCR` and pre-fills the submission form.
10. User reviews and submits; `IVRepository.calculate()` posts to the backend.
11. The backend response is rendered by `IVResultScreen`.

---

## Authentication flow

- `TokenStore` wraps `EncryptedSharedPreferences` (AES-256-GCM via Android Keystore) to persist the Bearer token across app restarts.
- `HailsDotGoApp.onCreate()` calls `AuthRepository.restoreSession()`, which reads the stored token and calls `ApiClient.setToken(token)`.
- `ApiClient.authInterceptor` attaches `Authorization: Bearer <token>` to every outgoing request.
- On login, `AuthRepository` receives the token from the backend, stores it via `TokenStore`, and calls `ApiClient.setToken()`.
- On logout, `AuthRepository` calls the backend logout endpoint, clears `TokenStore`, and calls `ApiClient.setToken(null)`.

---

## Key singletons

| Object | Location | Role |
|---|---|---|
| `ApiClient` | `api/ApiClient.kt` | Retrofit + OkHttp singleton, holds auth token |
| `CaptureState` | `capture/CaptureState.kt` | Accumulated OCR result across scans, process-scoped |
| `PokemonDataRepository` | `data/PokemonDataRepository.kt` | Caches pokemon stats and CP multipliers from backend |
