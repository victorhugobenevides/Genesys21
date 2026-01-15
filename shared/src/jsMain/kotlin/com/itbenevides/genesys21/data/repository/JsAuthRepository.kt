package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.repository.AuthRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.apps

class JsAuthRepository : AuthRepository {
    
    init {
        ensureFirebaseInitialized()
    }

    private fun ensureFirebaseInitialized() {
        try {
            if (Firebase.apps().isEmpty()) {
                Firebase.initialize(
                    options = FirebaseOptions(
                        apiKey = "AIzaSyCq22tklAK0iQd4jWDINkJZAS9-I_-dLSY",
                        authDomain = "genesys21-32035.firebaseapp.com",
                        applicationId = "1:674755208954:web:26e7b20a54f9ceb0dc4b43",
                        projectId = "genesys21-32035",
                        storageBucket = "genesys21-32035.firebasestorage.app"
                    )
                )
            }
        } catch (e: Exception) { }
    }

    private val auth get() = Firebase.auth

    override suspend fun signIn(email: String, password: String): Result<String?> {
        ensureFirebaseInitialized() 
        return try {
            auth.signInWithEmailAndPassword(email, password)
            Result.success(getCurrentUserToken())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUserToken(): String? {
        ensureFirebaseInitialized()
        return try {
            auth.currentUser?.getIdToken(false)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun signOut() {
        ensureFirebaseInitialized()
        try {
            auth.signOut()
        } catch (e: Exception) { }
    }
}
