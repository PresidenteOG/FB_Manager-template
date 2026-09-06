# Architecture

Built an Android app that signs an admin into a Firebase-backed console, lists connected devices
in real time, and notifies them when a new one shows up — using Kotlin, Jetpack Compose and
Firebase Realtime Database.

An admin signs in with email and password, lands on a list of every device registered under the
Firebase project, and that list updates the moment a device's status changes elsewhere — no
refresh needed, because it's reading a live `Flow` off the Realtime Database. When a brand-new
device shows up, the app fires a local notification so the admin doesn't have to be staring at the
screen to notice.

![FB_Manager architecture: Compose UI calls domain interfaces, implemented by AuthRepository and DeviceRepositoryImpl, backed by your own Firebase project](./docs/architecture.png)

**Why Kotlin + Jetpack Compose**: this is Google's current recommended stack for native Android —
Compose replaces the older View/XML layout system with declarative UI, which is what most new
Android code and most tutorials use today.

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
Android Studio). Verified: `./gradlew assembleDebug` compiles and packages a debug APK cleanly
against a placeholder `google-services.json` matching [SETUP.md](./SETUP.md)'s shape — the code
itself builds; the only thing missing to run it for real is your own Firebase project.
