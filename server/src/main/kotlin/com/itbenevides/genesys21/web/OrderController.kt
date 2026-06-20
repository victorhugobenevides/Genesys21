package com.itbenevides.genesys21.web

import com.itbenevides.genesys21.data.model.Order
import com.itbenevides.genesys21.data.model.OrderStatus
import com.itbenevides.genesys21.data.repository.InMemoryOrderRepository
import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * REST controller exposing order‑tracking endpoints.
 *
 * Endpoints:
 *   GET    /orders                     -> list orders for the authenticated user
 *   GET    /orders/{id}                -> fetch a single order
 *   PATCH  /orders/{id}/status?status= -> update order status (operator only)
 */
fun Application.orderRoutes() {
    // Simple in‑memory repository – thread‑safe via a Mutex
    val repository = InMemoryOrderRepository()
    val mutex = Mutex()

    routing {
        // List orders for current user (mocked token extraction)
        get("/orders") {
            // In a real app, extract userId from auth token. Here we use a placeholder.
            val userId = call.request.headers["X-User-Id"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing X-User-Id header")
            val orders = repository.findByUserId(userId)
            call.respond(orders)
        }

        // Get a single order by id
        get("/orders/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing order id")
            val order = repository.findById(id)
            if (order == null) {
                call.respond(HttpStatusCode.NotFound, "Order not found")
            } else {
                call.respond(order)
            }
        }

        // Update order status – only operators allowed (mocked role header)
        patch("/orders/{id}/status") {
            val id = call.parameters["id"] ?: return@patch call.respond(HttpStatusCode.BadRequest, "Missing order id")
            val role = call.request.headers["X-User-Role"] ?: "user"
            if (role != "operator") {
                return@patch call.respond(HttpStatusCode.Forbidden, "Only operators can change status")
            }
            val statusParam = call.request.queryParameters["status"] ?: return@patch call.respond(HttpStatusCode.BadRequest, "Missing status query param")
            val newStatus = try {
                OrderStatus.valueOf(statusParam.uppercase())
            } catch (e: IllegalArgumentException) {
                return@patch call.respond(HttpStatusCode.BadRequest, "Invalid status value")
            }
            // protect repository mutation
            val updated = mutex.withLock { repository.updateStatus(id, newStatus) }
            if (updated) {
                call.respond(HttpStatusCode.OK, "Status updated")
            } else {
                call.respond(HttpStatusCode.NotFound, "Order not found")
            }
        }
    }
}
