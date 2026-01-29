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
    
    // Rotas Públicas - O prefixo /api já vem do Application.kt
    route("/public") {
        get("/pages/first") {
            pageRepository.getPages("") 
                .firstOrNull()?.let { call.respond(it) }
                ?: call.respond(HttpStatusCode.NotFound, "Nenhuma página disponível")
        }

        get("/pages/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            pageRepository.getPublicPage(id)
                .onSuccess { page -> call.respond(page) }
                .onFailure { call.respond(HttpStatusCode.NotFound, it.message ?: "Não encontrado") }
        }

        get("/domain/{domain}") {
            val domain = call.parameters["domain"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            pageRepository.getPageByDomain(domain)
                .onSuccess { page -> call.respond(page) }
                .onFailure { call.respond(HttpStatusCode.NotFound, it.message ?: "Não encontrado") }
        }
    }

    // Rotas Autenticadas - O prefixo /api já vem do Application.kt
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
                    .onFailure { 
                        val msg = it.message ?: "Erro ao criar"
                        if (msg.contains("unique", true)) {
                            call.respond(HttpStatusCode.Conflict, "Este domínio já está sendo usado por outra página.")
                        } else {
                            call.respond(HttpStatusCode.InternalServerError, msg)
                        }
                    }
            }

            put {
                val principal = call.principal<UserIdPrincipal>() ?: return@put call.respond(HttpStatusCode.Unauthorized)
                val page = call.receive<Page>()
                pageRepository.savePage(page, principal.name, isEditing = true)
                    .onSuccess { call.respond(HttpStatusCode.OK) }
                    .onFailure { 
                        val msg = it.message ?: "Sem permissão ou erro interno"
                        if (msg.contains("negado", true)) {
                            call.respond(HttpStatusCode.Forbidden, msg)
                        } else if (msg.contains("unique", true)) {
                            call.respond(HttpStatusCode.Conflict, "Este domínio já está sendo usado por outra página.")
                        } else {
                            call.respond(HttpStatusCode.InternalServerError, msg)
                        }
                    }
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
