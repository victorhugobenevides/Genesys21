package com.itbenevides.genesys21.domain.usecase

import com.itbenevides.genesys21.domain.model.Order
import com.itbenevides.genesys21.domain.model.OrderResponse
import com.itbenevides.genesys21.domain.repository.OrderRepository

class SubmitOrderUseCase(private val orderRepository: OrderRepository) {
    suspend operator fun invoke(order: Order): Result<OrderResponse> {
        return orderRepository.createOrder(order)
    }
}
