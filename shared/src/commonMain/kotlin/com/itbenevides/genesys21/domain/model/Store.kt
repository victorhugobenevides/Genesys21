package com.itbenevides.genesys21.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Store(
    val id: String, // UUID
    val ownerId: String, // UserProfile.id
    val name: String,
    val description: String? = null,
    val logoUrl: String? = null,
    val whatsapp: String? = null,
    val originZipCode: String? = null,
    val originStreet: String? = null,
    val originNumber: String? = null,
    val originNeighborhood: String? = null,
    val originCity: String? = null,
    val originState: String? = null,
    val allowPayOnLocation: Boolean = true,
    val allowPayInApp: Boolean = true,
    val allowPickup: Boolean = true,
    val allowDelivery: Boolean = true,
    val stripePublicKey: String? = null,
    val stripeSecretKey: String? = null,
    val stripeAccountId: String? = null, // ID da conta Stripe Connect
    val asaasApiKey: String? = null,
    val paymentGateway: String = "STRIPE", // "STRIPE" or "ASAAS"
    val customDomain: String? = null,
    val theme: PageThemeConfig = PageThemeConfig.ELEGANCE,
    val customTheme: CustomThemeConfig? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val deletedAt: Long? = null
)
