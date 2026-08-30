package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.data.database.*
import com.itbenevides.genesys21.data.database.DatabaseFactory.dbQuery
import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.domain.repository.OrderRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus
import org.jetbrains.exposed.sql.SqlExpressionBuilder.case
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.*

class SqliteOrderRepository(
    private val bookingRepository: com.itbenevides.genesys21.domain.repository.BookingRepository
) : OrderRepository {
    override fun getOrders(token: String): Flow<List<Order>> =
        flow {
            val orders =
                dbQuery {
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

    override suspend fun getOrderById(orderId: String): Result<Order> =
        try {
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

    override suspend fun getCustomerOrders(sessionId: String): Result<List<Order>> =
        try {
            dbQuery {
                val orders =
                    OrdersTable.selectAll().where { (OrdersTable.customerId eq sessionId) or (OrdersTable.sessionId eq sessionId) }
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

    override suspend fun createOrder(order: Order): Result<OrderResponse> =
        try {
            dbQuery {
                val finalId = order.id.ifBlank { java.util.UUID.randomUUID().toString() }

                // IDEMPOTÊNCIA: Verificar se o pedido já existe
                val existing = OrdersTable.selectAll().where { OrdersTable.id eq finalId }.count()
                if (existing > 0) {
                    return@dbQuery Result.success(OrderResponse(finalId)) // Já processado
                }

                // 1. Recalcular total real (Segurança: Anti-manipulação de preço)
                var calculatedTotal = 0.0
                order.items.forEach { item ->
                    val product = item.product
                    val service = item.service

                    val actualPrice = when {
                        product != null -> {
                            ProductsTable.selectAll().where { ProductsTable.id eq product.id }
                                .map { it[ProductsTable.price] }.singleOrNull() ?: item.price
                        }
                        service != null -> {
                            BookingServicesTable.selectAll().where { BookingServicesTable.id eq service.id }
                                .map { it[BookingServicesTable.price] }.singleOrNull() ?: item.price
                        }
                        else -> item.price
                    }
                    calculatedTotal += actualPrice * item.quantity
                }

                // Inserir cabeçalho do pedido
                OrdersTable.insert {
                    it[id] = finalId
                    it[storeId] = order.storeId
                    it[customerId] = order.customerId
                    it[sessionId] = order.sessionId
                    it[customerName] = order.customerName
                    it[customerPhone] = order.customerPhone
                    it[total] = calculatedTotal // Usa o total recalculado pelo servidor
                    it[status] = order.status.name
                    it[paymentMethod] = order.paymentMethod.name
                    it[whatsappContact] = order.whatsappContact
                    it[theme] = order.theme.name
                    it[createdAt] = System.currentTimeMillis()
                    it[updatedAt] = System.currentTimeMillis()

                    // Dados de Frete (Validação simples de frete aqui se necessário)
                    it[shippingStreet] = order.shippingAddress?.street
                    it[shippingNumber] = order.shippingAddress?.number
                    it[shippingComplement] = order.shippingAddress?.complement
                    it[shippingNeighborhood] = order.shippingAddress?.neighborhood
                    it[shippingCity] = order.shippingAddress?.city
                    it[shippingState] = order.shippingAddress?.state
                    it[shippingZipCode] = order.shippingAddress?.zipCode
                    it[shippingPrice] = order.shippingPrice
                    it[shippingMethod] = order.shippingMethod
                }

                // 2. Inserir itens e atualizar estoque
                order.items.forEach { item ->
                    val product = item.product
                    val service = item.service

                    // SEGURANÇA: Busca o preço REAL do banco para persistir no item do pedido
                    val actualPrice = when {
                        product != null -> {
                            ProductsTable.selectAll().where { ProductsTable.id eq product.id }
                                .map { it[ProductsTable.price] }.singleOrNull() ?: item.price
                        }
                        service != null -> {
                            BookingServicesTable.selectAll().where { BookingServicesTable.id eq service.id }
                                .map { it[BookingServicesTable.price] }.singleOrNull() ?: item.price
                        }
                        else -> item.price
                    }

                    // Salva o item no histórico do pedido com o preço OFICIAL
                    OrderItemsTable.insert {
                        it[id] = java.util.UUID.randomUUID().toString()
                        it[orderId] = finalId
                        it[productId] = product?.id
                        it[serviceId] = service?.id
                        it[appointmentId] = item.appointment?.id
                        it[productName] = item.name
                        it[productPrice] = actualPrice
                        it[quantity] = item.quantity
                    }

                    // CONTROLE DE ESTOQUE: Diminui a quantidade disponível se for produto
                    product?.let { prod ->
                        ProductsTable.update({ ProductsTable.id eq prod.id }) {
                            it.update(stock, stock minus item.quantity)
                        }
                    }

                    // AGENDAMENTO: Se for um serviço, cria o Appointment real de forma atômica
                    val appt = item.appointment
                    if (service != null && appt != null) {
                        bookingRepository.createAppointment(
                            appt.copy(
                                customerId = order.customerId ?: appt.customerId,
                                customerName = order.customerName ?: appt.customerName,
                                customerPhone = order.customerPhone ?: appt.customerPhone,
                                status = if (order.paymentMethod == PaymentMethod.APP) BookingStatus.PENDING else BookingStatus.CONFIRMED
                            )
                        )
                    }
                }

                Result.success(OrderResponse(finalId))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }

    override suspend fun updateOrderStatus(
        token: String,
        orderId: String,
        status: OrderStatus,
    ): Result<Unit> =
        try {
            dbQuery {
                val oldStatus = OrdersTable.select(OrdersTable.status)
                    .where { OrdersTable.id eq orderId }
                    .singleOrNull()?.get(OrdersTable.status)

                val updated =
                    if (token == "SYSTEM") {
                        // Bypass de segurança para atualizações automáticas via Webhook/Servidor
                        println("REPO: Tentando atualização via SYSTEM para Pedido $orderId -> $status")
                        OrdersTable.update({ OrdersTable.id eq orderId }) {
                            it[this.status] = status.name
                            it[updatedAt] = System.currentTimeMillis()
                        }
                    } else {
                        OrdersTable.update({
                            (OrdersTable.id eq orderId) and (OrdersTable.storeId inSubQuery StoresTable.select(StoresTable.id).where { StoresTable.ownerId eq token })
                        }) {
                            it[this.status] = status.name
                            it[updatedAt] = System.currentTimeMillis()
                        }
                    }

                if (updated > 0) {
                    // Log de Mudança de Status
                    OrderStatusLogsTable.insert {
                        it[id] = java.util.UUID.randomUUID().toString()
                        it[this.orderId] = orderId
                        it[this.oldStatus] = oldStatus
                        it[this.newStatus] = status.name
                        it[this.timestamp] = System.currentTimeMillis()
                        it[this.note] = if (token == "SYSTEM") "Atualizado automaticamente pelo sistema" else "Atualizado pelo lojista"
                    }
                    Result.success(Unit)
                }
                else Result.failure(Exception("Falha ao atualizar: Pedido $orderId não encontrado ou acesso negado para $token"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun getAnalytics(token: String): Result<MerchantAnalytics> = try {
        dbQuery {
            val storeIds = StoresTable.selectAll().where { StoresTable.ownerId eq token }.map { it[StoresTable.id] }
            if (storeIds.isEmpty()) return@dbQuery Result.failure(Exception("Nenhuma loja encontrada"))

            val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)

            // 1. Receita Diária
            val recentOrders = OrdersTable.selectAll()
                .where { (OrdersTable.storeId inList storeIds) and (OrdersTable.createdAt greaterEq sevenDaysAgo) and ((OrdersTable.status eq OrderStatus.PROCESSING.name) or (OrdersTable.status eq OrderStatus.COMPLETED.name)) }
                .map { it[OrdersTable.total] to it[OrdersTable.createdAt] }

            val dailyRevenue = recentOrders.groupBy {
                val date = Instant.fromEpochMilliseconds(it.second).toLocalDateTime(TimeZone.currentSystemDefault()).date
                date.toString()
            }.map { (date, orders) -> DailyRevenue(date, orders.sumOf { it.first }) }
             .sortedBy { it.date }

            // 2. Top Produtos
            val topProductsRaw = (OrderItemsTable innerJoin OrdersTable)
                .select(OrderItemsTable.productName, OrderItemsTable.quantity, OrderItemsTable.productPrice)
                .where { (OrdersTable.storeId inList storeIds) and (OrderItemsTable.productId.isNotNull()) }
                .toList()

            val topProducts = topProductsRaw.groupBy { it[OrderItemsTable.productName] }
                .map { (name, rows) ->
                    TopProduct(
                        name = name,
                        quantity = rows.sumOf { it[OrderItemsTable.quantity] },
                        revenue = rows.sumOf { it[OrderItemsTable.quantity] * it[OrderItemsTable.productPrice] }
                    )
                }
                .sortedByDescending { it.revenue }
                .take(5)

            // 3. Resumo de Agendamentos
            val appointments = AppointmentsTable.selectAll()
                .where { AppointmentsTable.storeId inList storeIds }
                .toList()

            val bookingSummary = BookingSummary(
                pending = appointments.count { it[AppointmentsTable.status] == BookingStatus.PENDING.name },
                confirmed = appointments.count { it[AppointmentsTable.status] == BookingStatus.CONFIRMED.name },
                cancelled = appointments.count { it[AppointmentsTable.status] == BookingStatus.CANCELLED.name },
                upcoming = appointments.count { it[AppointmentsTable.startTime] > System.currentTimeMillis() }
            )

            // 4. Totais
            val allOrders = OrdersTable.selectAll()
                .where { (OrdersTable.storeId inList storeIds) and (OrdersTable.status neq OrderStatus.CANCELLED.name) }
                .map { it[OrdersTable.total] }

            Result.success(
                MerchantAnalytics(
                    dailyRevenue = dailyRevenue,
                    topProducts = topProducts,
                    bookingSummary = bookingSummary,
                    totalOrders = allOrders.size,
                    averageTicket = if (allOrders.isNotEmpty()) allOrders.average() else 0.0
                )
            )
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getB2BAnalytics(token: String): Result<B2BAnalytics> = try {
        dbQuery {
            // 1. Total de lojistas ativos (Lojistas com pelo menos um pedido ou página)
            val activeMerchantsCount = StoresTable.selectAll().count()

            // 2. GMV Global (Gross Merchandise Volume) - Soma de todos os pedidos COMPLETED
            val globalGMV = OrdersTable.selectAll()
                .where { OrdersTable.status eq OrderStatus.COMPLETED.name }
                .sumOf { it[OrdersTable.total] }

            // 3. Ticket Médio Global
            val completedOrders = OrdersTable.selectAll()
                .where { OrdersTable.status eq OrderStatus.COMPLETED.name }
            val globalAverageTicket = if (completedOrders.count() > 0) {
                globalGMV / completedOrders.count()
            } else 0.0

            // 4. Top Merchants (Ranking de Performance)
            // Agrupamos pedidos completados por StoreId
            val revenueSum = OrdersTable.total.sum()
            val orderCountSum = OrdersTable.id.count()

            val topMerchants = OrdersTable
                .select(OrdersTable.storeId, revenueSum, orderCountSum)
                .where { OrdersTable.status eq OrderStatus.COMPLETED.name }
                .groupBy(OrdersTable.storeId)
                .orderBy(revenueSum to SortOrder.DESC)
                .limit(10)
                .map { row ->
                    val sid = row[OrdersTable.storeId]
                    val storeName = StoresTable.select(StoresTable.name).where { StoresTable.id eq sid }.singleOrNull()?.get(StoresTable.name) ?: "Loja Desconhecida"
                    MerchantPerformance(
                        merchantName = storeName,
                        totalRevenue = row[revenueSum] ?: 0.0,
                        orderCount = row[orderCountSum].toInt()
                    )
                }

            // 5. Receita Diária Global (Últimos 30 dias)
            val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
            val dailyRevenueSum = OrdersTable.total.sum()
            val globalDailyRevenue = OrdersTable
                .select(OrdersTable.createdAt, dailyRevenueSum)
                .where { (OrdersTable.status eq OrderStatus.COMPLETED.name) and (OrdersTable.createdAt greaterEq thirtyDaysAgo) }
                .groupBy(OrdersTable.createdAt)
                .map { row ->
                    DailyRevenue(
                        date = row[OrdersTable.createdAt].toString(),
                        amount = row[dailyRevenueSum] ?: 0.0
                    )
                }

            Result.success(
                B2BAnalytics(
                    totalMerchants = activeMerchantsCount.toInt(),
                    platformGMV = globalGMV,
                    globalAverageTicket = globalAverageTicket,
                    topMerchants = topMerchants,
                    globalDailyRevenue = globalDailyRevenue
                )
            )
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getAuditLogs(token: String): Result<List<Map<String, String>>> = try {
        dbQuery {
            val logs = AuditLogsTable
                .selectAll()
                .orderBy(AuditLogsTable.createdAt to SortOrder.DESC)
                .limit(100)
                .map { row ->
                    val auditLogMap = mutableMapOf<String, String>()
                    auditLogMap["id"] = row[AuditLogsTable.id]
                    auditLogMap["userId"] = row[AuditLogsTable.userId]?.toString() ?: "Sistema"
                    auditLogMap["action"] = row[AuditLogsTable.action]
                    auditLogMap["entityName"] = row[AuditLogsTable.entityName]
                    auditLogMap["details"] = row[AuditLogsTable.details] ?: ""
                    auditLogMap["createdAt"] = row[AuditLogsTable.createdAt].toString()
                    auditLogMap.toMap()
                }
            Result.success(logs)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    private fun fetchOrderItems(orderId: String, storeId: String): List<CartItem> {
        return OrderItemsTable.selectAll().where { OrderItemsTable.orderId eq orderId }
            .map { row ->
                val productId = row[OrderItemsTable.productId]
                val serviceId = row[OrderItemsTable.serviceId]

                CartItem(
                    product = if (productId != null) Product(
                        id = productId,
                        storeId = storeId,
                        name = row[OrderItemsTable.productName],
                        price = row[OrderItemsTable.productPrice]
                    ) else null,
                    service = if (serviceId != null) BookingService(
                        id = serviceId,
                        storeId = storeId,
                        name = row[OrderItemsTable.productName],
                        price = row[OrderItemsTable.productPrice],
                        durationMinutes = 0,
                        description = "Reserva de serviço"
                    ) else null,
                    quantity = row[OrderItemsTable.quantity],
                    appointment = row[OrderItemsTable.appointmentId]?.let { apptId ->
                        Appointment(
                            id = apptId,
                            storeId = storeId,
                            serviceId = serviceId ?: "",
                            customerName = "",
                            customerPhone = "",
                            startTime = kotlinx.datetime.Instant.fromEpochMilliseconds(0),
                            endTime = kotlinx.datetime.Instant.fromEpochMilliseconds(0)
                        )
                    },
                    customName = if (productId == null && serviceId == null) row[OrderItemsTable.productName] else null,
                    customPrice = if (productId == null && serviceId == null) row[OrderItemsTable.productPrice] else null
                )
            }
    }

    private fun ResultRow.toOrder(items: List<CartItem>) =
        Order(
            id = this[OrdersTable.id],
            storeId = this[OrdersTable.storeId],
            customerId = this[OrdersTable.customerId],
            sessionId = this[OrdersTable.sessionId],
            customerName = this[OrdersTable.customerName],
            customerPhone = this[OrdersTable.customerPhone],
            items = items,
            total = this[OrdersTable.total],
            status =
                try {
                    OrderStatus.valueOf(this[OrdersTable.status])
                } catch (e: Exception) {
                    OrderStatus.PENDING
                },
            paymentMethod = try {
                PaymentMethod.valueOf(this[OrdersTable.paymentMethod])
            } catch (e: Exception) {
                PaymentMethod.LOCAL
            },
            createdAt = this[OrdersTable.createdAt],
            updatedAt = this[OrdersTable.updatedAt],
            deletedAt = this[OrdersTable.deletedAt],
            whatsappContact = this[OrdersTable.whatsappContact],
            theme =
                try {
                    PageThemeConfig.valueOf(this[OrdersTable.theme])
                } catch (e: Exception) {
                    PageThemeConfig.ELEGANCE
                },
            shippingAddress = if (this[OrdersTable.shippingZipCode] != null) Address(
                id = "",
                userId = this[OrdersTable.customerId],
                street = this[OrdersTable.shippingStreet] ?: "",
                number = this[OrdersTable.shippingNumber] ?: "",
                complement = this[OrdersTable.shippingComplement],
                neighborhood = this[OrdersTable.shippingNeighborhood] ?: "",
                city = this[OrdersTable.shippingCity] ?: "",
                state = this[OrdersTable.shippingState] ?: "",
                zipCode = this[OrdersTable.shippingZipCode] ?: ""
            ) else null,
            shippingPrice = this[OrdersTable.shippingPrice],
            shippingMethod = this[OrdersTable.shippingMethod],
        )
}
