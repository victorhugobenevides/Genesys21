package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.model.CartItem
import com.itbenevides.genesys21.domain.repository.AuthRepository
import com.itbenevides.genesys21.domain.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.random.Random

@JsFun("() => localStorage.getItem('cart_session_id')")
private external fun getSessionIdFromLocalStorage(): String?

@JsFun("(sessionId) => localStorage.setItem('cart_session_id', sessionId)")
private external fun setSessionIdInLocalStorage(sessionId: String)

class WasmCartRepository : CartRepository, KoinComponent {

    private val authRepository: AuthRepository by inject()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    override val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()
    private var currentSessionId: String? = null

    override fun getSessionId(): String {
        return currentSessionId ?: run {
            val fromStorage = getSessionIdFromLocalStorage()
            if (fromStorage != null) {
                currentSessionId = fromStorage
                fromStorage
            } else {
                val newId = "SESSION_${uuid4()}"
                setSessionIdInLocalStorage(newId)
                currentSessionId = newId
                newId
            }
        }
    }

    override suspend fun loadInitialCart() { }

    override suspend fun addToCart(item: CartItem): kotlin.Result<Unit> = runCatching {
        val currentCart = _cartItems.value.toMutableList()
        val existingItem = currentCart.find { it.product.id == item.product.id }
        if (existingItem != null) {
            val updatedItem = existingItem.copy(quantity = existingItem.quantity + item.quantity)
            currentCart[currentCart.indexOf(existingItem)] = updatedItem
        } else {
            currentCart.add(item)
        }
        _cartItems.value = currentCart
    }

    override suspend fun removeFromCart(productId: String): kotlin.Result<Unit> = runCatching {
        _cartItems.value = _cartItems.value.filterNot { it.product.id == productId }
    }

    override suspend fun updateQuantity(productId: String, quantity: Int): kotlin.Result<Unit> = runCatching {
        _cartItems.value = if (quantity <= 0) {
            _cartItems.value.filterNot { it.product.id == productId }
        } else {
            _cartItems.value.map {
                if (it.product.id == productId) it.copy(quantity = quantity) else it
            }
        }
    }

    override suspend fun clearCart(): kotlin.Result<Unit> = runCatching {
        _cartItems.value = emptyList()
    }
    
    override suspend fun syncWithServer(): kotlin.Result<Unit> = runCatching { }
}

private fun uuid4(): String {
    val chars = "0123456789abcdef".toCharArray()
    val uuid = CharArray(36)
    val rnd = Random.Default
    for (i in 0..35) {
        when (i) {
            8, 13, 18, 23 -> uuid[i] = '-'
            14 -> uuid[i] = '4'
            else -> {
                val r = rnd.nextInt(0, 16)
                uuid[i] = chars[r]
            }
        }
    }
    return uuid.concatToString()
}
