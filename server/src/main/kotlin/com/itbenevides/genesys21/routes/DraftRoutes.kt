package com.itbenevides.genesys21.routes

import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.repository.DraftRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.draftRoutes(draftRepository: DraftRepository) {
    authenticate("firebase") {
        route("/api/drafts") {
            get("/{pageId}") {
                val principal = call.principal<UserIdPrincipal>()!!
                val pageId = call.parameters["pageId"] ?: return@get call.respond(HttpStatusCode.BadRequest)

                draftRepository.getDraft(pageId, principal.name).onSuccess {
                    if (it != null) call.respond(it) else call.respond(HttpStatusCode.NotFound)
                }.onFailure {
                    call.respond(HttpStatusCode.InternalServerError, it.message ?: "Erro ao buscar rascunho")
                }
            }

            post {
                val principal = call.principal<UserIdPrincipal>()!!
                val page = call.receive<Page>()

                draftRepository.saveDraft(page, principal.name).onSuccess {
                    call.respond(HttpStatusCode.OK)
                }.onFailure {
                    call.respond(HttpStatusCode.InternalServerError, it.message ?: "Erro ao salvar rascunho")
                }
            }

            delete("/{pageId}") {
                val principal = call.principal<UserIdPrincipal>()!!
                val pageId = call.parameters["pageId"] ?: return@delete call.respond(HttpStatusCode.BadRequest)

                draftRepository.deleteDraft(pageId, principal.name).onSuccess {
                    call.respond(HttpStatusCode.OK)
                }.onFailure {
                    call.respond(HttpStatusCode.InternalServerError, it.message ?: "Erro ao excluir rascunho")
                }
            }
        }
    }
}
