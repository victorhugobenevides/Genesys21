package com.itbenevides.genesys21.routes

import com.itbenevides.genesys21.domain.model.Address
import com.itbenevides.genesys21.domain.repository.AddressRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.addressRoutes(repository: AddressRepository) {
    authenticate("firebase") {
        route("/addresses") {
            get {
                val principal = call.principal<UserIdPrincipal>()!!
                val addresses = repository.getAddresses(principal.name)
                call.respond(addresses)
            }

            post {
                val principal = call.principal<UserIdPrincipal>()!!
                val address = call.receive<Address>().copy(userId = principal.name)
                repository.saveAddress(address).fold(
                    onSuccess = { call.respond(HttpStatusCode.Created, it) },
                    onFailure = { call.respond(HttpStatusCode.InternalServerError, it.message ?: "Erro ao salvar endereço") }
                )
            }

            delete("/{id}") {
                val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                repository.deleteAddress(id).fold(
                    onSuccess = { call.respond(HttpStatusCode.OK) },
                    onFailure = { call.respond(HttpStatusCode.InternalServerError, it.message ?: "Erro ao excluir endereço") }
                )
            }
        }
    }
}
