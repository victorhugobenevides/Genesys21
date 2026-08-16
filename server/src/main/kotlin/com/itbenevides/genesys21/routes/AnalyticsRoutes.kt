package com.itbenevides.genesys21.routes

import com.itbenevides.genesys21.data.repository.SqliteOrderRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.analyticsRoutes(orderRepository: SqliteOrderRepository) {
    authenticate("firebase") {
        route("/admin/analytics") {
            get("/summary") {
                val principal = call.principal<UserIdPrincipal>() ?: return@get call.respond(HttpStatusCode.Unauthorized)

                orderRepository.getAnalytics(principal.name).onSuccess {
                    call.respond(it)
                }.onFailure {
                    call.respond(HttpStatusCode.InternalServerError, it.message ?: "Erro ao gerar analytics")
                }
            }
        }
    }
}
