package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.model.CartItem
import com.itbenevides.genesys21.domain.repository.AuthRepository
import io.ktor.client.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsFun("(key) => window.localStorage.getItem(key)")
private external fun jsGetItem(key: String): String?

@JsFun("(key, value) => window.localStorage.setItem(key, value)")
private external fun jsSetItem(key: String, value: String)

class LocalStorageCartRepository(
    httpClient: HttpClient,
    baseUrl: String,
    json: Json,
    authRepository: AuthRepository
) : BaseCartRepository(httpClient, baseUrl, json, authRepository) {

    private val CART_STORAGE_KEY = "genesys21_cart"
    private val SESSION_STORAGE_KEY = "genesys21_session_id"
    private var cachedSessionId: String? = null

    override fun getSessionId(): String {
        cachedSessionId?.let { return it }
        var id = jsGetItem(SESSION_STORAGE_KEY)
        if (id == null) {
            id = "sess_" + (1..16).map { "abcdefghijklmnopqrstuvwxyz0123456789".random() }.joinToString("")
            jsSetItem(SESSION_STORAGE_KEY, id)
        }
        cachedSessionId = id
        return id
    }

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

    override suspend fun saveSessionId(id: String) {
        jsSetItem(SESSION_STORAGE_KEY, id)
        cachedSessionId = id
    }

    override suspend fun loadSessionId(): String? {
        return jsGetItem(SESSION_STORAGE_KEY)
    }
}
