package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.model.CartItem
import com.itbenevides.genesys21.domain.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryCartRepository : CartRepository {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    override val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    override suspend fun addToCart(item: CartItem): Result<Unit> {
        _cartItems.value = _cartItems.value + item
        return Result.success(Unit)
    }

    override suspend fun removeFromCart(productId: String): Result<Unit> {
        _cartItems.value = _cartItems.value.filterNot { it.product.id == productId }
        return Result.success(Unit)
    }

    override suspend fun updateQuantity(productId: String, quantity: Int): Result<Unit> {
        // Implementation for update quantity
        return Result.success(Unit)
    }

    override suspend fun clearCart(): Result<Unit> {
        _cartItems.value = emptyList()
        return Result.success(Unit)
    }

    override suspend fun syncWithServer(): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun loadInitialCart() {
        // No-op
    }

    override fun getSessionId(): String = "test-session"
}
