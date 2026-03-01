package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.data.database.*
import com.itbenevides.genesys21.data.database.DatabaseFactory.dbQuery
import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus

class SqliteOrderRepository : OrderRepository {

    override suspend fun createMercadoPagoCheckout(order: Order, token: String): Result<String> {
        return Result.failure(UnsupportedOperationException("Operação não suportada no servidor"))
    }

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

    override suspend fun getOrderById(orderId: String): Result<Order> = runCatching {
        dbQuery {
            OrdersTable.selectAll().where { OrdersTable.id eq orderId }
                .map { row ->
                    val items = fetchOrderItems(orderId)
                    row.toOrder(items)
                }
                .singleOrNull() ?: throw Exception("Pedido não encontrado")
        }
    }

    override suspend fun getCustomerOrders(sessionId: String): Result<List<Order>> = runCatching {
        dbQuery {
            OrdersTable.selectAll().where { OrdersTable.customerId eq sessionId }
                .orderBy(OrdersTable.createdAt to SortOrder.DESC)
                .map { row ->
                    val orderId = row[OrdersTable.id]
                    val items = fetchOrderItems(orderId)
                    row.toOrder(items)
                }
        }
    }

    override suspend fun createOrder(order: Order): Result<String> = runCatching {
        dbQuery {
            // Verifica se pedido já existe para evitar duplicidade
            val exists = OrdersTable.selectAll().where { OrdersTable.id eq order.id }.count() > 0
            if (exists) return@dbQuery order.id

            // Garante que o pedido tenha uma data de criação válida (timestamp atual em ms)
            val timestamp = if (order.createdAt <= 0) System.currentTimeMillis() else order.createdAt

            OrdersTable.insert {
                it[id] = order.id
                it[userId] = order.userId
                it[customerId] = order.customerId
                it[customerName] = order.customerName
                it[customerPhone] = order.customerPhone
                it[total] = order.total
                it[status] = order.status.name
                it[createdAt] = timestamp
                it[whatsappContact] = order.whatsappContact
                it[theme] = order.theme.name
            }

            order.items.forEach { item ->
                OrderItemsTable.insert {
                    it[orderId] = order.id
                    it[productId] = item.product.id
                    it[productName] = item.product.name
                    it[productPrice] = item.product.price
                    it[quantity] = item.quantity
                }

                // Deduz do estoque
                ProductsTable.update({ ProductsTable.id eq item.product.id }) {
                    with(SqlExpressionBuilder) {
                        it.update(stock, stock - item.quantity)
                    }
                }
            }
            order.id
        }
    }

    override suspend fun updateOrderStatus(token: String, orderId: String, status: OrderStatus): Result<Unit> = runCatching {
        dbQuery {
            val oldStatusName = OrdersTable.selectAll().where { OrdersTable.id eq orderId }
                .map { it[OrdersTable.status] }.singleOrNull()
            
            val updated = OrdersTable.update({ (OrdersTable.id eq orderId) and (OrdersTable.userId eq token) }) {
                it[this.status] = status.name
            }
            
            if (updated > 0 && oldStatusName != null) {
                handleStockOnStatusChange(orderId, OrderStatus.valueOf(oldStatusName), status)
            }
        }
    }

    suspend fun updateOrderStatusForWebhook(orderId: String, newStatus: OrderStatus): Result<Unit> = runCatching {
        dbQuery {
            val orderRow = OrdersTable.selectAll().where { OrdersTable.id eq orderId }.singleOrNull() 
                ?: throw Exception("Pedido não encontrado")
            
            val oldStatus = OrderStatus.valueOf(orderRow[OrdersTable.status])
            
            if (oldStatus != newStatus) {
                OrdersTable.update({ OrdersTable.id eq orderId }) {
                    it[status] = newStatus.name
                }
                handleStockOnStatusChange(orderId, oldStatus, newStatus)
            }
        }
    }

    private fun handleStockOnStatusChange(orderId: String, oldStatus: OrderStatus, newStatus: OrderStatus) {
        // Se o pedido for cancelado ou falhar, devolvemos os itens ao estoque
        val isFailureStatus = newStatus == OrderStatus.CANCELLED || newStatus == OrderStatus.FAILED
        val wasFailureStatus = oldStatus == OrderStatus.CANCELLED || oldStatus == OrderStatus.FAILED

        if (isFailureStatus && !wasFailureStatus) {
            // Devolver estoque
            val items = fetchOrderItems(orderId)
            items.forEach { item ->
                ProductsTable.update({ ProductsTable.id eq item.product.id }) {
                    with(SqlExpressionBuilder) {
                        it.update(stock, stock + item.quantity)
                    }
                }
            }
        } else if (!isFailureStatus && wasFailureStatus) {
            // Re-deduzir estoque se um pedido falho for reativado
            val items = fetchOrderItems(orderId)
            items.forEach { item ->
                ProductsTable.update({ ProductsTable.id eq item.product.id }) {
                    with(SqlExpressionBuilder) {
                        it.update(stock, stock - item.quantity)
                    }
                }
            }
        }
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
