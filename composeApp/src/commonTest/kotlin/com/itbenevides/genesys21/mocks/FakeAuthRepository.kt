package com.itbenevides.genesys21.mocks

import com.itbenevides.genesys21.domain.repository.AuthRepository

class FakeAuthRepository : AuthRepository {
    var shouldReturnError = false
    var mockToken: String? = "fake_token"

    override suspend fun signIn(
        email: String,
        password: String,
    ): Result<String?> {
        return if (shouldReturnError) {
            Result.failure(Exception("Login falhou"))
        } else {
            Result.success(mockToken)
        }
    }

    override suspend fun signIn(
        idToken: String,
        accessToken: String?,
        provider: String,
    ): Result<String?> {
        return if (shouldReturnError) {
            Result.failure(Exception("Login falhou"))
        } else {
            Result.success(mockToken)
        }
    }

    override suspend fun getCurrentUserToken(): String? = mockToken

    fun setToken(token: String?) {
        mockToken = token
    }

    override suspend fun signOut() {
        mockToken = null
    }
}
