package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.data.database.CartsTable
import com.itbenevides.genesys21.data.database.DatabaseFactory.dbQuery
import com.itbenevides.genesys21.domain.model.CartItem
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class SqliteCartRepository {
    suspend fun getCart(userId: String): List<CartItem> = dbQuery {
        CartsTable.selectAll().where { CartsTable.userId eq userId }
            .map { it[CartsTable.items] }
            .firstOrNull() ?: emptyList()
    }

    suspend fun saveCart(userId: String, items: List<CartItem>) = dbQuery {
        val exists = CartsTable.selectAll().where { CartsTable.userId eq userId }.count() > 0
        if (exists) {
            CartsTable.update({ CartsTable.userId eq userId }) {
                it[CartsTable.items] = items
            }
        } else {
            CartsTable.insert {
                it[CartsTable.userId] = userId
                it[CartsTable.items] = items
            }
        }
    }
}
