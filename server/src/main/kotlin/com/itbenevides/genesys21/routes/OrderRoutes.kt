package com.itbenevides.genesys21.routes

import com.itbenevides.genesys21.data.repository.MercadoPagoCheckoutRequest
import com.itbenevides.genesys21.data.repository.SqliteOrderRepository
import com.itbenevides.genesys21.domain.model.Order
import com.itbenevides.genesys21.domain.model.OrderStatus
import com.itbenevides.genesys21.domain.repository.OrderRepository
import com.mercadopago.MercadoPagoConfig
import com.mercadopago.client.payment.PaymentClient
import com.mercadopago.client.preference.*
import com.mercadopago.exceptions.MPApiException
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.first
import java.math.BigDecimal
import java.math.RoundingMode

fun Route.orderRoutes(orderRepository: OrderRepository) {
    val sqliteOrderRepository = orderRepository as SqliteOrderRepository

    // --- ADMIN ROUTES ---
    authenticate("firebase") {
        get("/orders") {
            val userId = call.principal<UserIdPrincipal>()?.name ?: return@get call.respond(HttpStatusCode.Unauthorized)
            try {
                val orders = sqliteOrderRepository.getOrders(userId).first()
                call.respond(orders)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Erro ao buscar pedidos")
            }
        }

        patch("/orders/{orderId}/status") {
            val userId = call.principal<UserIdPrincipal>()?.name ?: return@patch call.respond(HttpStatusCode.Unauthorized)
            val orderId = call.parameters["orderId"] ?: return@patch call.respond(HttpStatusCode.BadRequest, "ID do pedido ausente.")
            try {
                val newStatus = call.receive<OrderStatus>()
                sqliteOrderRepository.updateOrderStatus(userId, orderId, newStatus).fold(
                    onSuccess = { call.respond(HttpStatusCode.OK) },
                    onFailure = { ex -> call.respond(HttpStatusCode.InternalServerError, "Erro ao atualizar o status do pedido: ${ex.message}") }
                )
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Corpo da requisição inválido: ${e.message}")
            }
        }
    }

    // --- CHECKOUT FLOW ---
    post("/checkout/mercadopago") {
        try {
            val token = MercadoPagoConfig.getAccessToken()
            if (token.isNullOrBlank()) {
                call.respond(HttpStatusCode.InternalServerError, "Configuração do Mercado Pago ausente")
                return@post
            }

            val request = call.receive<MercadoPagoCheckoutRequest>()
            val order = request.order
            
            // 1. Criar o pedido no banco com status inicial
            // Usamos PAYMENT_PENDING para indicar que o usuário foi para o checkout mas ainda não pagou
            sqliteOrderRepository.createOrder(order.copy(status = OrderStatus.PAYMENT_PENDING))
            
            // 2. Montar itens para o Mercado Pago
            val items = order.items.map {
                val unitPrice = if (it.product.price <= 0) 1.0 else it.product.price
                PreferenceItemRequest.builder()
                    .id(it.product.id)
                    .title(it.product.name.take(50))
                    .quantity(it.quantity)
                    .currencyId("BRL")
                    .unitPrice(BigDecimal(unitPrice).setScale(2, RoundingMode.HALF_UP))
                    .build()
            }
            
            // 3. Dados do Pagador
            val nameParts = (order.customerName ?: "Cliente").trim().split("\\s+".toRegex())
            val pFirstName = nameParts.firstOrNull() ?: "Cliente"
            val pLastName = if (nameParts.size > 1) nameParts.drop(1).joinToString(" ") else "Teste"

            val payer = PreferencePayerRequest.builder()
                .name(pFirstName)
                .surname(pLastName)
                .email("test_user_123456@testuser.com")
                .build()

            // 4. Configurar URLs de retorno (Onde o cliente cai ao terminar)
            val host = call.request.host()
            val port = call.request.port()
            val webRedirectBase = "http://$host:$port/order/${order.id}"

            val backUrls = PreferenceBackUrlsRequest.builder()
                .success("$webRedirectBase?payment_status=approved")
                .pending("$webRedirectBase?payment_status=pending")
                .failure("$webRedirectBase?payment_status=error")
                .build()

            // 5. Criar a Preferência
            val preferenceRequest = PreferenceRequest.builder()
                .items(items)
                .payer(payer)
                .backUrls(backUrls)
                .externalReference(order.id) // Fundamental para o Webhook
                .binaryMode(true) // Força status final (aprovado ou recusado, sem "em análise" se possível)
                .statementDescriptor("GENESYS21") // Nome na fatura do cartão
                .build()

            val client = PreferenceClient()
            val preference = client.create(preferenceRequest)
            
            // Retornamos o link para o frontend abrir
            call.respond(HttpStatusCode.OK, preference.sandboxInitPoint)
            
        } catch (e: MPApiException) {
            println("MP_ERROR: ${e.apiResponse.content}")
            call.respond(HttpStatusCode.BadRequest, "Erro ao gerar pagamento")
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Erro interno no checkout")
        }
    }

    // --- WEBHOOK (O CORAÇÃO DA VENDA CONCLUÍDA) ---
    post("/webhook/mercadopago") {
        try {
            // O MP envia o ID do pagamento no parâmetro 'data.id' (para notificações v2)
            // ou 'id' para notificações v1.
            val paymentId = call.request.queryParameters["data.id"] ?: call.request.queryParameters["id"]
            val type = call.request.queryParameters["type"] ?: "payment"

            if (paymentId != null && type == "payment") {
                val client = PaymentClient()
                val payment = client.get(paymentId.toLong())
                val orderId = payment.externalReference
                
                if (orderId != null) {
                    val newStatus = when (payment.status) {
                        "approved" -> OrderStatus.COMPLETED
                        "in_process", "in_mediation" -> OrderStatus.PROCESSING
                        "rejected", "cancelled", "refunded", "charged_back" -> OrderStatus.FAILED
                        else -> OrderStatus.PENDING
                    }
                    
                    // Atualiza o banco de dados
                    sqliteOrderRepository.updateOrderStatusForWebhook(orderId, newStatus)
                    println("WEBHOOK: Pedido $orderId atualizado para $newStatus via MP ID $paymentId")
                }
            }
            call.respond(HttpStatusCode.OK)
        } catch (e: Exception) {
            println("WEBHOOK_ERROR: ${e.message}")
            // Respondemos OK para o MP não ficar reenviando a mesma notificação em caso de erro de lógica
            call.respond(HttpStatusCode.OK)
        }
    }
}

fun Route.publicOrderRoutes(orderRepository: OrderRepository) {
    val sqliteOrderRepository = orderRepository as SqliteOrderRepository

    route("/public/orders") {
        get("/customer/{sessionId}") {
            val sessionId = call.parameters["sessionId"] ?: ""
            sqliteOrderRepository.getCustomerOrders(sessionId).fold(
                onSuccess = { call.respond(it) },
                onFailure = { call.respond(HttpStatusCode.InternalServerError, "Erro ao buscar histórico") }
            )
        }

        get("/{orderId}") {
            val orderId = call.parameters["orderId"] ?: ""
            sqliteOrderRepository.getOrderById(orderId).fold(
                onSuccess = { call.respond(it) },
                onFailure = { call.respond(HttpStatusCode.NotFound, "Não encontrado") }
            )
        }
    }
}
