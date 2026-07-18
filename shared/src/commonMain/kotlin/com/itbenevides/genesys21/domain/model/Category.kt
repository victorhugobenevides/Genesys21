package com.itbenevides.genesys21.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: String, // UUID
    val storeId: String, // Store.id
    val name: String,
    val parentId: String? = null, // Multi-level support
    val icon: String? = null,
    val color: String? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val deletedAt: Long? = null
)
