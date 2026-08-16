package com.itbenevides.genesys21.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val authState: Flow<String?> // Retorna o UID do usuário ou null
    val userRole: Flow<com.itbenevides.genesys21.domain.model.UserRole?>

    suspend fun signIn(
        email: String,
        password: String,
    ): Result<String?>

    suspend fun signIn(
        idToken: String,
        accessToken: String?,
        provider: String,
    ): Result<String?>

    suspend fun signUp(
        email: String,
        password: String,
    ): Result<String?>

    suspend fun getCurrentUserToken(): String?

    suspend fun getCurrentUserId(): String?

    suspend fun getCurrentUserEmail(): String?

    suspend fun getCurrentUserName(): String?

    fun initializeOneTap()

    suspend fun signOut()

    suspend fun deleteUser(): Result<Unit>
}
