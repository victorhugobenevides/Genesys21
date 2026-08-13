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

                // 1. Inserir cabeçalho do pedido
                OrdersTable.insert {
                    it[id] = finalId
                    it[storeId] = order.storeId
                    it[customerId] = order.customerId
                    it[sessionId] = order.sessionId
                    it[customerName] = order.customerName
                    it[customerPhone] = order.customerPhone
                    it[total] = order.total
                    it[status] = order.status.name
                    it[paymentMethod] = order.paymentMethod.name
                    it[whatsappContact] = order.whatsappContact
                    it[theme] = order.theme.name
                    it[createdAt] = System.currentTimeMillis()
                    it[updatedAt] = System.currentTimeMillis()

                    // Dados de Frete
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
                    // Salva o item no histórico do pedido
                    OrderItemsTable.insert {
                        it[id] = java.util.UUID.randomUUID().toString()
                        it[orderId] = finalId
                        it[productId] = item.product?.id
                        it[serviceId] = item.service?.id
                        it[appointmentId] = item.appointment?.id
                        it[productName] = item.name
                        it[productPrice] = item.price
                        it[quantity] = item.quantity
                    }

                    // CONTROLE DE ESTOQUE: Diminui a quantidade disponível se for produto
                    item.product?.let { prod ->
                        ProductsTable.update({ ProductsTable.id eq prod.id }) {
                            it.update(stock, stock minus item.quantity)
                        }
                    }

                    // AGENDAMENTO: Se for um serviço, cria o Appointment real de forma atômica
                    val appt = item.appointment
                    if (item.service != null && appt != null) {
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

                if (updated > 0) Result.success(Unit)
                else Result.failure(Exception("Falha ao atualizar: Pedido $orderId não encontrado ou acesso negado para $token"))
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
