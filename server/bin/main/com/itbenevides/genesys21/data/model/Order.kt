package com.itbenevides.genesys21.data.model

/**
 * Represents the current status of an order.
 */
enum class OrderStatus {
    CREATED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED
}

/**
 * Entry of the status history for an order.
 * Stores the status, timestamp and an optional comment.
 */
 data class OrderStatusEntry(
    val status: OrderStatus,
    val timestamp: Long = System.currentTimeMillis(),
    val comment: String? = null
)

/**
 * Main order entity.
 *
 * @property id Unique identifier for the order.
 * @property userId Identifier of the user who placed the order.
 * @property items List of item identifiers (could be product IDs).
 * @property createdAt Timestamp when the order was created.
 * @property status Current status of the order.
 * @property history List of status changes.
 */
 data class Order(
    val id: String,
    val userId: String,
    val items: List<String>,
    val createdAt: Long = System.currentTimeMillis(),
    var status: OrderStatus = OrderStatus.CREATED,
    val history: MutableList<OrderStatusEntry> = mutableListOf(OrderStatusEntry(OrderStatus.CREATED))
) {
    /**
     * Updates the order status and records the change in the history.
     */
    fun updateStatus(newStatus: OrderStatus, comment: String? = null) {
        status = newStatus
        history.add(OrderStatusEntry(newStatus, comment = comment))
    }
}
