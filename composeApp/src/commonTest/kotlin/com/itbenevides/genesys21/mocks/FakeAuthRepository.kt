package com.itbenevides.genesys21.mocks

import com.itbenevides.genesys21.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeAuthRepository : AuthRepository {
    var shouldReturnError = false
    var mockToken: String? = "fake_token"

    private val _authState = MutableStateFlow<String?>(null)
    override val authState: Flow<String?> = _authState.asStateFlow()

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

    override suspend fun signUp(
        email: String,
        password: String,
    ): Result<String?> {
        return if (shouldReturnError) {
            Result.failure(Exception("Signup falhou"))
        } else {
            Result.success(mockToken)
        }
    }

    override suspend fun getCurrentUserToken(): String? = mockToken

    override suspend fun getCurrentUserId(): String? = if (mockToken != null) "test_user_id" else null

    override suspend fun getCurrentUserEmail(): String? = if (mockToken != null) "test@example.com" else null

    override suspend fun getCurrentUserName(): String? = if (mockToken != null) "Test User" else null

    fun setToken(token: String?) {
        mockToken = token
    }

    override suspend fun signOut() {
        mockToken = null
    }

    override fun initializeOneTap() {
        // Mock implementation
    }
}
