package com.itbenevides.genesys21.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Order(
    val id: String,
    val userId: String, // O dono da página que recebeu o pedido
    val customerName: String? = null,
    val items: List<CartItem>,
    val total: Double,
    val status: OrderStatus = OrderStatus.PENDING,
    val createdAt: Long,
    val whatsappContact: String? = null
)

@Serializable
enum class OrderStatus {
    PENDING, PROCESSING, COMPLETED, CANCELLED
}
