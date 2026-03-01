package com.itbenevides.genesys21.mocks

import com.itbenevides.genesys21.domain.repository.AuthRepository

class FakeAuthRepository : AuthRepository {
    var shouldReturnError = false
    var mockToken: String? = "fake_token"

    override suspend fun signIn(email: String, password: String): Result<String?> {
        return if (shouldReturnError) Result.failure(Exception("Login falhou"))
        else Result.success(mockToken)
    }

    override suspend fun getCurrentUserToken(): String? = mockToken

    override suspend fun signOut(): Result<Unit> {
        mockToken = null
        return Result.success(Unit)
    }
}
