package com.itbenevides.genesys21.routes

import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.repository.PageRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.pageRoutes(repository: PageRepository) {
    route("/pages") {
        get {
            call.respond(repository.getPages())
        }

        authenticate("firebase") {
            post {
                val page = call.receive<Page>()
                repository.savePage(page, "").onSuccess {
                    call.respond(HttpStatusCode.Created, it)
                }.onFailure {
                    call.respond(HttpStatusCode.BadRequest, it.message ?: "Error")
                }
            }
            put {
                val page = call.receive<Page>()
                repository.updatePage(page, "").onSuccess {
                    call.respond(HttpStatusCode.OK, it)
                }.onFailure {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
            delete("/{id}") {
                val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                repository.deletePage(id, "").onSuccess {
                    call.respond(HttpStatusCode.OK)
                }.onFailure {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}
