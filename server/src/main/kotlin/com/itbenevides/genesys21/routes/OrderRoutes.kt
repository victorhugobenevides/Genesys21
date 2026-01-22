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

fun Route.orderRoutes(orderRepository: SqliteOrderRepository) {
    authenticate("firebase") {
        route("/orders") {
            get {
                val principal = call.principal<UserIdPrincipal>() ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val orders = orderRepository.getOrders(principal.name)
                call.respond(orders)
            }

            patch("/{id}/status") {
                val principal = call.principal<UserIdPrincipal>() ?: return@patch call.respond(HttpStatusCode.Unauthorized)
                val id = call.parameters["id"] ?: return@patch call.respond(HttpStatusCode.BadRequest)
                val status = call.receive<OrderStatus>()
                
                // TODO: Validar se o pedido pertence ao usuário principal.name se necessário
                orderRepository.updateOrderStatus(id, status)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
    
    // Rota pública para criar pedido (clientes enviando pedido para a loja)
    post("/api/public/orders") {
        val order = call.receive<Order>()
        orderRepository.saveOrder(order)
        call.respond(HttpStatusCode.Created)
    }
}
