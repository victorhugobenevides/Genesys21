package com.itbenevides.genesys21.data.database

import org.jetbrains.exposed.sql.ReferenceOption

object CartsTable : BaseTable("carts") {
    val userId = varchar("user_id", 100)
    override val primaryKey = PrimaryKey(userId)
}

object CartItemsTable : BaseTable("cart_items") {
    val id = varchar("id", 50) // UUID
    val userId = varchar("user_id", 100).references(CartsTable.userId, onDelete = ReferenceOption.CASCADE)
    val productId = varchar("product_id", 50).nullable()
    val serviceId = varchar("service_id", 50).nullable()
    val appointmentData = text("appointment_data").nullable()
    val quantity = integer("quantity")

    override val primaryKey = PrimaryKey(id)
}
