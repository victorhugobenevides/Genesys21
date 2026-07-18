package com.itbenevides.genesys21.data.database

import org.jetbrains.exposed.sql.ReferenceOption

object OrdersTable : BaseTable("orders") {
    val id = varchar("id", 50) // UUID
    val storeId = varchar("store_id", 50).references(StoresTable.id, onDelete = ReferenceOption.RESTRICT)
    val customerId = varchar("customer_id", 100).references(UsersTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val customerName = varchar("customer_name", 255).nullable()
    val customerPhone = varchar("customer_phone", 50).nullable()
    val total = double("total")
    val status = varchar("status", 50)
    val whatsappContact = varchar("whatsapp_contact", 50).nullable()
    val theme = varchar("theme", 50).default("ROYAL")

    override val primaryKey = PrimaryKey(id)
}

object OrderItemsTable : BaseTable("order_items") {
    val id = varchar("id", 50) // UUID
    val orderId = varchar("order_id", 50).references(OrdersTable.id, onDelete = ReferenceOption.CASCADE)
    val productId = varchar("product_id", 50).references(ProductsTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val productName = varchar("product_name", 255)
    val productPrice = double("product_price")
    val quantity = integer("quantity")

    override val primaryKey = PrimaryKey(id)
}
