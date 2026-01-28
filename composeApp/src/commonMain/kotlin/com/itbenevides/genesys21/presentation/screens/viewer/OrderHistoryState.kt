package com.itbenevides.genesys21.presentation.screens.viewer

import com.itbenevides.genesys21.domain.model.Order

/**
 * UI State para a tela de Histórico de Pedidos do Cliente.
 */
data class OrderHistoryState(
    val orders: List<Order> = emptyList(),
    val isLoading: Boolean = false
)

/**
 * UI Intents (Eventos) para a tela de Histórico de Pedidos.
 */
sealed class OrderHistoryEvent {
    object OnBackClicked : OrderHistoryEvent()
    data class OnOrderClicked(val order: Order) : OrderHistoryEvent()
}
