package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.data.database.*
import com.itbenevides.genesys21.data.database.DatabaseFactory.dbQuery
import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus
import org.jetbrains.exposed.sql.transactions.transaction

class SqliteOrderRepository : OrderRepository {

    override fun getOrders(token: String): Flow<List<Order>> = flow {
        val orders = dbQuery {
            OrdersTable.selectAll().where { OrdersTable.userId eq token }
                .orderBy(OrdersTable.createdAt to SortOrder.DESC)
                .map { row ->
                    val orderId = row[OrdersTable.id]
                    val items = fetchOrderItems(orderId)
                    row.toOrder(items)
                }
        }
        emit(orders)
    }

    override suspend fun getOrderById(orderId: String): Result<Order> = try {
        dbQuery {
            OrdersTable.selectAll().where { OrdersTable.id eq orderId }
                .map { row ->
                    val items = fetchOrderItems(orderId)
                    row.toOrder(items)
                }
                .singleOrNull()?.let { Result.success(it) }
                ?: Result.failure(Exception("Pedido não encontrado"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getCustomerOrders(sessionId: String): Result<List<Order>> = try {
        dbQuery {
            val orders = OrdersTable.selectAll().where { OrdersTable.customerId eq sessionId }
                .orderBy(OrdersTable.createdAt to SortOrder.DESC)
                .map { row ->
                    val orderId = row[OrdersTable.id]
                    val items = fetchOrderItems(orderId)
                    row.toOrder(items)
                }
            Result.success(orders)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun createOrder(order: Order): Result<Unit> = try {
        dbQuery {
            // 1. Inserir cabeçalho do pedido
            OrdersTable.insert {
                it[id] = order.id
                it[userId] = order.userId
                it[customerId] = order.customerId
                it[customerName] = order.customerName
                it[customerPhone] = order.customerPhone
                it[total] = order.total
                it[status] = order.status.name
                it[createdAt] = order.createdAt
                it[whatsappContact] = order.whatsappContact
                it[theme] = order.theme.name
            }

            // 2. Inserir itens e atualizar estoque
            order.items.forEach { item ->
                // Salva o item no histórico do pedido
                OrderItemsTable.insert {
                    it[orderId] = order.id
                    it[productId] = item.product.id
                    it[productName] = item.product.name
                    it[productPrice] = item.product.price
                    it[quantity] = item.quantity
                }

                // CONTROLE DE ESTOQUE: Diminui a quantidade disponível
                ProductsTable.update({ ProductsTable.id eq item.product.id }) {
                    it.update(stock, stock minus item.quantity)
                }
            }
            
            Result.success(Unit)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Result.failure(e)
    }

    override suspend fun updateOrderStatus(token: String, orderId: String, status: OrderStatus): Result<Unit> = try {
        dbQuery {
            val updated = OrdersTable.update({ (OrdersTable.id eq orderId) and (OrdersTable.userId eq token) }) {
                it[this.status] = status.name
            }
            if (updated > 0) Result.success(Unit) else Result.failure(Exception("Acesso negado ou pedido inexistente"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    private fun fetchOrderItems(orderId: String): List<CartItem> {
        return OrderItemsTable.selectAll().where { OrderItemsTable.orderId eq orderId }
            .map { row ->
                CartItem(
                    product = Product(
                        id = row[OrderItemsTable.productId],
                        name = row[OrderItemsTable.productName],
                        price = row[OrderItemsTable.productPrice]
                    ),
                    quantity = row[OrderItemsTable.quantity]
                )
            }
    }

    private fun ResultRow.toOrder(items: List<CartItem>) = Order(
        id = this[OrdersTable.id],
        userId = this[OrdersTable.userId],
        customerId = this[OrdersTable.customerId],
        customerName = this[OrdersTable.customerName],
        customerPhone = this[OrdersTable.customerPhone],
        items = items,
        total = this[OrdersTable.total],
        status = try { OrderStatus.valueOf(this[OrdersTable.status]) } catch (e: Exception) { OrderStatus.PENDING },
        createdAt = this[OrdersTable.createdAt],
        whatsappContact = this[OrdersTable.whatsappContact],
        theme = try { PageThemeConfig.valueOf(this[OrdersTable.theme]) } catch (e: Exception) { PageThemeConfig.ROYAL }
    )
}
