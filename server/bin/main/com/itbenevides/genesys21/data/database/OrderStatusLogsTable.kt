package com.itbenevides.genesys21.data.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.ReferenceOption

object OrderStatusLogsTable : Table("order_status_logs") {
    val id = integer("id").autoIncrement()
    val orderId = varchar("order_id", 50).references(OrdersTable.id, onDelete = ReferenceOption.CASCADE)
    val oldStatus = varchar("old_status", 50).nullable()
    val newStatus = varchar("new_status", 50)
    val timestamp = long("timestamp")
    val note = varchar("note", 255).nullable()

    override val primaryKey = PrimaryKey(id)
}
