package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.model.CartItem
import com.itbenevides.genesys21.domain.repository.AuthRepository
import com.itbenevides.genesys21.domain.repository.CartRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.browser.localStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class LocalStorageCartRepository(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val json: Json,
    private val authRepository: AuthRepository
) : CartRepository {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    override val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val CART_STORAGE_KEY = "genesys21_cart"
    private val SESSION_STORAGE_KEY = "genesys21_session_id"

    // CORREÇÃO: Adicionado override e tornado public conforme a interface
    override fun getSessionId(): String {
        var id = localStorage.getItem(SESSION_STORAGE_KEY)
        if (id == null) {
            id = "sess_" + (1..16).map { "abcdefghijklmnopqrstuvwxyz0123456789".random() }.joinToString("")
            localStorage.setItem(SESSION_STORAGE_KEY, id)
        }
        return id
    }

    override suspend fun loadInitialCart() {
        val cached = localStorage.getItem(CART_STORAGE_KEY)
        if (cached != null) {
            try {
                _cartItems.value = json.decodeFromString(cached)
            } catch (e: Exception) {
                _cartItems.value = emptyList()
            }
        }

        try {
            val token = authRepository.getCurrentUserToken()
            val response = httpClient.get("$baseUrl/api/cart") {
                if (token != null) header(HttpHeaders.Authorization, "Bearer $token")
                else header("X-Cart-Session-Id", getSessionId())
            }
            if (response.status.isSuccess()) {
                val serverItems: List<CartItem> = response.body()
                if (serverItems.isNotEmpty()) {
                    _cartItems.value = serverItems
                    saveToLocal()
                }
            }
        } catch (e: Exception) {
            println("Cart: Falha ao sincronizar inicial - ${e.message}")
        }
    }

    private fun saveToLocal() {
        localStorage.setItem(CART_STORAGE_KEY, json.encodeToString(_cartItems.value))
    }

    override suspend fun addToCart(item: CartItem): Result<Unit> {
        val current = _cartItems.value.toMutableList()
        val existing = current.find { it.product.id == item.product.id }
        if (existing != null) {
            val idx = current.indexOf(existing)
            current[idx] = existing.copy(quantity = existing.quantity + item.quantity)
        } else {
            current.add(item)
        }
        _cartItems.value = current
        saveToLocal()
        return syncWithServer()
    }

    override suspend fun removeFromCart(productId: String): Result<Unit> {
        _cartItems.value = _cartItems.value.filter { it.product.id != productId }
        saveToLocal()
        return syncWithServer()
    }

    override suspend fun updateQuantity(productId: String, quantity: Int): Result<Unit> {
        if (quantity <= 0) return removeFromCart(productId)
        _cartItems.value = _cartItems.value.map { if (it.product.id == productId) it.copy(quantity = quantity) else it }
        saveToLocal()
        return syncWithServer()
    }

    override suspend fun clearCart(): Result<Unit> {
        _cartItems.value = emptyList()
        localStorage.removeItem(CART_STORAGE_KEY)
        return syncWithServer()
    }

    override suspend fun syncWithServer(): Result<Unit> {
        return try {
            val token = authRepository.getCurrentUserToken()
            val response = httpClient.post("$baseUrl/api/cart") {
                if (token != null) header(HttpHeaders.Authorization, "Bearer $token")
                else header("X-Cart-Session-Id", getSessionId())
                
                contentType(ContentType.Application.Json)
                setBody(_cartItems.value)
            }
            if (response.status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception("Server error"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
