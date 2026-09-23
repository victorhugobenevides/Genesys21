package com.itbenevides.genesys21.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    /** UUID */
    val id: String,
    /** Store.id */
    val storeId: String,
    val name: String,
    val price: Double,
    val description: String? = null,
    val imageUrls: List<String> = emptyList(),
    /** Refactor to String (UUID) */
    val categoryId: String? = null,
    val categoryName: String? = null,
    val stock: Int = 0,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val deletedAt: Long? = null,
)
