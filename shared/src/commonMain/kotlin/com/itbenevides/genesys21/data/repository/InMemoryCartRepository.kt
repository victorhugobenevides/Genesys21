package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.model.CartItem
import com.itbenevides.genesys21.domain.repository.AuthRepository
import com.itbenevides.genesys21.data.storage.SecureStorage
import io.ktor.client.*
import kotlinx.serialization.json.Json

class InMemoryCartRepository(
    httpClient: HttpClient,
    baseUrl: String,
    json: Json,
    authRepository: AuthRepository,
    secureStorage: SecureStorage,
) : BaseCartRepository(httpClient, baseUrl, json, authRepository, secureStorage) {

    override suspend fun saveToLocal(items: List<CartItem>) {
        // Just in memory, handled by BaseCartRepository._cartItems
    }

    override suspend fun loadFromLocal(): List<CartItem> {
        return emptyList()
    }
}
