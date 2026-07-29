package com.itbenevides.genesys21.routes

import com.itbenevides.genesys21.data.repository.SqliteCartRepository
import com.itbenevides.genesys21.domain.model.CartItem
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.cartRoutes(cartRepository: SqliteCartRepository) {
    // REMOVIDO /api/ pois já está no Application.kt
    route("/cart") {
        fun getCartIdentifier(call: ApplicationCall): String? {
            val authUser = call.principal<UserIdPrincipal>()?.name
            if (authUser != null) return authUser

            return call.request.headers["X-Cart-Session-Id"] ?: call.request.queryParameters["sessionId"]
        }

        authenticate("firebase", optional = true) {
            get {
                val cartId = getCartIdentifier(call)
                if (cartId == null) {
                    call.respond(emptyList<CartItem>())
                    return@get
                }
                val cart = cartRepository.getCart(cartId)
                call.respond(cart)
            }

            post {
                val cartId = getCartIdentifier(call) ?: return@post call.respond(HttpStatusCode.BadRequest, "Sessão inválida")
                val items = call.receive<List<CartItem>>()
                cartRepository.saveCart(cartId, items)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}
