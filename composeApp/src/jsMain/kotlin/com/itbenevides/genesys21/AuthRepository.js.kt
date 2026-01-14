package com.itbenevides.genesys21

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth

class JsAuthRepository : AuthRepository {
    private val auth = Firebase.auth

    override suspend fun signIn(email: String, password: String): Result<String?> {
        return try {
            auth.signInWithEmailAndPassword(email, password)
            Result.success(getCurrentUserToken())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUserToken(): String? {
        return auth.currentUser?.getIdToken(false)
    }

    override suspend fun signOut() {
        auth.signOut()
    }
}

actual fun getAuthRepository(): AuthRepository = JsAuthRepository()
