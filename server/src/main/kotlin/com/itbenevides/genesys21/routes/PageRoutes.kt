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
    
    // Rotas Públicas (sem autenticação)
    route("/api/public/pages") {
        get("/{id}") {
            val id = call.parameters["id"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "ID da página não fornecido")
                return@get
            }
            
            pageRepository.getPublicPage(id)
                .onSuccess { page -> call.respond(page) }
                .onFailure { call.respond(HttpStatusCode.NotFound) }
        }
    }

    // Rotas Autenticadas
    authenticate("firebase") {
        route("/pages") {
            get {
                val principal = call.principal<UserIdPrincipal>()
                if (principal == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@get
                }
                val pages = pageRepository.getPages(principal.name)
                call.respond(pages)
            }

            post {
                val principal = call.principal<UserIdPrincipal>()
                if (principal == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@post
                }
                val page = call.receive<Page>()
                pageRepository.savePage(page, principal.name, isEditing = false)
                call.respond(HttpStatusCode.Created)
            }

            put {
                val principal = call.principal<UserIdPrincipal>()
                if (principal == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@put
                }
                val page = call.receive<Page>()
                pageRepository.savePage(page, principal.name, isEditing = true)
                call.respond(HttpStatusCode.OK)
            }

            delete("/{id}") {
                val principal = call.principal<UserIdPrincipal>()
                if (principal == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@delete
                }
                val id = call.parameters["id"]
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "ID da página não fornecido")
                    return@delete
                }
                pageRepository.deletePage(id, principal.name)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}
