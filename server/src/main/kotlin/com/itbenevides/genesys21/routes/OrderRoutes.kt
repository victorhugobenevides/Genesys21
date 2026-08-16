package com.itbenevides.genesys21.routes

import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.domain.repository.OrderRepository
import com.itbenevides.genesys21.domain.repository.StoreRepository
import com.itbenevides.genesys21.data.service.StripeService
import com.itbenevides.genesys21.util.PrivacyUtils
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

        // WEBHOOK DA STRIPE (Suporte a PaymentIntent)
        post("/webhook") {
            val payload = call.receiveText()
            val sigHeader = call.request.header("Stripe-Signature")

            val envSecret = System.getenv("STRIPE_WEBHOOK_SECRET")
            val endpointSecret = envSecret ?: "whsec_f3f2d698e8f7104ceb2ecc2bbe59d54c1680da981260b000404dc940f91ebfe6"

            try {
                val event = Webhook.constructEvent(payload, sigHeader ?: "", endpointSecret)
                println("WEBHOOK: Evento '${event.type}' recebido.")

                when (event.type) {
                    "payment_intent.succeeded" -> {
                        val intent = event.dataObjectDeserializer.deserializeUnsafe() as com.stripe.model.PaymentIntent
                        val orderId = intent.metadata["order_id"]
                        if (!orderId.isNullOrBlank()) {
                            println("WEBHOOK: Pagamento PI confirmado para o Pedido ID: $orderId")
                            orderRepository.updateOrderStatus("SYSTEM", orderId, OrderStatus.PROCESSING)
                        }
                    }
                    "checkout.session.completed" -> {
                        val session = event.dataObjectDeserializer.deserializeUnsafe() as com.stripe.model.checkout.Session
                        val orderId = session.clientReferenceId
                        if (!orderId.isNullOrBlank()) {
                            println("WEBHOOK: Pagamento Checkout confirmado para o Pedido ID: $orderId")
                            orderRepository.updateOrderStatus("SYSTEM", orderId, OrderStatus.PROCESSING)
                        }
                    }
                }
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                println("WEBHOOK ERROR: ${e.message}")
                call.respond(HttpStatusCode.OK) // Sempre 200 para evitar retentativas infinitas
            }
        }

        // POST: Criar novo pedido
        post {
            try {
                val order = call.receive<Order>()
                if (order.storeId.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, "Store ID não informado.")
                    return@post
                }

                orderRepository.createOrder(order).onSuccess { response ->
                    val generatedId = response.orderId

                    if (order.paymentMethod == PaymentMethod.APP) {
                        val store = storeRepository.getStore(order.storeId).getOrNull()
                        val secretKey = store?.stripeSecretKey

                        if (!secretKey.isNullOrBlank()) {
                            try {
                                // MIGRADO: De CheckoutSession para PaymentIntent (Embedded Checkout)
                                val clientSecret = stripeService.createPaymentIntent(
                                    order = order.copy(id = generatedId),
                                    secretKey = secretKey,
                                    connectedAccountId = store.stripeAccountId
                                )

                                call.respond(
                                    HttpStatusCode.Created,
                                    OrderResponse(
                                        orderId = generatedId,
                                        stripeClientSecret = clientSecret,
                                        stripePublishableKey = store.stripePublicKey // Lojista deve fornecer para o bridge
                                    )
                                )
                            } catch (e: Exception) {
                                println("STRIPE ERROR: ${e.message}")
                                call.respond(HttpStatusCode.InternalServerError, "Erro ao processar pagamento.")
                            }
                        } else {
                            call.respond(HttpStatusCode.BadRequest, "Configuração de pagamento incompleta.")
                        }
                    } else {
                        call.respond(HttpStatusCode.Created, OrderResponse(orderId = generatedId))
                    }
                }.onFailure {
                    call.respond(HttpStatusCode.InternalServerError, it.message ?: "Erro ao salvar pedido.")
                }
            } catch (_: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Dados inválidos.")
            }
        }

        // GET: Acompanhamento de um pedido específico
        get("/{orderId}") {
            val orderId = call.parameters["orderId"] ?: ""
            orderRepository.getOrderById(orderId)
                .onSuccess { order ->
                    // LGPD: Mascarar PII (Informações Pessoais) em rastreio público
                    val maskedOrder = order.copy(
                        customerName = order.customerName?.take(3) + "****",
                        customerPhone = PrivacyUtils.maskPhone(order.customerPhone),
                        shippingAddress = order.shippingAddress?.copy(
                            street = PrivacyUtils.maskAddress(order.shippingAddress?.street, order.shippingAddress?.number),
                            complement = null,
                            zipCode = order.shippingAddress?.zipCode?.take(5) + "-***"
                        )
                    )
                    call.respond(maskedOrder)
                }
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
            get {
                val principal = call.principal<UserIdPrincipal>() ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val orders = orderRepository.getOrders(principal.name).first()
                call.respond(orders)
            }

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
