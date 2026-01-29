package com.itbenevides.genesys21.data.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.ReferenceOption

object CartsTable : Table("carts") {
    val userId = varchar("user_id", 100)
    override val primaryKey = PrimaryKey(userId)
}

object CartItemsTable : Table("cart_items") {
    val id = integer("id").autoIncrement()
    val userId = varchar("user_id", 100).references(CartsTable.userId, onDelete = ReferenceOption.CASCADE)
    val productId = varchar("product_id", 50)
    val quantity = integer("quantity")

    override val primaryKey = PrimaryKey(id)
}
