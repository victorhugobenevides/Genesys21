package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.model.CartItem
import com.itbenevides.genesys21.domain.repository.AuthRepository
import io.ktor.client.*
import io.ktor.http.*
import kotlinx.browser.localStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class LocalStorageCartRepository(
    httpClient: HttpClient,
    baseUrl: String,
    json: Json,
    authRepository: AuthRepository,
) : BaseCartRepository(httpClient, baseUrl, json, authRepository) {

    private val CART_STORAGE_KEY = "genesys21_cart"
    private val SESSION_STORAGE_KEY = "genesys21_session_id"

    override suspend fun saveToLocal(items: List<CartItem>) {
        localStorage.setItem(CART_STORAGE_KEY, json.encodeToString(items))
    }

    override suspend fun loadFromLocal(): List<CartItem> {
        val cached = localStorage.getItem(CART_STORAGE_KEY) ?: return emptyList()
        return try {
            json.decodeFromString(cached)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun saveSessionId(id: String) {
        localStorage.setItem(SESSION_STORAGE_KEY, id)
    }

    override suspend fun loadSessionId(): String? {
        return localStorage.getItem(SESSION_STORAGE_KEY)
    }
}
