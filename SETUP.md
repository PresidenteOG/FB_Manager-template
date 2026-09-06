# Setup

This app needs its own Firebase project — it doesn't ship with one.

1. Create a free project at [console.firebase.google.com](https://console.firebase.google.com).
2. Enable **Realtime Database** and **Authentication** (Email/Password is enough to start).
3. Register an Android app in that project with package name `com.fbmanager` (or change the
   package name to match yours in `app/build.gradle.kts` and the Kotlin sources).
4. Download the generated `google-services.json` and place it at `app/google-services.json` —
   use `app/google-services.json.example` as a reference for the shape Firebase expects.
5. Build and run as normal (`./gradlew assembleDebug` or via Android Studio).

`app/google-services.json` is gitignored — never commit your real one.
