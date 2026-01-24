package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.data.database.OrdersTable
import com.itbenevides.genesys21.data.database.DatabaseFactory.dbQuery
import com.itbenevides.genesys21.domain.model.Order
import com.itbenevides.genesys21.domain.model.OrderStatus
import com.itbenevides.genesys21.domain.model.PageThemeConfig
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class SqliteOrderRepository {
    suspend fun getOrders(userId: String): List<Order> = dbQuery {
        OrdersTable.selectAll().where { OrdersTable.userId eq userId }
            .orderBy(OrdersTable.createdAt to SortOrder.DESC)
            .map { rowToOrder(it) }
    }

    suspend fun getCustomerOrders(customerId: String): List<Order> = dbQuery {
        OrdersTable.selectAll().where { OrdersTable.customerId eq customerId }
            .orderBy(OrdersTable.createdAt to SortOrder.DESC)
            .map { rowToOrder(it) }
    }

    suspend fun getOrderById(id: String): Order? = dbQuery {
        OrdersTable.selectAll().where { OrdersTable.id eq id }
            .map { rowToOrder(it) }
            .singleOrNull()
    }

    suspend fun saveOrder(order: Order) = dbQuery {
        OrdersTable.insert {
            it[id] = order.id
            it[userId] = order.userId
            it[customerId] = order.customerId
            it[customerName] = order.customerName
            it[items] = order.items
            it[total] = order.total
            it[status] = order.status.name
            it[createdAt] = order.createdAt
            it[whatsappContact] = order.whatsappContact
            it[theme] = order.theme.name // SALVANDO O TEMA
        }
    }

    suspend fun updateOrderStatus(orderId: String, status: OrderStatus) = dbQuery {
        OrdersTable.update({ OrdersTable.id eq orderId }) {
            it[OrdersTable.status] = status.name
        }
    }

    private fun rowToOrder(row: ResultRow) = Order(
        id = row[OrdersTable.id],
        userId = row[OrdersTable.userId],
        customerId = row[OrdersTable.customerId],
        customerName = row[OrdersTable.customerName],
        items = row[OrdersTable.items],
        total = row[OrdersTable.total],
        status = OrderStatus.valueOf(row[OrdersTable.status]),
        createdAt = row[OrdersTable.createdAt],
        whatsappContact = row[OrdersTable.whatsappContact],
        theme = PageThemeConfig.valueOf(row[OrdersTable.theme]) // CARREGANDO O TEMA
    )
}
