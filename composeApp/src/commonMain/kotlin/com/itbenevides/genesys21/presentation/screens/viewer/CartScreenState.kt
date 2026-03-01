package com.itbenevides.genesys21.presentation.screens.viewer

import com.itbenevides.genesys21.domain.model.CartItem

/**
 * UI State para a tela de Carrinho.
 */
data class CartScreenState(
    val cartItems: List<CartItem> = emptyList(),
    val total: Double = 0.0,
    val customerName: String = "",
    val customerPhone: String = "",
    val isLoading: Boolean = false,
    val isCreatingCheckout: Boolean = false, // Novo estado para o botão de pagamento
    val paymentUrl: String? = null
) {
    val isCheckoutEnabled: Boolean get() =
        customerName.isNotBlank() &&
        customerPhone.length >= 8 &&
        cartItems.isNotEmpty() &&
        !isLoading
}

/**
 * UI Intents (Eventos) para a tela de Carrinho.
 */
sealed class CartScreenEvent {
    data class OnUpdateQuantity(val productId: String, val newQuantity: Int) : CartScreenEvent()
    data class OnRemoveItem(val productId: String) : CartScreenEvent()
    data class OnCustomerNameChanged(val name: String) : CartScreenEvent()
    data class OnCustomerPhoneChanged(val phone: String) : CartScreenEvent()
    object OnCheckoutClicked : CartScreenEvent()
    object OnMercadoPagoCheckout : CartScreenEvent()
    object OnBackClicked : CartScreenEvent()
}
