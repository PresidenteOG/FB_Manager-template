package com.fbmanager.data.auth

import android.content.Context
import android.util.Patterns
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local sign-in. There is no auth server — the app ships with a single demo admin
 * account and remembers the signed-in state in SharedPreferences.
 *
 * Demo credentials (also in the README):  1234@test.com  /  1234
 */
@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences("fbm_auth", Context.MODE_PRIVATE)

    fun isSignedIn(): Boolean = prefs.getBoolean(KEY_SIGNED_IN, false)

    /** Throws [IllegalArgumentException] with a user-facing message when the login is rejected. */
    fun signIn(email: String, password: String) {
        val normalized = email.trim()
        require(Patterns.EMAIL_ADDRESS.matcher(normalized).matches()) {
            "El email no tiene un formato válido"
        }
        require(normalized.equals(DEMO_EMAIL, ignoreCase = true) && password == DEMO_PASSWORD) {
            "Email o contraseña incorrectos"
        }
        prefs.edit().putBoolean(KEY_SIGNED_IN, true).apply()
    }

    fun signOut() = prefs.edit().putBoolean(KEY_SIGNED_IN, false).apply()

    companion object {
        const val DEMO_EMAIL = "1234@test.com"
        const val DEMO_PASSWORD = "1234"
        private const val KEY_SIGNED_IN = "signed_in"
    }
}
