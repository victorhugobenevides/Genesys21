package com.itbenevides.genesys21.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    SUPERADMIN,
    MERCHANT,
    CUSTOMER
}

@Serializable
enum class UserStatus {
    PENDING,
    APPROVED,
    BLOCKED
}

@Serializable
data class UserProfile(
    val id: String, // Firebase UID
    val email: String,
    val name: String,
    val role: UserRole = UserRole.CUSTOMER,
    val status: UserStatus = UserStatus.APPROVED,
    val createdAt: Long = 0
)
