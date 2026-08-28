package com.itbenevides.genesys21.domain.repository

import com.itbenevides.genesys21.domain.model.Order
import com.itbenevides.genesys21.domain.model.OrderResponse
import com.itbenevides.genesys21.domain.model.OrderStatus
import com.itbenevides.genesys21.domain.model.MerchantAnalytics
import com.itbenevides.genesys21.domain.model.B2BAnalytics
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun getOrders(token: String): Flow<List<Order>>

    suspend fun createOrder(order: Order): Result<OrderResponse>

    suspend fun updateOrderStatus(
        token: String,
        orderId: String,
        status: OrderStatus,
    ): Result<Unit>

    suspend fun getOrderById(orderId: String): Result<Order>

    suspend fun getCustomerOrders(sessionId: String): Result<List<Order>>

    suspend fun getAnalytics(token: String): Result<MerchantAnalytics>

    suspend fun getB2BAnalytics(token: String): Result<B2BAnalytics>

    suspend fun getAuditLogs(token: String): Result<List<Map<String, String>>>
}
