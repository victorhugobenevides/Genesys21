package com.itbenevides.genesys21.domain.repository

import com.itbenevides.genesys21.domain.model.Order
import com.itbenevides.genesys21.domain.model.OrderStatus
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    // Para o Cliente (App)
    suspend fun createOrder(order: Order): Result<String>
    suspend fun createMercadoPagoCheckout(order: Order, token: String): Result<String>
    suspend fun getOrderById(orderId: String): Result<Order>
    suspend fun getCustomerOrders(sessionId: String): Result<List<Order>>

    // Para o Lojista (Admin no App)
    fun getOrders(token: String): Flow<List<Order>>
    suspend fun updateOrderStatus(token: String, orderId: String, status: OrderStatus): Result<Unit>
}
