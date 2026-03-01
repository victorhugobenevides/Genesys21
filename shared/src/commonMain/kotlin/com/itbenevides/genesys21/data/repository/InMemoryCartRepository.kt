package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.model.CartItem
import com.itbenevides.genesys21.domain.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryCartRepository : CartRepository {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    override val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    override suspend fun addToCart(item: CartItem): Result<Unit> = runCatching {
        val current = _cartItems.value.toMutableList()
        val existing = current.find { it.product.id == item.product.id }
        if (existing != null) {
            val idx = current.indexOf(existing)
            current[idx] = existing.copy(quantity = existing.quantity + item.quantity)
        } else {
            current.add(item)
        }
        _cartItems.value = current
    }

    override suspend fun removeFromCart(productId: String): Result<Unit> = runCatching {
        _cartItems.value = _cartItems.value.filter { it.product.id != productId }
    }

    override suspend fun updateQuantity(productId: String, quantity: Int): Result<Unit> = runCatching {
        if (quantity <= 0) {
            removeFromCart(productId)
        } else {
            _cartItems.value = _cartItems.value.map { 
                if (it.product.id == productId) it.copy(quantity = quantity) else it 
            }
        }
    }

    override suspend fun clearCart(): Result<Unit> = runCatching {
        _cartItems.value = emptyList()
    }

    override suspend fun syncWithServer(): Result<Unit> = Result.success(Unit)
    override suspend fun loadInitialCart() {}
    override fun getSessionId(): String = "memory_session"
}
