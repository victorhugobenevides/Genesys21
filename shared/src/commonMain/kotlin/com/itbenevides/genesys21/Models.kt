package com.itbenevides.genesys21

import kotlinx.serialization.Serializable

@Serializable
data class Page(
    val id: String,
    val title: String
)
