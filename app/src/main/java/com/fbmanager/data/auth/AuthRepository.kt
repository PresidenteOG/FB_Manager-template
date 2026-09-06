package com.fbmanager.data.auth

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor() {
    private val auth = FirebaseAuth.getInstance()

    fun isSignedIn(): Boolean = auth.currentUser != null

    suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    fun signOut() = auth.signOut()
}
