package com.itbenevides.genesys21.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    /** UUID */
    val id: String,
    /** Store.id */
    val storeId: String,
    val name: String,
    /** Multi-level support */
    val parentId: String? = null,
    val icon: String? = null,
    val color: String? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val deletedAt: Long? = null,
)
