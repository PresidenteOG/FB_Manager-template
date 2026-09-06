# Architecture

Built an Android app that signs an admin into a Firebase-backed console, lists connected devices
in real time, and notifies them when a new one shows up — using Kotlin, Jetpack Compose and
Firebase Realtime Database.

## Structure

Clean-architecture-ish split, MVVM on top:

| Layer | Location | What it does |
|---|---|---|
| `domain/` | Plain Kotlin models and repository interfaces — no Android or Firebase imports. |
| `data/` | Implements those interfaces. `data/auth/AuthRepository` wraps Firebase Auth (email/password). `data/firebase/DeviceRepositoryImpl` listens to the Realtime Database and turns its snapshots into a Kotlin `Flow`. |
| `di/` | Hilt modules wiring the interfaces to their implementations. |
| `ui/` | Jetpack Compose screens and ViewModels (e.g. `DeviceListScreen` + `DeviceListViewModel`). |
| `notification/` | Local notification channel that fires when a new device appears. |

## Language / framework breakdown

| Part | Technology |
|---|---|
| UI | Kotlin + Jetpack Compose |
| State / DI | ViewModel + Hilt |
| Auth | Firebase Authentication |
| Data | Firebase Realtime Database, read as a Kotlin `Flow` |
| Local storage | `EncryptedSharedPreferences` for anything cached on-device |

## Data and external services

This app is built around Firebase — that's the point of it, not something to route around.
**It ships without a Firebase project of its own.** `app/google-services.json` is gitignored and
excluded from this template; `app/google-services.json.example` shows the shape Firebase expects.
Anyone using this template creates their own free Firebase project, drops in their own
`google-services.json`, and the app talks to their backend instead. See [SETUP.md](./SETUP.md).

## Running it

Follow [SETUP.md](./SETUP.md) first, then build normally (`./gradlew assembleDebug` or via
Android Studio).
