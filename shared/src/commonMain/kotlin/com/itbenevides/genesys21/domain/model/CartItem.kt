package com.itbenevides.genesys21.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CartItem(
    val product: Product? = null,
    val quantity: Int = 1,
    val service: BookingService? = null,
    val appointment: Appointment? = null,
) {
    val price: Double get() = product?.price ?: service?.price ?: 0.0
    val name: String get() = product?.name ?: service?.name ?: "Desconhecido"
}
