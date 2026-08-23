package com.itbenevides.genesys21.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DomainMapping(
    val id: String, // UUID
    val domain: String, // e.g. "my-custom-site.com"
    val targetPageId: String, // Page.id
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)
