package com.itbenevides.genesys21

interface AuthRepository {
    suspend fun signIn(email: String, password: String): Result<String?> // Retorna o ID Token
    suspend fun getCurrentUserToken(): String?
    suspend fun signOut()
}

expect fun getAuthRepository(): AuthRepository
