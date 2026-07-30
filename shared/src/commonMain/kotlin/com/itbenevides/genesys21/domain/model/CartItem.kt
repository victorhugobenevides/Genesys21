package com.itbenevides.genesys21.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CartItem(
    val product: Product? = null,
    val quantity: Int = 1,
    val service: BookingService? = null,
    val appointment: Appointment? = null,
    val customName: String? = null,
    val customPrice: Double? = null,
) {
    val price: Double get() = customPrice ?: product?.price ?: service?.price ?: 0.0
    val name: String get() = customName ?: product?.name ?: service?.name ?: "Desconhecido"
}
