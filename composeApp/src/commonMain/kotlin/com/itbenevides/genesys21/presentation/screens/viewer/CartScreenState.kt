package com.itbenevides.genesys21.presentation.screens.viewer

import com.itbenevides.genesys21.domain.model.CartItem

/**
 * UI State para a tela de Carrinho.
 */
data class CartScreenState(
    val cartItems: List<CartItem> = emptyList(),
    val total: Double = 0.0,
    val customerName: String = "",
    val isLoading: Boolean = false
) {
    val isCheckoutEnabled: Boolean get() = customerName.isNotBlank() && cartItems.isNotEmpty() && !isLoading
}

/**
 * UI Intents (Eventos) para a tela de Carrinho.
 */
sealed class CartScreenEvent {
    data class OnUpdateQuantity(val productId: String, val newQuantity: Int) : CartScreenEvent()
    data class OnRemoveItem(val productId: String) : CartScreenEvent()
    data class OnCustomerNameChanged(val name: String) : CartScreenEvent()
    object OnCheckoutClicked : CartScreenEvent()
    object OnBackClicked : CartScreenEvent()
}
