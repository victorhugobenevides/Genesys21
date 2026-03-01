package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.repository.AuthRepository

class JsAuthRepository : AuthRepository {
    override suspend fun signIn(email: String, password: String): Result<String?> {
        return Result.failure(Exception("Firebase not supported on JS in this version"))
    }

    override suspend fun getCurrentUserToken(): String? {
        return null
    }

    override suspend fun signOut(): Result<Unit> {
        return Result.success(Unit)
    }
}
