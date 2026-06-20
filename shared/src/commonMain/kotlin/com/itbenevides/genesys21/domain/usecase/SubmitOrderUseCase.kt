package com.itbenevides.genesys21.domain.usecase

import com.itbenevides.genesys21.domain.model.Order
import com.itbenevides.genesys21.domain.repository.OrderRepository

class SubmitOrderUseCase(private val orderRepository: OrderRepository) {
    suspend operator fun invoke(order: Order): Result<Unit> {
        return orderRepository.createOrder(order)
    }
}
