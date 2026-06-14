package com.itbenevides.genesys21.domain.repository

interface AuthRepository {
    suspend fun signIn(
        email: String,
        password: String,
    ): Result<String?>

    suspend fun getCurrentUserToken(): String?

    suspend fun signOut()
}
