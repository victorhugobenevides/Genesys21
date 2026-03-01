package com.itbenevides.genesys21.mocks

import com.itbenevides.genesys21.domain.repository.AuthRepository

class FakeAuthRepository : AuthRepository {
    private var fakeToken: String? = null

    var shouldSucceed = true
    var lastAction = ""

    override suspend fun signIn(email: String, password: String): Result<String?> {
        lastAction = "signIn"
        return if (shouldSucceed) {
            fakeToken = "fake-token"
            Result.success(fakeToken)
        } else {
            Result.failure(Exception("Login failed"))
        }
    }

    override suspend fun signOut(): Result<Unit> {
        lastAction = "signOut"
        fakeToken = null
        return Result.success(Unit)
    }

    override suspend fun getCurrentUserToken(): String? {
        return fakeToken
    }
}
