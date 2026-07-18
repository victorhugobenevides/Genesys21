package com.itbenevides.genesys21.routes

import com.itbenevides.genesys21.domain.model.Category
import com.itbenevides.genesys21.domain.repository.PageRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.categoryRoutes(repository: PageRepository) {
    route("/categories") {
        authenticate("firebase") {
            get {
                val principal = call.principal<UserIdPrincipal>() ?: return@get call.respond(HttpStatusCode.Unauthorized)
                repository.getCategories(principal.name).onSuccess {
                    call.respond(it)
                }.onFailure {
                    call.respond(HttpStatusCode.InternalServerError, it.message ?: "Erro ao buscar categorias")
                }
            }

            post {
                val principal = call.principal<UserIdPrincipal>() ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val category = call.receive<Category>()
                repository.saveCategory(category, principal.name).onSuccess {
                    call.respond(HttpStatusCode.Created)
                }.onFailure {
                    call.respond(HttpStatusCode.InternalServerError, it.message ?: "Erro ao salvar categoria")
                }
            }

            put {
                val principal = call.principal<UserIdPrincipal>() ?: return@put call.respond(HttpStatusCode.Unauthorized)
                val category = call.receive<Category>()
                repository.saveCategory(category, principal.name).onSuccess {
                    call.respond(HttpStatusCode.OK)
                }.onFailure {
                    call.respond(HttpStatusCode.InternalServerError, it.message ?: "Erro ao atualizar categoria")
                }
            }

            delete("/{id}") {
                val principal = call.principal<UserIdPrincipal>() ?: return@delete call.respond(HttpStatusCode.Unauthorized)
                val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                repository.deleteCategory(id, principal.name).onSuccess {
                    call.respond(HttpStatusCode.OK)
                }.onFailure {
                    call.respond(HttpStatusCode.InternalServerError, it.message ?: "Erro ao excluir categoria")
                }
            }
        }
    }
}
