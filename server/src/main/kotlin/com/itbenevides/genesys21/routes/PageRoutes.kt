package com.itbenevides.genesys21.routes

import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.repository.PageRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.pageRoutes(pageRepository: PageRepository) {
    
    // Rotas Públicas
    route("/api/public/pages") {
        get("/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            pageRepository.getPublicPage(id)
                .onSuccess { page -> call.respond(page) }
                .onFailure { call.respond(HttpStatusCode.NotFound) }
        }
    }

    // Rotas Autenticadas
    authenticate("firebase") {
        route("/pages") {
            get {
                val principal = call.principal<UserIdPrincipal>() ?: return@get call.respond(HttpStatusCode.Unauthorized)
                call.respond(pageRepository.getPages(principal.name))
            }

            post {
                val principal = call.principal<UserIdPrincipal>() ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val page = call.receive<Page>()
                pageRepository.savePage(page, principal.name, isEditing = false)
                    .onSuccess { call.respond(HttpStatusCode.Created) }
                    .onFailure { call.respond(HttpStatusCode.InternalServerError, it.message ?: "Erro ao criar") }
            }

            put {
                val principal = call.principal<UserIdPrincipal>() ?: return@put call.respond(HttpStatusCode.Unauthorized)
                val page = call.receive<Page>()
                pageRepository.savePage(page, principal.name, isEditing = true)
                    .onSuccess { call.respond(HttpStatusCode.OK) }
                    .onFailure { call.respond(HttpStatusCode.Forbidden, it.message ?: "Sem permissão") }
            }

            delete("/{id}") {
                val principal = call.principal<UserIdPrincipal>() ?: return@delete call.respond(HttpStatusCode.Unauthorized)
                val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                pageRepository.deletePage(id, principal.name)
                    .onSuccess { call.respond(HttpStatusCode.OK) }
                    .onFailure { call.respond(HttpStatusCode.NotFound) }
            }
        }
    }
}
