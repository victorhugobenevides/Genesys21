package com.itbenevides.genesys21.routes

import com.itbenevides.genesys21.domain.model.DomainMapping
import com.itbenevides.genesys21.domain.model.UserRole
import com.itbenevides.genesys21.domain.repository.DomainRepository
import com.itbenevides.genesys21.domain.repository.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.systemRoutes(domainRepository: DomainRepository, userRepository: UserRepository) {
    authenticate("firebase") {
        route("/admin/system") {
            // SuperAdmin Protection Interceptor
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

            route("/domains") {
                get {
                    domainRepository.getAllMappings().onSuccess {
                        call.respond(it)
                    }.onFailure {
                        call.respond(HttpStatusCode.InternalServerError, it.message ?: "Erro ao buscar mapeamentos")
                    }
                }

                post {
                    val mapping = call.receive<DomainMapping>()
                    domainRepository.saveMapping(mapping).onSuccess {
                        call.respond(HttpStatusCode.Created)
                    }.onFailure {
                        call.respond(HttpStatusCode.InternalServerError, it.message ?: "Erro ao salvar mapeamento")
                    }
                }

                delete("/{id}") {
                    val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                    domainRepository.deleteMapping(id).onSuccess {
                        call.respond(HttpStatusCode.OK)
                    }.onFailure {
                        call.respond(HttpStatusCode.InternalServerError, it.message ?: "Erro ao excluir mapeamento")
                    }
                }
            }
        }
    }
}
