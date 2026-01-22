package com.itbenevides.genesys21.data.database

import com.itbenevides.genesys21.domain.model.CartItem
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.json.json

object CartsTable : Table("carts") {
    val userId = varchar("user_id", 100)
    val items = json<List<CartItem>>("items", Json { ignoreUnknownKeys = true })

    override val primaryKey = PrimaryKey(userId)
}
