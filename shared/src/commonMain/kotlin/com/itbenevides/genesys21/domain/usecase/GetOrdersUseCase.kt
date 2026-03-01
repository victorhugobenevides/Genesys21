package com.itbenevides.genesys21.domain.usecase

import com.itbenevides.genesys21.domain.model.Order
import com.itbenevides.genesys21.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow

class GetOrdersUseCase(private val orderRepository: OrderRepository) {
    operator fun invoke(token: String): Flow<List<Order>> {
        return orderRepository.getOrders(token)
    }
}
