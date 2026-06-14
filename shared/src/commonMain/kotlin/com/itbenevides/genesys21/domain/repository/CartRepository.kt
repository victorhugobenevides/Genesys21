package com.itbenevides.genesys21.domain.repository

import com.itbenevides.genesys21.domain.model.CartItem
import kotlinx.coroutines.flow.StateFlow

interface CartRepository {
    val cartItems: StateFlow<List<CartItem>>

    suspend fun addToCart(item: CartItem): Result<Unit>

    suspend fun removeFromCart(productId: String): Result<Unit>

    suspend fun updateQuantity(
        productId: String,
        quantity: Int,
    ): Result<Unit>

    suspend fun clearCart(): Result<Unit>

    suspend fun syncWithServer(): Result<Unit>

    suspend fun loadInitialCart()

    fun getSessionId(): String
}
