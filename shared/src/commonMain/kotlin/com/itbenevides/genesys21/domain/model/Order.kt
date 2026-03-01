package com.itbenevides.genesys21.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Order(
    val id: String,
    val userId: String,
    val customerId: String? = null,
    val customerName: String? = null,
    val customerPhone: String? = null,
    val items: List<CartItem>,
    val total: Double,
    val status: OrderStatus = OrderStatus.PENDING,
    val createdAt: Long,
    val whatsappContact: String? = null,
    val theme: PageThemeConfig = PageThemeConfig.ROYAL
)

@Serializable
enum class OrderStatus {
    PENDING, 
    PAYMENT_PENDING, 
    PROCESSING, 
    COMPLETED, 
    CANCELLED, 
    FAILED
}
