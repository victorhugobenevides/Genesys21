package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.model.CartItem
import com.itbenevides.genesys21.domain.repository.AuthRepository
import io.ktor.client.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsFun("(key) => window.localStorage.getItem(key)")
private external fun jsGetItem(key: String): String?

@JsFun("(key, value) => window.localStorage.setItem(key, value)")
private external fun jsSetItem(
    key: String,
    value: String,
)

@JsFun("(key) => window.localStorage.removeItem(key)")
private external fun jsRemoveItem(key: String)

class LocalStorageCartRepository(
    httpClient: HttpClient,
    baseUrl: String,
    json: Json,
    authRepository: AuthRepository,
    secureStorage: com.itbenevides.genesys21.data.storage.SecureStorage,
) : BaseCartRepository(httpClient, baseUrl, json, authRepository, secureStorage) {
    private val CART_STORAGE_KEY = "genesys21_cart"

    override suspend fun saveToLocal(items: List<CartItem>) {
        jsSetItem(CART_STORAGE_KEY, json.encodeToString(items))
    }

    override suspend fun loadFromLocal(): List<CartItem> {
        val cached = jsGetItem(CART_STORAGE_KEY) ?: return emptyList()
        return try {
            json.decodeFromString(cached)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun clearCart(): Result<Unit> {
        jsRemoveItem(CART_STORAGE_KEY)
        return super.clearCart()
    }
}
