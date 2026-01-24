package com.itbenevides.genesys21.domain.repository

import com.itbenevides.genesys21.domain.model.Order
import com.itbenevides.genesys21.domain.model.OrderStatus
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun getOrders(token: String): Flow<List<Order>>
    suspend fun createOrder(order: Order): Result<Unit>
    suspend fun updateOrderStatus(token: String, orderId: String, status: OrderStatus): Result<Unit>
    suspend fun getOrderById(orderId: String): Result<Order>
    suspend fun getCustomerOrders(sessionId: String): Result<List<Order>>
}
