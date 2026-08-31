package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.data.database.*
import com.itbenevides.genesys21.data.database.DatabaseFactory.dbQuery
import com.itbenevides.genesys21.domain.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class SqliteCartRepository {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getCart(userId: String): List<CartItem> =
        dbQuery {
            // 1. Garantir que o carrinho mestre exista
            if (CartsTable.selectAll().where { CartsTable.userId eq userId }.count() == 0L) {
                CartsTable.insert { it[CartsTable.userId] = userId }
                return@dbQuery emptyList<CartItem>()
            }

            // 2. Busca com tratamento de nulos para produtos/serviços que podem ter sido removidos
            CartItemsTable
                .selectAll().where { CartItemsTable.userId eq userId }
                .mapNotNull { row ->
                    try {
                        val productId = row[CartItemsTable.productId]
                        val serviceId = row[CartItemsTable.serviceId]

                        val product = if (productId != null) fetchProduct(productId) else null
                        val service = if (serviceId != null) fetchService(serviceId) else null

                        // Se era um produto/serviço e não existe mais no catálogo, ignoramos para não dar erro 500
                        if (productId != null && product == null) return@mapNotNull null
                        if (serviceId != null && service == null) return@mapNotNull null

                        CartItem(
                            product = product,
                            service = service,
                            appointment = row[CartItemsTable.appointmentData]?.let {
                                try { json.decodeFromString<Appointment>(it) } catch (e: Exception) { null }
                            },
                            quantity = row[CartItemsTable.quantity]
                        )
                    } catch (e: Exception) {
                        println("CART_REPO: Erro ao processar item do carrinho para usuário $userId: ${e.message}")
                        null
                    }
                }
        }

    private fun fetchProduct(id: String): Product? {
        return ProductsTable.selectAll().where { ProductsTable.id eq id }
            .map { row ->
                Product(
                    id = row[ProductsTable.id],
                    storeId = row[ProductsTable.storeId],
                    name = row[ProductsTable.name],
                    price = row[ProductsTable.price]
                )
            }.singleOrNull()
    }

    private fun fetchService(id: String): BookingService? {
        return BookingServicesTable.selectAll().where { BookingServicesTable.id eq id }
            .map { row ->
                BookingService(
                    id = row[BookingServicesTable.id],
                    storeId = row[BookingServicesTable.storeId],
                    name = row[BookingServicesTable.name],
                    price = row[BookingServicesTable.price],
                    durationMinutes = row[BookingServicesTable.durationMinutes]
                )
            }.singleOrNull()
    }

    suspend fun saveCart(
        userId: String,
        items: List<CartItem>,
    ) = dbQuery {
        if (CartsTable.selectAll().where { CartsTable.userId eq userId }.count() == 0L) {
            CartsTable.insert { it[CartsTable.userId] = userId }
        }

        CartItemsTable.deleteWhere { CartItemsTable.userId eq userId }

        items.forEach { item ->
            CartItemsTable.insert {
                it[id] = java.util.UUID.randomUUID().toString()
                it[CartItemsTable.userId] = userId
                it[productId] = item.product?.id
                it[serviceId] = item.service?.id
                it[appointmentData] = item.appointment?.let { appt -> json.encodeToString(appt) }
                it[quantity] = item.quantity
            }
        }
    }
}
