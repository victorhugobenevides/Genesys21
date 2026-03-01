package com.itbenevides.genesys21.mocks

import com.itbenevides.genesys21.domain.model.Order
import com.itbenevides.genesys21.domain.model.OrderStatus
import com.itbenevides.genesys21.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeOrderRepository : OrderRepository {
    private val orders = mutableListOf<Order>()
    private val ordersFlow = MutableStateFlow<List<Order>>(emptyList())

    override suspend fun createOrder(order: Order): Result<String> {
        orders.add(order)
        updateFlow()
        return Result.success(order.id)
    }

    override suspend fun createMercadoPagoCheckout(order: Order, token: String): Result<String> {
        return Result.success("mp_checkout_url_${order.id}")
    }

    override suspend fun getOrderById(orderId: String): Result<Order> {
        return orders.find { it.id == orderId }
            ?.let { Result.success(it) }
            ?: Result.failure(Exception("Order not found"))
    }

    override suspend fun getCustomerOrders(sessionId: String): Result<List<Order>> {
        return Result.success(orders.filter { it.customerId == sessionId })
    }

    override fun getOrders(token: String): Flow<List<Order>> {
        return ordersFlow.asStateFlow()
    }

    override suspend fun updateOrderStatus(token: String, orderId: String, status: OrderStatus): Result<Unit> {
        val index = orders.indexOfFirst { it.id == orderId }
        return if (index != -1) {
            orders[index] = orders[index].copy(status = status)
            updateFlow()
            Result.success(Unit)
        } else {
            Result.failure(Exception("Order not found"))
        }
    }

    // Helper para os testes
    fun clear() {
        orders.clear()
        updateFlow()
    }

    fun addOrder(order: Order) {
        orders.add(order)
        updateFlow()
    }

    private fun updateFlow() {
        ordersFlow.value = orders.toList()
    }
}
