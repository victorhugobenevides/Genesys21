package com.itbenevides.genesys21.data.database

import org.jetbrains.exposed.sql.ReferenceOption

object AuditLogsTable : BaseTable("audit_logs") {
    val id = varchar("id", 50) // UUID
    val userId = varchar("user_id", 100).references(UsersTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val storeId = varchar("store_id", 50).references(StoresTable.id, onDelete = ReferenceOption.CASCADE).nullable()
    val action = varchar("action", 100)
    val entityName = varchar("entity_name", 50)
    val entityId = varchar("entity_id", 50)
    val details = text("details").nullable()
    val ipAddress = varchar("ip_address", 45).nullable()

    override val primaryKey = PrimaryKey(id)
}
