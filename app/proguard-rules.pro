# Hilt / Dagger
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.InstallIn class * { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keepnames @dagger.hilt.android.lifecycle.HiltViewModel class *

# Firebase
-keep class com.google.firebase.database.** { *; }
-keep class com.google.firebase.auth.** { *; }
-dontwarn com.google.firebase.**

# Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keep class com.fbmanager.domain.model.** { *; }
