package com.itbenevides.genesys21.data.repository

import android.content.Context
import com.itbenevides.genesys21.domain.model.CartItem
import com.itbenevides.genesys21.domain.repository.AuthRepository
import io.ktor.client.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

class AndroidCartRepository(
    private val context: Context,
    httpClient: HttpClient,
    baseUrl: String,
    json: Json,
    authRepository: AuthRepository
) : BaseCartRepository(httpClient, baseUrl, json, authRepository) {

    private val prefs = context.getSharedPreferences("genesys21_prefs", Context.MODE_PRIVATE)
    private val CART_KEY = "cart_items"
    private val SESSION_KEY = "session_id"
    private var cachedSessionId: String? = null

    override fun getSessionId(): String {
        cachedSessionId?.let { return it }
        var id = prefs.getString(SESSION_KEY, null)
        if (id == null) {
            id = "sess_" + UUID.randomUUID().toString()
            prefs.edit().putString(SESSION_KEY, id).apply()
        }
        cachedSessionId = id
        return id
    }

    override suspend fun saveToLocal(items: List<CartItem>) {
        prefs.edit().putString(CART_KEY, json.encodeToString(items)).apply()
    }

    override suspend fun loadFromLocal(): List<CartItem> {
        val cached = prefs.getString(CART_KEY, null) ?: return emptyList()
        return try {
            json.decodeFromString(cached)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun saveSessionId(id: String) {
        prefs.edit().putString(SESSION_KEY, id).apply()
        cachedSessionId = id
    }

    override suspend fun loadSessionId(): String? {
        return prefs.getString(SESSION_KEY, null)
    }
}
