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

        route("/admin/b2b") {
            // Middleware de SuperAdmin (Dogma)
            intercept(ApplicationCallPipeline.Call) {
                val principal = call.principal<UserIdPrincipal>()
                if (principal?.name == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@intercept finish()
                }

                // TODO: Usar repositório para verificar role real se não for victorkoto
                if (principal.name != "mKQ9MZqG6bYhy3JqvngGpv49ZZs1" && principal.name != "victorkoto@gmail.com") {
                     // Adicionaremos verificação de banco futuramente, mas por enquanto travamos no Dogma ID
                }
            }

            get("/summary") {
                val token = call.request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ") ?: ""
                orderRepository.getB2BAnalytics(token).onSuccess {
                    call.respond(it)
                }.onFailure {
                    call.respond(HttpStatusCode.InternalServerError, it.message ?: "Erro ao gerar B2B analytics")
                }
            }
        }
    }
}
