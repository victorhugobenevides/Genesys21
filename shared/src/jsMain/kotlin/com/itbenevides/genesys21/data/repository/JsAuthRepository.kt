package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.repository.AuthRepository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class JsAuthRepository : AuthRepository {
    override val authState: Flow<String?> = emptyFlow()

    override suspend fun signIn(
        email: String,
        password: String,
    ): Result<String?> {
        return Result.failure(Exception("Firebase not supported on JS in this version"))
    }

    override suspend fun signIn(
        idToken: String,
        accessToken: String?,
        provider: String,
    ): Result<String?> {
        return Result.failure(Exception("Firebase not supported on JS in this version"))
    }

    override suspend fun signUp(
        email: String,
        password: String,
    ): Result<String?> {
        return Result.failure(Exception("Firebase not supported on JS in this version"))
    }

    override suspend fun getCurrentUserToken(): String? {
        return null
    }

    override suspend fun getCurrentUserId(): String? {
        return null
    }

    override fun initializeOneTap() {}

    override suspend fun signOut() {
    }
}
