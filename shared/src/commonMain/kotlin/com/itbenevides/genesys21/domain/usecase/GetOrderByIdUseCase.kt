package com.itbenevides.genesys21.domain.usecase

import com.itbenevides.genesys21.domain.model.Order
import com.itbenevides.genesys21.domain.repository.OrderRepository

class GetOrderByIdUseCase(private val orderRepository: OrderRepository) {
    suspend operator fun invoke(orderId: String): Result<Order> {
        return orderRepository.getOrderById(orderId)
    }
}