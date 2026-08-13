package com.itbenevides.genesys21.routes

import com.itbenevides.genesys21.domain.model.UserProfile
import com.itbenevides.genesys21.domain.model.toPublic
import com.itbenevides.genesys21.domain.repository.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes(userRepository: UserRepository) {
    route("/public/users") {
        get("/profile/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            userRepository.getUserProfile(id).onSuccess {
                // LGPD: Nunca retorne o perfil completo em rota pública.
                // Convertemos para o DTO PublicUserProfile antes de enviar ao cliente.
                call.respond(it.toPublic())
            }.onFailure {
                call.respond(HttpStatusCode.NotFound, "Perfil não encontrado")
            }
        }
    }

    authenticate("firebase") {
        route("/users") {
            post("/profile") {
                val principal = call.principal<UserIdPrincipal>() ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val profile = call.receive<UserProfile>()

                // Garante que o usuário só salve seu próprio perfil
                if (profile.id != principal.name) {
                    return@post call.respond(HttpStatusCode.Forbidden)
                }

                userRepository.saveUserProfile(profile).onSuccess {
                    call.respond(HttpStatusCode.OK)
                }.onFailure {
                    call.respond(HttpStatusCode.InternalServerError, it.message ?: "Erro ao salvar perfil")
                }
            }

            // LGPD: Direito de Exclusão
            delete("/profile") {
                val principal = call.principal<UserIdPrincipal>() ?: return@delete call.respond(HttpStatusCode.Unauthorized)
                val userId = principal.name

                // 1. Loga a intenção antes de apagar
                com.itbenevides.genesys21.data.service.AuditLogger.log(
                    userId = userId,
                    storeId = null,
                    action = "DELETE_ACCOUNT_REQUEST",
                    entityName = "User",
                    entityId = userId,
                    details = "Usuário solicitou exclusão de conta"
                )

                // 2. Executa a exclusão e anonimização
                userRepository.deleteUser(userId).onSuccess {
                    call.respond(HttpStatusCode.OK, "Conta excluída com sucesso. Seus logs foram anonimizados.")
                }.onFailure {
                    call.respond(HttpStatusCode.InternalServerError, "Erro ao processar exclusão: ${it.message}")
                }
            }
        }
    }
}
