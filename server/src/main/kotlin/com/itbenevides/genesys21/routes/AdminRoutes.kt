package com.itbenevides.genesys21.routes

import com.itbenevides.genesys21.domain.model.UserRole
import com.itbenevides.genesys21.domain.model.UserStatus
import com.itbenevides.genesys21.domain.repository.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.adminRoutes(userRepository: UserRepository) {
    authenticate("firebase") {
        route("/admin") {
            // Middleware de SuperAdmin
            intercept(ApplicationCallPipeline.Call) {
                val principal = call.principal<UserIdPrincipal>()
                if (principal == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@intercept finish()
                }

                val user = userRepository.getUserProfile(principal.name).getOrNull()
                if (user?.role != UserRole.SUPERADMIN) {
                    call.respond(HttpStatusCode.Forbidden, "Acesso restrito ao SuperAdmin")
                    return@intercept finish()
                }
            }

            get("/users") {
                val token = call.request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ") ?: ""
                userRepository.getAllUsers(token).onSuccess {
                    call.respond(it)
                }.onFailure {
                    call.respond(HttpStatusCode.InternalServerError, it.message ?: "Erro ao buscar usuários")
                }
            }

            put("/users/{userId}/role") {
                val userId = call.parameters["userId"] ?: return@put call.respond(HttpStatusCode.BadRequest)
                val roleName = call.request.queryParameters["role"] ?: return@put call.respond(HttpStatusCode.BadRequest)
                val token = "" // Token não é mais necessário aqui pois já passou pelo interceptor

                try {
                    val role = UserRole.valueOf(roleName)
                    userRepository.updateUserRole(token, userId, role).onSuccess {
                        call.respond(HttpStatusCode.OK)
                    }.onFailure {
                        call.respond(HttpStatusCode.InternalServerError, it.message ?: "Erro ao atualizar cargo")
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "Cargo inválido")
                }
            }

            put("/users/{userId}/status") {
                val userId = call.parameters["userId"] ?: return@put call.respond(HttpStatusCode.BadRequest)
                val statusName = call.request.queryParameters["status"] ?: return@put call.respond(HttpStatusCode.BadRequest)
                val token = ""

                try {
                    val status = UserStatus.valueOf(statusName)
                    userRepository.updateUserStatus(token, userId, status).onSuccess {
                        call.respond(HttpStatusCode.OK)
                    }.onFailure {
                        call.respond(HttpStatusCode.InternalServerError, it.message ?: "Erro ao atualizar status")
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "Status inválido")
                }
            }
        }
    }
}
