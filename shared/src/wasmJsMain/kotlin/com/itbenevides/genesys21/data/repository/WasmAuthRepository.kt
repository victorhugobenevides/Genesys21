package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.repository.AuthRepository

class WasmAuthRepository : AuthRepository {
    override suspend fun signIn(email: String, password: String): Result<String?> = Result.failure(Exception("Not supported"))
    override suspend fun getCurrentUserToken(): String? = null
    override suspend fun signOut() {}
}
