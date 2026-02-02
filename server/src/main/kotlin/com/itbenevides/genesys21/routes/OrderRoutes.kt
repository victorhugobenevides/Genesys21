package com.itbenevides.genesys21.routes

import com.itbenevides.genesys21.domain.model.Order
import com.itbenevides.genesys21.domain.model.OrderStatus
import com.itbenevides.genesys21.domain.repository.OrderRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.first

fun Route.orderRoutes(orderRepository: OrderRepository) {

    // 1. Rotas Públicas (Acesso sem Login)
    route("/public/orders") {
        
        // POST: Criar novo pedido
        post {
            val order = call.receive<Order>()
            orderRepository.createOrder(order)
                .onSuccess { call.respond(HttpStatusCode.Created, order.id) }
                .onFailure { call.respond(HttpStatusCode.InternalServerError, it.message ?: "Erro ao salvar pedido") }
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
