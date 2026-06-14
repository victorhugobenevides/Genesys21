package com.itbenevides.genesys21.domain.usecase

import com.itbenevides.genesys21.domain.model.Order
import com.itbenevides.genesys21.domain.repository.OrderRepository

class GetCustomerOrdersUseCase(private val orderRepository: OrderRepository) {
    suspend operator fun invoke(sessionId: String): Result<List<Order>> {
        return orderRepository.getCustomerOrders(sessionId)
    }
}
