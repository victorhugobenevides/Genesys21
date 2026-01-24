package com.itbenevides.genesys21.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Order(
    val id: String,
    val userId: String, // O dono da página (Lojista)
    val customerId: String? = null, // ID da sessão do visitante
    val customerName: String? = null,
    val items: List<CartItem>,
    val total: Double,
    val status: OrderStatus = OrderStatus.PENDING,
    val createdAt: Long,
    val whatsappContact: String? = null,
    val theme: PageThemeConfig = PageThemeConfig.ROYAL // ADICIONADO: Persiste o tema no pedido
)

@Serializable
enum class OrderStatus {
    PENDING, PROCESSING, COMPLETED, CANCELLED
}
