package com.itbenevides.genesys21.routes

import com.itbenevides.genesys21.domain.model.Store
import com.itbenevides.genesys21.domain.repository.StoreRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.storeRoutes(repository: StoreRepository) {
    route("/stores") {
        get("/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            repository.getStore(id).fold(
                onSuccess = { call.respond(it) },
                onFailure = { call.respond(HttpStatusCode.NotFound) }
            )
        }

        authenticate("firebase") {
            post {
                val principal = call.principal<UserIdPrincipal>()!!
                val store = call.receive<Store>()
                repository.saveStore(store, principal.name).fold(
                    onSuccess = { call.respond(HttpStatusCode.Created) },
                    onFailure = { call.respond(HttpStatusCode.InternalServerError, it.message ?: "Erro ao salvar loja") }
                )
            }
        }
    }
}
