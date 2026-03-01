package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.repository.AuthRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth

class IosAuthRepository : AuthRepository {
    private val auth = Firebase.auth

    override suspend fun signIn(email: String, password: String): Result<String?> {
        return runCatching {
            auth.signInWithEmailAndPassword(email, password)
            getCurrentUserToken()
        }
    }

    override suspend fun getCurrentUserToken(): String? {
        return auth.currentUser?.getIdToken(false)
    }

    override suspend fun signOut(): Result<Unit> {
        return runCatching {
            auth.signOut()
        }
    }
}
