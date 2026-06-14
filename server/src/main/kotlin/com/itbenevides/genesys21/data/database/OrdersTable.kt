package com.itbenevides.genesys21.data.database

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object OrdersTable : Table("orders") {
    val id = varchar("id", 50)
    val userId = varchar("user_id", 100)
    val customerId = varchar("customer_id", 100).nullable()
    val customerName = varchar("customer_name", 255).nullable()
    val customerPhone = varchar("customer_phone", 50).nullable() // ADICIONADO
    val total = double("total")
    val status = varchar("status", 50)
    val createdAt = long("created_at")
    val whatsappContact = varchar("whatsapp_contact", 50).nullable()
    val theme = varchar("theme", 50).default("ROYAL")

    override val primaryKey = PrimaryKey(id)
}

object OrderItemsTable : Table("order_items") {
    val id = integer("id").autoIncrement()
    val orderId = varchar("order_id", 50).references(OrdersTable.id, onDelete = ReferenceOption.CASCADE)
    val productId = varchar("product_id", 50)
    val productName = varchar("product_name", 255)
    val productPrice = double("product_price")
    val quantity = integer("quantity")

    override val primaryKey = PrimaryKey(id)
}
