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
    val customDomain: String? = null,
    val theme: PageThemeConfig = PageThemeConfig.ROYAL,
    val customTheme: CustomThemeConfig? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val deletedAt: Long? = null
)
