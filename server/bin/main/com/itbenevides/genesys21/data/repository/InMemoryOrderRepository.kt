package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.data.model.Order
import com.itbenevides.genesys21.data.model.OrderStatus

/**
 * Simple in‑memory repository for orders. Suitable for MVP and unit tests.
 */
class InMemoryOrderRepository {
    // Map of orderId -> Order instance
    private val orders = mutableMapOf<String, Order>()

    /** Returns all orders belonging to a given user. */
    fun findByUserId(userId: String): List<Order> =
        orders.values.filter { it.userId == userId }

    /** Returns the order with the given id, or null if not found. */
    fun findById(id: String): Order? = orders[id]

    /** Saves a new order. If an order with the same id exists it will be overwritten. */
    fun save(order: Order) {
        orders[order.id] = order
    }

    /** Updates the status of an existing order. */
    fun updateStatus(
        id: String,
        newStatus: OrderStatus,
    ): Boolean {
        val order = orders[id] ?: return false
        order.updateStatus(newStatus)
        return true
    }

    /** Clears all stored orders – useful for tests. */
    fun clear() = orders.clear()
}
