package com.itbenevides.genesys21.presentation.screens.viewer

import com.itbenevides.genesys21.domain.model.Address
import com.itbenevides.genesys21.domain.model.CartItem
import com.itbenevides.genesys21.domain.model.PaymentMethod
import com.itbenevides.genesys21.domain.model.ShippingOption

/**
 * UI State para a tela de Carrinho.
 */
data class CartScreenState(
    val cartItems: List<CartItem> = emptyList(),
    val total: Double = 0.0,
    val customerName: String = "",
    val customerPhone: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.APP,
    val shippingAddress: Address? = null,
    val availableShippingOptions: List<ShippingOption> = emptyList(),
    val selectedShippingOption: ShippingOption? = null,
    val isLoading: Boolean = false,
    val currentStep: Int = 1, // 1: Itens, 2: Identificação/Endereço, 3: Pagamento/Revisão
    val isGuestCheckout: Boolean = false,
    val stripeClientSecret: String? = null,
    val stripePublishableKey: String? = null,
) {
    val isCheckoutEnabled: Boolean get() {
        val isPickup = selectedShippingOption?.id == "pickup"
        val needsAddress = needsShipping && !isPickup

        return customerName.isNotBlank() &&
            customerPhone.length >= 8 &&
            cartItems.isNotEmpty() &&
            !isLoading &&
            (!needsAddress || shippingAddress != null)
    }

    val grandTotal: Double get() {
        val travelFees = cartItems.sumOf { it.appointment?.travelFee ?: 0.0 }
        return total + (selectedShippingOption?.price ?: 0.0) + travelFees
    }

    val needsShipping: Boolean get() = cartItems.any { it.product != null }
}

/**
 * UI Intents (Eventos) para a tela de Carrinho.
 */
sealed class CartScreenEvent {
    data class OnUpdateQuantity(val productId: String, val newQuantity: Int) : CartScreenEvent()

    data class OnRemoveItem(val itemId: String) : CartScreenEvent()

    data class OnCustomerNameChanged(val name: String) : CartScreenEvent()

    data class OnCustomerPhoneChanged(val phone: String) : CartScreenEvent()

    data class OnAddressChanged(val address: Address) : CartScreenEvent()

    data class OnShippingOptionSelected(val option: ShippingOption) : CartScreenEvent()

    data class OnPaymentMethodChanged(val method: PaymentMethod) : CartScreenEvent()

    data class OnStepChanged(val step: Int) : CartScreenEvent()

    object OnCheckoutClicked : CartScreenEvent()

    object OnBackClicked : CartScreenEvent()

    data class OnStripePaymentConfirmed(val orderId: String) : CartScreenEvent()
    data class OnStripePaymentError(val message: String) : CartScreenEvent()
}
