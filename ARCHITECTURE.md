# Architecture

An admin signs in, lands on the list of every device in the fleet, and can open any one of
them to change its state or extend its subscription. Each row shows the device's status and
whether its payment is current, expiring or overdue, and the header keeps a running count of
each. A second screen per device shows what that device has been listening to.

Kotlin and Jetpack Compose for the UI. A ViewModel per screen, Room for storage, Hilt to
wire it together. **Why this stack:** it is what Google recommends for new native Android
today — Compose instead of XML layouts, Room instead of raw SQLite, Hilt as the standard DI
option on top of Dagger.

![FB Manager architecture: Compose screens and a ViewModel call a DeviceRepository interface in domain, implemented by DeviceRepositoryImpl over a Room database in data/local; Hilt modules connect the two](./docs/architecture.png)

## Layers

MVVM in the UI, a repository boundary underneath it.

| Layer | Location | What lives there |
|---|---|---|
| `domain/` | `model/DeviceNode`, `repository/DeviceRepository` | Plain Kotlin. The model and the repository interface — no Android, no Room imports. `DeviceNode.paymentStatus` is the one piece of business logic here: it turns an expiry timestamp into active / expiring / overdue / unset. |
| `data/local/` | `AppDatabase`, `DeviceDao`, `DeviceEntity`, `DeviceRepositoryImpl`, `SeedData` | The Room implementation of `DeviceRepository`. `DeviceEntity` maps to the `devices` table and converts to the domain model; `DeviceRepositoryImpl` exposes the table as a `Flow` and seeds it on first use. |
| `data/auth/` | `AuthRepository` | Local sign-in. Checks the entered credentials against one hardcoded demo pair and keeps a boolean in `SharedPreferences`. |
| `di/` | `DatabaseModule`, `RepositoryModule` | Hilt. `DatabaseModule` builds the Room instance and hands out the DAO; `RepositoryModule` binds `DeviceRepositoryImpl` to the `DeviceRepository` interface. |
| `ui/` | `screens/devices/*`, `theme/`, `navigation/` | Compose. `DeviceListViewModel` holds auth state and the device list; `DeviceListScreen` renders the list, the edit sheet and the per-device data dialog. |
| `notification/` | `NotificationHelper` | A local notification channel that fires when a row appears in the list that was not there at sign-in. |

## How a change moves through it

Editing a device from the bottom sheet: the sheet calls `DeviceListViewModel.updateDevice`
with a map of changed fields, the ViewModel forwards it to `DeviceRepository.updateDevice`,
`DeviceRepositoryImpl` loads the current `DeviceEntity`, copies it with the new values and
writes it back through the DAO. Room re-emits the `devices` query, the ViewModel's collector
pushes the new list into its `StateFlow`, and the list recomposes.

## Storage and external services

There are none. The manifest has no `INTERNET` permission. Everything the app shows comes
from the Room database at `fbm.db`, which is populated from `data/local/SeedData.kt` — 22
invented devices, all fictional — the first time `DeviceRepositoryImpl` sees an empty table.

## Tech at a glance

| Part | Technology |
|---|---|
| UI | Kotlin, Jetpack Compose, Material 3 |
| Screen state / DI | ViewModel, Hilt |
| Persistence | Room (SQLite), exposed as a `Flow` |
| Auth | Local check against a demo credential, flag in `SharedPreferences` |
| Notifications | `NotificationManagerCompat`, one local channel |

## Running it

See [SETUP.md](./SETUP.md). Verified here: `./gradlew testDebugUnitTest assembleDebug`
runs the unit tests and packages a debug APK with no additional configuration.
