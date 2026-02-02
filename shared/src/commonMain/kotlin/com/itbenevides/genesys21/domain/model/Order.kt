package com.itbenevides.genesys21.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Order(
    val id: String,
    val userId: String, // O dono da página (Lojista)
    val customerId: String? = null, // ID da sessão do visitante
    val customerName: String? = null,
    val customerPhone: String? = null, // ADICIONADO: Telefone do cliente
    val items: List<CartItem>,
    val total: Double,
    val status: OrderStatus = OrderStatus.PENDING,
    val createdAt: Long,
    val whatsappContact: String? = null, // WhatsApp da LOJA
    val theme: PageThemeConfig = PageThemeConfig.ROYAL
)

@Serializable
enum class OrderStatus {
    PENDING, PROCESSING, COMPLETED, CANCELLED
}
