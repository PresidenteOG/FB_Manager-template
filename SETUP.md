# Setup

Nothing external to configure — no Firebase, no API keys, no `.env`. Clone, build, run.

## Prerequisites

- JDK 17 (Android Studio bundles one at `<studio>/jbr`; `gradle.properties` points at it)
- Android SDK with platform 36 and build-tools, plus a `local.properties` with your
  `sdk.dir` (Android Studio writes this automatically on first open)
- An emulator or a device on API 26 or newer

## Build and run

```
./gradlew assembleDebug          # debug APK in app/build/outputs/apk/debug/
./gradlew installDebug           # install on a connected device/emulator
./gradlew testDebugUnitTest      # unit tests
```

Or open the project in Android Studio and use Run.

## Demo account

The app has one built-in admin login, checked locally against a hardcoded pair — there is no
account system:

```
1234@test.com  /  1234
```

The device database is empty on a fresh install and seeds itself with demo data (see
`data/local/SeedData.kt`) on the first launch. To start over, clear the app's storage or
reinstall.
