package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.repository.AuthRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.auth

class AndroidAuthRepository : AuthRepository {
    private val auth = Firebase.auth

    override suspend fun signIn(
        email: String,
        password: String,
    ): Result<String?> {
        return try {
            auth.signInWithEmailAndPassword(email, password)
            Result.success(getCurrentUserToken())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signIn(
        idToken: String,
        accessToken: String?,
        provider: String,
    ): Result<String?> {
        return try {
            if (provider == "google") {
                val credential = GoogleAuthProvider.credential(idToken, accessToken)
                auth.signInWithCredential(credential)
                Result.success(getCurrentUserToken())
            } else {
                Result.failure(Exception("Provedor não suportado: \$provider"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUp(
        email: String,
        password: String,
    ): Result<String?> {
        return try {
            auth.createUserWithEmailAndPassword(email, password)
            Result.success(getCurrentUserToken())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUserToken(): String? {
        return auth.currentUser?.getIdToken(false)
    }

    override suspend fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    override suspend fun signOut() {
        auth.signOut()
    }
}
