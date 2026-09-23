package com.itbenevides.genesys21.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DomainMapping(
    /** UUID */
    val id: String,
    /** e.g. "my-custom-site.com" */
    val domain: String,
    /** Page.id */
    val targetPageId: String,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
)
