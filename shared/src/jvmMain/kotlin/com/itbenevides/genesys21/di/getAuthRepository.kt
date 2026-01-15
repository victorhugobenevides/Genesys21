package com.itbenevides.genesys21.di

import com.itbenevides.genesys21.domain.repository.AuthRepository

// Implementação dummy para o Servidor (JVM), que usa Firebase Admin nativamente
actual fun getAuthRepository(): AuthRepository = object : AuthRepository {
    override suspend fun signIn(email: String, password: String): Result<String?> = Result.failure(Exception("Not implemented on JVM"))
    override suspend fun getCurrentUserToken(): String? = null
    override suspend fun signOut() {}
}
