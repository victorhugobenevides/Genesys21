package com.itbenevides.genesys21.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class OrderResponse(
    val orderId: String,
    val checkoutUrl: String? = null
)
