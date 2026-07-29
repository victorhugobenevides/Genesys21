package com.itbenevides.genesys21.routes

import com.itbenevides.genesys21.domain.model.Order
import com.itbenevides.genesys21.domain.model.OrderResponse
import com.itbenevides.genesys21.domain.model.OrderStatus
import com.itbenevides.genesys21.domain.model.PaymentMethod
import com.itbenevides.genesys21.domain.repository.OrderRepository
import com.itbenevides.genesys21.domain.repository.StoreRepository
import com.itbenevides.genesys21.data.service.StripeService
import com.stripe.net.Webhook
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.first

fun Route.orderRoutes(
    orderRepository: OrderRepository,
    storeRepository: StoreRepository,
    stripeService: StripeService
) {
    // 1. Rotas Públicas (Acesso sem Login)
    route("/public/orders") {

        // WEBHOOK DA STRIPE (Assíncrono e Seguro)
        post("/webhook") {
            val payload = call.receiveText()
            val sigHeader = call.request.header("Stripe-Signature")

            val envSecret = System.getenv("STRIPE_WEBHOOK_SECRET")
            val endpointSecret = envSecret ?: "whsec_f3f2d698e8f7104ceb2ecc2bbe59d54c1680da981260b000404dc940f91ebfe6"

            println("WEBHOOK: Evento recebido. Origem: ${if (envSecret != null) "Ambiente/CI" else "Fallback Local"}")

            try {
                val event = Webhook.constructEvent(payload, sigHeader ?: "", endpointSecret)

                println("WEBHOOK: Tipo do evento: ${event.type}")

                when (event.type) {
                    "checkout.session.completed", "checkout.session.async_payment_succeeded" -> {
                        // Forma mais segura de extrair o objeto independente da versão da API
                        val stripeObject = event.dataObjectDeserializer.deserializeUnsafe()
                        if (stripeObject is com.stripe.model.checkout.Session) {
                            val orderId = stripeObject.clientReferenceId
                            println("WEBHOOK: Pagamento confirmado para o Pedido ID: $orderId")
                            if (!orderId.isNullOrBlank()) {
                                orderRepository.updateOrderStatus("SYSTEM", orderId, OrderStatus.PROCESSING)
                            }
                        } else {
                            println("WEBHOOK ERROR: Objeto recebido não é uma Session de Checkout")
                        }
                    }
                    "checkout.session.expired" -> {
                        val stripeObject = event.dataObjectDeserializer.deserializeUnsafe()
                        if (stripeObject is com.stripe.model.checkout.Session) {
                            val orderId = stripeObject.clientReferenceId
                            if (!orderId.isNullOrBlank()) {
                                println("WEBHOOK: Checkout expirado para o pedido $orderId")
                                orderRepository.updateOrderStatus("SYSTEM", orderId, OrderStatus.CANCELLED)
                            }
                        }
                    }
                }
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                println("WEBHOOK ERROR: Falha crítica: ${e.message}")
                e.printStackTrace()
                call.respond(HttpStatusCode.OK)
            }
        }
        // POST: Criar novo pedido
        post {
            try {
                val order = call.receive<Order>()
                println("SERVIDOR: Recebido pedido para loja '${order.storeId}'. Metodo: ${order.paymentMethod}")

                if (order.storeId.isBlank()) {
                    println("ERRO: Store ID vazio no JSON recebido")
                    call.respond(HttpStatusCode.BadRequest, "Erro: Store ID não informado no pedido.")
                    return@post
                }

                orderRepository.createOrder(order)
                    .onSuccess { response ->
                        val generatedId = response.orderId
                        println("PEDIDO SALVO: $generatedId")

                        if (order.paymentMethod == PaymentMethod.APP) {
                            val store = storeRepository.getStore(order.storeId).getOrNull()
                            val secretKey = store?.stripeSecretKey

                            if (!secretKey.isNullOrBlank()) {
                                try {
                                    val baseUrl = "${call.request.origin.scheme}://${call.request.origin.serverHost}"
                                    val finalBaseUrl = if (call.request.origin.serverPort != 80 && call.request.origin.serverPort != 443) {
                                        "$baseUrl:${call.request.origin.serverPort}"
                                    } else {
                                        baseUrl
                                    }

                                    // IMPORTANTE: Passar o pedido com o ID gerado para a Stripe
                                    val checkoutUrl = stripeService.createCheckoutSession(
                                        order = order.copy(id = generatedId),
                                        secretKey = secretKey,
                                        successUrl = "$finalBaseUrl/?orderId=$generatedId&status=success",
                                        cancelUrl = "$finalBaseUrl/?status=cancel",
                                        connectedAccountId = store.stripeAccountId
                                    )
                                    println("STRIPE: Checkout URL gerada com sucesso!")
                                    call.respond(HttpStatusCode.Created, OrderResponse(orderId = generatedId, checkoutUrl = checkoutUrl))
                                } catch (e: com.stripe.exception.StripeException) {
                                    val msg = "STRIPE ERROR: ${e.message}"
                                    println(msg)
                                    call.respond(HttpStatusCode.BadRequest, "Erro na Stripe: $msg. Verifique se o lojista completou o cadastro.")
                                } catch (e: Exception) {
                                    val msg = "STRIPE ERROR: ${e.message}"
                                    println(msg)
                                    e.printStackTrace()
                                    call.respond(HttpStatusCode.InternalServerError, msg)
                                }
                            } else {
                                val msg = "STRIPE ERROR: Chave secreta ausente para a loja ${order.storeId}"
                                println(msg)
                                call.respond(HttpStatusCode.BadRequest, msg)
                            }
                        } else {
                            call.respond(HttpStatusCode.Created, OrderResponse(orderId = generatedId))
                        }
                    }
                    .onFailure {
                        val msg = "REPO ERROR: ${it.message}"
                        println(msg)
                        it.printStackTrace()
                        call.respond(HttpStatusCode.InternalServerError, msg)
                    }
            } catch (e: Exception) {
                val errorMsg = "DESERIALIZATION ERROR: ${e.message}"
                println(errorMsg)
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest, errorMsg)
            }
        }

        // GET: Acompanhamento de um pedido específico
        get("/{orderId}") {
            val orderId = call.parameters["orderId"] ?: ""
            orderRepository.getOrderById(orderId)
                .onSuccess { call.respond(it) }
                .onFailure { call.respond(HttpStatusCode.NotFound, it.message ?: "Pedido não encontrado") }
        }

        // GET: Histórico de pedidos do cliente (Sessão do Visitante)
        get("/customer/{sessionId}") {
            val sessionId = call.parameters["sessionId"] ?: ""
            orderRepository.getCustomerOrders(sessionId)
                .onSuccess { call.respond(it) }
                .onFailure { call.respond(HttpStatusCode.InternalServerError, it.message ?: "Erro ao buscar histórico") }
        }
    }

    // 2. Rotas Administrativas (Apenas Lojista Logado)
    authenticate("firebase") {
        route("/orders") {
            // GET: Lista todos os pedidos do lojista autenticado
            get {
                val principal = call.principal<UserIdPrincipal>()
                if (principal == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Usuário não autenticado")
                    return@get
                }

                // Buscamos os pedidos usando o UID decodificado do Firebase
                val orders = orderRepository.getOrders(principal.name).first()
                call.respond(orders)
            }

            // PATCH: Atualizar status do pedido
            patch("/{orderId}/status") {
                val principal = call.principal<UserIdPrincipal>() ?: return@patch call.respond(HttpStatusCode.Unauthorized)
                val orderId = call.parameters["orderId"] ?: ""
                val status = call.receive<OrderStatus>()

                orderRepository.updateOrderStatus(principal.name, orderId, status)
                    .onSuccess { call.respond(HttpStatusCode.OK) }
                    .onFailure { call.respond(HttpStatusCode.Forbidden, it.message ?: "Acesso negado") }
            }
        }
    }
}
