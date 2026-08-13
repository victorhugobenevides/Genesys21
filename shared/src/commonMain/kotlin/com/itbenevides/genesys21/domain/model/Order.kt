package com.itbenevides.genesys21.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class PaymentMethod {
    LOCAL,
    APP
}

@Serializable
data class Order(
    val id: String, // UUID
    val storeId: String, // Store.id
    val customerId: String? = null, // UserProfile.id
    val sessionId: String? = null, // Temporary session for visitors
    val customerName: String? = null,
    val customerPhone: String? = null,
    val items: List<CartItem>,
    val total: Double,
    val status: OrderStatus = OrderStatus.PENDING,
    val paymentMethod: PaymentMethod = PaymentMethod.LOCAL,
    val shippingAddress: Address? = null,
    val shippingPrice: Double = 0.0,
    val shippingMethod: String? = null,
    val whatsappContact: String? = null,
    val theme: PageThemeConfig = PageThemeConfig.ELEGANCE,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val deletedAt: Long? = null
)

@Serializable
enum class OrderStatus {
    PENDING,
    AWAITING_PAYMENT,
    PROCESSING,
    COMPLETED,
    CANCELLED,
}
