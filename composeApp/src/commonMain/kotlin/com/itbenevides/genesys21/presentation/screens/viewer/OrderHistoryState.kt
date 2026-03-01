package com.itbenevides.genesys21.presentation.screens.viewer

import com.itbenevides.genesys21.domain.model.Order

data class OrderHistoryState(
    val orders: List<Order> = emptyList(),
    val isLoading: Boolean = false
)

sealed class OrderHistoryEvent {
    data class OnOrderClicked(val order: Order) : OrderHistoryEvent()
    object OnBackClicked : OrderHistoryEvent()
}
