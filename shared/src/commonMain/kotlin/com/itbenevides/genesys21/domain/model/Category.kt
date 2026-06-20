package com.itbenevides.genesys21.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: Int? = null,
    val ownerId: String,
    val name: String,
    val icon: String? = null,
    val color: String? = null,
)
