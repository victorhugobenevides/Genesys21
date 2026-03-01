package com.itbenevides.genesys21.domain.usecase

import com.itbenevides.genesys21.domain.model.OrderStatus
import com.itbenevides.genesys21.domain.repository.OrderRepository

class UpdateOrderStatusUseCase(private val orderRepository: OrderRepository) {
    suspend operator fun invoke(token: String, orderId: String, status: OrderStatus): Result<Unit> {
        return orderRepository.updateOrderStatus(token, orderId, status)
    }
}
