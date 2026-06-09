package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.model.CartItem
import com.itbenevides.genesys21.domain.repository.AuthRepository
import com.itbenevides.genesys21.domain.repository.CartRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

abstract class BaseCartRepository(
    protected val httpClient: HttpClient,
    protected val baseUrl: String,
    protected val json: Json,
    protected val authRepository: AuthRepository
) : CartRepository {

    protected val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    override val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    protected abstract suspend fun saveToLocal(items: List<CartItem>)
    protected abstract suspend fun loadFromLocal(): List<CartItem>
    protected abstract suspend fun saveSessionId(id: String)
    protected abstract suspend fun loadSessionId(): String?

    override fun getSessionId(): String {
        // This needs to be synchronous for some callers, but persistence might be async.
        // We'll assume for now that the ID is loaded into memory or generated.
        return "placeholder" // Implement specifically in platforms or use a cached value
    }

    override suspend fun loadInitialCart() {
        _cartItems.value = loadFromLocal()
        syncWithServer()
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
        saveToLocal(current)
        return syncWithServer()
    }

    override suspend fun removeFromCart(productId: String): Result<Unit> {
        val updated = _cartItems.value.filter { it.product.id != productId }
        _cartItems.value = updated
        saveToLocal(updated)
        return syncWithServer()
    }

    override suspend fun updateQuantity(productId: String, quantity: Int): Result<Unit> {
        if (quantity <= 0) return removeFromCart(productId)
        val updated = _cartItems.value.map { 
            if (it.product.id == productId) it.copy(quantity = quantity) else it 
        }
        _cartItems.value = updated
        saveToLocal(updated)
        return syncWithServer()
    }

    override suspend fun clearCart(): Result<Unit> {
        _cartItems.value = emptyList()
        saveToLocal(emptyList())
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
            if (response.status.isSuccess()) {
                val serverItems: List<CartItem> = response.body()
                if (serverItems.isNotEmpty()) {
                    _cartItems.value = serverItems
                    saveToLocal(serverItems)
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Sync failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
