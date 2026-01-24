package com.itbenevides.genesys21.routes

import com.itbenevides.genesys21.data.repository.SqliteOrderRepository
import com.itbenevides.genesys21.domain.model.Order
import com.itbenevides.genesys21.domain.model.OrderStatus
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("OrderRoutes")

fun Route.orderRoutes(orderRepository: SqliteOrderRepository) {
    authenticate("firebase") {
        route("/api/orders") { // Prefixo /api/ para evitar conflito com páginas
            get {
                val principal = call.principal<UserIdPrincipal>() ?: return@get call.respond(HttpStatusCode.Unauthorized)
                logger.info("ADMIN: Buscando pedidos para o dono: ${principal.name}")
                val orders = orderRepository.getOrders(principal.name)
                call.respond(orders)
            }

            patch("/{id}/status") {
                val principal = call.principal<UserIdPrincipal>() ?: return@patch call.respond(HttpStatusCode.Unauthorized)
                val id = call.parameters["id"] ?: return@patch call.respond(HttpStatusCode.BadRequest)
                val status = call.receive<OrderStatus>()
                
                logger.info("ADMIN: Atualizando pedido $id para status $status")
                orderRepository.updateOrderStatus(id, status)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
    
    // Rota pública para criar pedido
    post("/api/public/orders") {
        try {
            val order = call.receive<Order>()
            logger.info("PUBLIC: Recebendo novo pedido: ID=${order.id}, ParaDono=${order.userId}, Total=${order.total}")
            
            if (order.userId.isBlank()) {
                logger.error("PUBLIC: Tentativa de salvar pedido sem userId (Dono)")
                return@post call.respond(HttpStatusCode.BadRequest, "Proprietário da loja não identificado")
            }

            // Garante que o timestamp seja do servidor para evitar fraude de data no cliente
            val orderWithServerTime = order.copy(createdAt = System.currentTimeMillis())
            
            orderRepository.saveOrder(orderWithServerTime)
            call.respond(HttpStatusCode.Created)
        } catch (e: Exception) {
            logger.error("PUBLIC: Erro ao salvar pedido: ${e.message}")
            call.respond(HttpStatusCode.InternalServerError, "Erro no processamento do pedido")
        }
    }

    // Rota pública para consultar UM pedido específico (Acompanhamento do cliente)
    get("/api/public/orders/{id}") {
        val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
        val order = orderRepository.getOrderById(id)
        if (order != null) {
            call.respond(order)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }

    // Rota pública para consultar TODOS os pedidos de um cliente (Histórico do visitante)
    get("/api/public/orders/customer/{sessionId}") {
        val sessionId = call.parameters["sessionId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
        val orders = orderRepository.getCustomerOrders(sessionId)
        call.respond(orders)
    }
}
