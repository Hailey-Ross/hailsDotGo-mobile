# hailsDotGo-mobile Wiki

Welcome to the project documentation. This wiki covers everything needed to understand, build, and contribute to the hailsDotGo-mobile Android app.

---

## About

hailsDotGo-mobile is an Android companion app for Pokemon GO. It provides on-device IV scanning via a floating overlay, raid lobby coordination, event listings, and a personal Pokemon box. All OCR processing runs on-device using Google ML Kit; no screenshot data is uploaded.

The app connects to a private backend at `https://pogo.hails.live/` using Bearer token authentication.

---

## Pages

| Page | Description |
|---|---|
| [Architecture](Architecture.md) | Kotlin package tree, IV scan data flow, and authentication flow |
| [OCR System](OCR-System.md) | Screen capture pipeline, two-pass ML Kit OCR, and all pixel detectors |
| [API Reference](API-Reference.md) | All backend endpoints grouped by service, with request and response shapes |
| [Firebase Setup](Firebase-Setup.md) | Step-by-step guide to configure Firebase and enable push notifications |
| [Build Guide](Build-Guide.md) | Debug and release build instructions, ProGuard notes, and permission requirements |
| [Push Notifications](Push-Notifications.md) | FCM implementation details and guide to completing the notification stub |

---

## Quick start

For first-time setup, follow the steps in the root [README](../README.md), then refer to [Firebase Setup](Firebase-Setup.md) and [Build Guide](Build-Guide.md).

For understanding how the IV scanner works end-to-end, read [Architecture](Architecture.md) followed by [OCR System](OCR-System.md).
