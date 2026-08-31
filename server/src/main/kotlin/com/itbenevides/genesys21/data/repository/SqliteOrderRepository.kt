package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.data.database.*
import com.itbenevides.genesys21.data.database.DatabaseFactory.dbQuery
import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.domain.repository.OrderRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SqliteOrderRepository(
    private val bookingRepository: com.itbenevides.genesys21.domain.repository.BookingRepository
) : OrderRepository {
    override fun getOrders(token: String): Flow<List<Order>> =
        flow {
            val orders = dbQuery {
                (OrdersTable innerJoin StoresTable)
                    .selectAll().where { StoresTable.ownerId eq token }
                    .orderBy(OrdersTable.createdAt to SortOrder.DESC)
                    .map { row ->
                        val orderId = row[OrdersTable.id]
                        val items = fetchOrderItems(orderId, row[OrdersTable.storeId])
                        row.toOrder(items)
                    }
            }
            emit(orders)
        }

    override suspend fun getOrderById(orderId: String): Result<Order> = try {
        dbQuery {
            OrdersTable.selectAll().where { OrdersTable.id eq orderId }
                .map { row ->
                    val items = fetchOrderItems(orderId, row[OrdersTable.storeId])
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
            val orders = OrdersTable.selectAll().where { (OrdersTable.customerId eq sessionId) or (OrdersTable.sessionId eq sessionId) }
                .orderBy(OrdersTable.createdAt to SortOrder.DESC)
                .map { row ->
                    val orderId = row[OrdersTable.id]
                    val items = fetchOrderItems(orderId, row[OrdersTable.storeId])
                    row.toOrder(items)
                }
            Result.success(orders)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun createOrder(order: Order): Result<OrderResponse> = try {
        dbQuery {
            val finalId = order.id.ifBlank { java.util.UUID.randomUUID().toString() }

            // 1. RECALCULAR TOTAL REAL (Zero Trust Logic)
            var calculatedTotal = 0.0
            val officialItemsData = order.items.map { item ->
                val product = item.product
                val service = item.service

                val (name, price) = when {
                    product != null -> {
                        val row = ProductsTable.selectAll().where { ProductsTable.id eq product.id }.singleOrNull()
                        if (row == null) {
                            val count = ProductsTable.selectAll().count()
                            println("PRODUCT NOT FOUND! DB STORE COUNT: $count")
                            throw Exception("Produto ${product.id} inválido")
                        }
                        println("CHECKING DB FOR PRODUCT ${product.id}: FOUND ${row[ProductsTable.name]} PRICE ${row[ProductsTable.price]}")
                        row[ProductsTable.name] to row[ProductsTable.price]
                    }
                    service != null -> {
                        val row = BookingServicesTable.selectAll().where { BookingServicesTable.id eq service.id }.singleOrNull()
                            ?: throw Exception("Serviço ${service.id} inválido")
                        row[BookingServicesTable.name] to row[BookingServicesTable.price]
                    }
                    else -> item.name to item.price
                }
                calculatedTotal += price * item.quantity
                item.copy(customName = name, customPrice = price)
            }

            // Inserir cabeçalho com o total OFICIAL
            OrdersTable.insert {
                it[id] = finalId
                it[storeId] = order.storeId
                it[customerId] = order.customerId
                it[sessionId] = order.sessionId
                it[customerName] = order.customerName
                it[customerPhone] = order.customerPhone
                it[total] = calculatedTotal
                it[status] = order.status.name
                it[paymentMethod] = order.paymentMethod.name
                it[whatsappContact] = order.whatsappContact
                it[theme] = order.theme.name
                it[createdAt] = System.currentTimeMillis()
                it[updatedAt] = System.currentTimeMillis()
            }

            // 2. Inserir itens com dados OFICIAIS
            officialItemsData.forEach { item ->
                OrderItemsTable.insert {
                    it[id] = java.util.UUID.randomUUID().toString()
                    it[orderId] = finalId
                    it[productId] = item.product?.id
                    it[serviceId] = item.service?.id
                    it[productName] = item.name
                    it[productPrice] = item.price
                    it[quantity] = item.quantity
                }

                // Update Stock if product
                item.product?.let { prod ->
                    ProductsTable.update({ ProductsTable.id eq prod.id }) {
                        it.update(stock, stock minus item.quantity)
                    }
                }
            }

            Result.success(OrderResponse(finalId))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateOrderStatus(token: String, orderId: String, status: OrderStatus): Result<Unit> = try {
        dbQuery {
            OrdersTable.update({ OrdersTable.id eq orderId }) {
                it[this.status] = status.name
                it[updatedAt] = System.currentTimeMillis()
            }
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getAnalytics(token: String): Result<MerchantAnalytics> = try {
        dbQuery {
            val storeIds = StoresTable.selectAll().where { StoresTable.ownerId eq token }.map { it[StoresTable.id] }
            if (storeIds.isEmpty()) return@dbQuery Result.failure(Exception("Nenhuma loja encontrada"))

            val allOrders = OrdersTable.selectAll().where { (OrdersTable.storeId inList storeIds) }
            val total = allOrders.sumOf { it[OrdersTable.total] }

            Result.success(
                MerchantAnalytics(
                    dailyRevenue = emptyList(),
                    topProducts = emptyList(),
                    bookingSummary = BookingSummary(0, 0, 0, 0),
                    totalOrders = allOrders.count().toInt(),
                    averageTicket = if (allOrders.count() > 0) total / allOrders.count() else 0.0
                )
            )
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getB2BAnalytics(token: String): Result<B2BAnalytics> = try {
        dbQuery {
            Result.success(B2BAnalytics(0, 0.0, 0.0, emptyList(), emptyList()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getAuditLogs(token: String): Result<List<Map<String, String>>> = try {
        dbQuery { Result.success(emptyList()) }
    } catch (e: Exception) {
        Result.failure(e)
    }

    private fun fetchOrderItems(orderId: String, storeId: String): List<CartItem> {
        return OrderItemsTable.selectAll().where { OrderItemsTable.orderId eq orderId }
            .map { row ->
                val productId = row[OrderItemsTable.productId]
                val serviceId = row[OrderItemsTable.serviceId]
                CartItem(
                    product = productId?.let { Product(it, storeId, row[OrderItemsTable.productName], row[OrderItemsTable.productPrice]) },
                    service = serviceId?.let { BookingService(it, storeId, row[OrderItemsTable.productName], "", row[OrderItemsTable.productPrice], 0) },
                    quantity = row[OrderItemsTable.quantity],
                    customName = row[OrderItemsTable.productName],
                    customPrice = row[OrderItemsTable.productPrice]
                )
            }
    }

    private fun ResultRow.toOrder(items: List<CartItem>) = Order(
        id = this[OrdersTable.id],
        storeId = this[OrdersTable.storeId],
        customerId = this[OrdersTable.customerId],
        sessionId = this[OrdersTable.sessionId],
        customerName = this[OrdersTable.customerName],
        customerPhone = this[OrdersTable.customerPhone],
        items = items,
        total = this[OrdersTable.total],
        status = try { OrderStatus.valueOf(this[OrdersTable.status]) } catch (_: Exception) { OrderStatus.PENDING },
        paymentMethod = try { PaymentMethod.valueOf(this[OrdersTable.paymentMethod]) } catch (_: Exception) { PaymentMethod.LOCAL },
        createdAt = this[OrdersTable.createdAt],
        updatedAt = this[OrdersTable.updatedAt],
        theme = try { PageThemeConfig.valueOf(this[OrdersTable.theme]) } catch (_: Exception) { PageThemeConfig.ELEGANCE }
    )
}
