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
        val current = _cartItems.value.toMutableList()
        val existing = current.find { it.product.id == item.product.id }
        if (existing != null) {
            val idx = current.indexOf(existing)
            current[idx] = existing.copy(quantity = existing.quantity + item.quantity)
        } else {
            current.add(item)
        }
        _cartItems.value = current
        return Result.success(Unit)
    }

    override suspend fun removeFromCart(productId: String): Result<Unit> {
        _cartItems.value = _cartItems.value.filter { it.product.id != productId }
        return Result.success(Unit)
    }

    override suspend fun updateQuantity(productId: String, quantity: Int): Result<Unit> {
        if (quantity <= 0) return removeFromCart(productId)
        _cartItems.value = _cartItems.value.map { if (it.product.id == productId) it.copy(quantity = quantity) else it }
        return Result.success(Unit)
    }

    override suspend fun clearCart(): Result<Unit> {
        _cartItems.value = emptyList()
        return Result.success(Unit)
    }

    override suspend fun syncWithServer(): Result<Unit> = Result.success(Unit)
    override suspend fun loadInitialCart() {}
    override fun getSessionId(): String = "ios_session"
}
