package com.itbenevides.genesys21.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class UserPermission {
    MANAGE_VITRINES,
    MANAGE_ORDERS,
    MANAGE_AGENDA,
    MANAGE_SERVICES,
    MANAGE_STORE,
    MANAGE_RECEIPTS,
    ACCESS_ADMIN_PANEL
}
