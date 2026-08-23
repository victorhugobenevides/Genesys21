package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.model.CartItem
import com.itbenevides.genesys21.domain.repository.AuthRepository
import com.itbenevides.genesys21.domain.repository.CartRepository
import com.itbenevides.genesys21.data.storage.SecureStorage
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
    protected val authRepository: AuthRepository,
    protected val secureStorage: com.itbenevides.genesys21.data.storage.SecureStorage,
) : CartRepository {
    protected val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    override val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    protected abstract suspend fun saveToLocal(items: List<CartItem>)

    protected abstract suspend fun loadFromLocal(): List<CartItem>

    private val SESSION_KEY = "genesys21_session_id"

    override suspend fun getSessionId(): String {
        val cached = secureStorage.get(SESSION_KEY)
        if (cached != null) return cached

        val newId = "sess_" + (1..16).map { "abcdefghijklmnopqrstuvwxyz0123456789".random() }.joinToString("")
        secureStorage.save(SESSION_KEY, newId)
        return newId
    }

    override suspend fun loadInitialCart() {
        val local = loadFromLocal()
        _cartItems.value = local

        try {
            val token = authRepository.getCurrentUserToken()
            val response =
                httpClient.get("$baseUrl/api/cart") {
                    if (token != null) {
                        header(HttpHeaders.Authorization, "Bearer $token")
                    } else {
                        header("X-Cart-Session-Id", getSessionId())
                    }
                }
            if (response.status.isSuccess()) {
                val serverItems: List<CartItem> = response.body()
                if (serverItems.isNotEmpty()) {
                    _cartItems.value = serverItems
                    saveToLocal(serverItems)
                }
            }
        } catch (e: Exception) {
            // Silencioso: mantem o que veio do local
        }
    }

    override suspend fun addToCart(item: CartItem): Result<Unit> {
        val current = _cartItems.value.toMutableList()
        val itemId = item.product?.id ?: item.service?.id ?: ""

        val existing = current.find {
            (it.product?.id ?: it.service?.id ?: "") == itemId
        }

        if (existing != null && item.product != null) {
            val idx = current.indexOf(existing)
            current[idx] = existing.copy(quantity = existing.quantity + item.quantity)
        } else {
            current.add(item)
        }
        _cartItems.value = current
        saveToLocal(current)
        return syncWithServer()
    }

    override suspend fun removeFromCart(itemId: String): Result<Unit> {
        val updated = _cartItems.value.filter { (it.product?.id ?: it.service?.id ?: "") != itemId }
        _cartItems.value = updated
        saveToLocal(updated)
        return syncWithServer()
    }

    override suspend fun updateQuantity(
        itemId: String,
        quantity: Int,
    ): Result<Unit> {
        if (quantity <= 0) return removeFromCart(itemId)
        val updated =
            _cartItems.value.map {
                if ((it.product?.id ?: it.service?.id ?: "") == itemId) it.copy(quantity = quantity) else it
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

    override suspend fun mergeWithServer(): Result<Unit> {
        return try {
            val token = authRepository.getCurrentUserToken() ?: return Result.failure(Exception("Not logged in"))

            // 1. Get current local items
            val localItems = _cartItems.value

            // 2. Fetch server items for the authenticated user
            val response = httpClient.get("$baseUrl/api/cart") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }

            if (response.status.isSuccess()) {
                val serverItems: List<CartItem> = response.body()

                // 3. Merge lists
                val mergedList = localItems.toMutableList()
                serverItems.forEach { serverItem ->
                    val serverItemId = serverItem.product?.id ?: serverItem.service?.id ?: ""
                    val existing = mergedList.find { (it.product?.id ?: it.service?.id ?: "") == serverItemId }

                    if (existing != null) {
                        val idx = mergedList.indexOf(existing)
                        // Sum quantities for products
                        if (serverItem.product != null) {
                            mergedList[idx] = existing.copy(quantity = existing.quantity + serverItem.quantity)
                        } else {
                            // For services/appointments, we keep the latest or one of them (usually 1 quantity anyway)
                            mergedList[idx] = serverItem
                        }
                    } else {
                        mergedList.add(serverItem)
                    }
                }

                // 4. Update local state and persistence
                _cartItems.value = mergedList
                saveToLocal(mergedList)

                // 5. Push merged list back to server
                syncWithServer()
            } else {
                Result.failure(Exception("Failed to fetch server cart for merge"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncWithServer(): Result<Unit> {
        return try {
            val token = authRepository.getCurrentUserToken()
            val response =
                httpClient.post("$baseUrl/api/cart") {
                    if (token != null) {
                        header(HttpHeaders.Authorization, "Bearer $token")
                    } else {
                        header("X-Cart-Session-Id", getSessionId())
                    }

                    contentType(ContentType.Application.Json)
                    setBody(_cartItems.value)
                }
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Sync failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
