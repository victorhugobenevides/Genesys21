package com.itbenevides.genesys21.presentation.screens.viewer

import com.itbenevides.genesys21.domain.model.Order

/**
 * UI State para a tela de Acompanhamento de Pedido.
 */
data class OrderTrackingState(
    val order: Order? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * UI Intents (Eventos) para a tela de Acompanhamento de Pedido.
 */
sealed class OrderTrackingEvent {
    data class OnTrackOrder(val orderId: String) : OrderTrackingEvent()
    object OnCopyOrderIdClicked : OrderTrackingEvent()
    object OnBackClicked : OrderTrackingEvent()
}
