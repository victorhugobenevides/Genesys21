package com.itbenevides.genesys21.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Order(
    val id: String, // UUID
    val storeId: String, // Store.id
    val customerId: String? = null, // UserProfile.id
    val customerName: String? = null,
    val customerPhone: String? = null,
    val items: List<CartItem>,
    val total: Double,
    val status: OrderStatus = OrderStatus.PENDING,
    val whatsappContact: String? = null,
    val theme: PageThemeConfig = PageThemeConfig.ROYAL,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val deletedAt: Long? = null
)

@Serializable
enum class OrderStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    CANCELLED,
}
