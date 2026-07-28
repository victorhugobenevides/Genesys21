package com.itbenevides.genesys21.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Address(
    val id: String = "",
    val userId: String? = null,
    val street: String,
    val number: String,
    val complement: String? = null,
    val neighborhood: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val isDefault: Boolean = false
)

@Serializable
data class ShippingOption(
    val id: String,
    val name: String,
    val price: Double,
    val estimatedDays: Int
)
