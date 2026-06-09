package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.model.CartItem
import com.itbenevides.genesys21.domain.repository.AuthRepository
import io.ktor.client.*
import kotlinx.serialization.json.Json

// Forced sync to resolve disk desync
class InMemoryCartRepository(
    httpClient: HttpClient,
    baseUrl: String,
    json: Json,
    authRepository: AuthRepository
) : BaseCartRepository(httpClient, baseUrl, json, authRepository) {

    private var session: String? = null

    override fun getSessionId(): String {
        if (session == null) session = "jvm_sess_" + (1..8).map { (0..9).random() }.joinToString("")
        return session!!
    }

    override suspend fun saveToLocal(items: List<CartItem>) {
        // Just in memory
    }

    override suspend fun loadFromLocal(): List<CartItem> {
        return emptyList()
    }

    override suspend fun saveSessionId(id: String) {
        session = id
    }

    override suspend fun loadSessionId(): String? {
        return session
    }
}
