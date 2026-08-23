package com.itbenevides.genesys21.data.database

import org.jetbrains.exposed.sql.ReferenceOption

object OrderStatusLogsTable : BaseTable("order_status_logs") {
    val id = varchar("id", 50) // UUID
    val orderId = varchar("order_id", 50).references(OrdersTable.id, onDelete = ReferenceOption.CASCADE)
    val oldStatus = varchar("old_status", 50).nullable()
    val newStatus = varchar("new_status", 50)
    val timestamp = long("timestamp")
    val note = varchar("note", 255).nullable()

    override val primaryKey = PrimaryKey(id)
}
