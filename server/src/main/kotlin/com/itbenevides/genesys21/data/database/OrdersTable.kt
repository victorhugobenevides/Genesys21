package com.itbenevides.genesys21.data.database

import com.itbenevides.genesys21.domain.model.CartItem
import com.itbenevides.genesys21.domain.model.OrderStatus
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.json.json

object OrdersTable : Table("orders") {
    val id = varchar("id", 50)
    val userId = varchar("user_id", 100) // Dono da loja
    val customerName = varchar("customer_name", 255).nullable()
    val items = json<List<CartItem>>("items", Json { ignoreUnknownKeys = true })
    val total = double("total")
    val status = varchar("status", 50)
    val createdAt = long("created_at")
    val whatsappContact = varchar("whatsapp_contact", 50).nullable()

    override val primaryKey = PrimaryKey(id)
}
