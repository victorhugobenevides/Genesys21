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
    // Rota de carrinho agora é pública, mas identifica o usuário se houver token
    route("/cart") {
        
        // Função auxiliar para pegar o ID do carrinho (Auth UID ou Session Header)
        fun ApplicationCall.getCartIdentifier(): String? {
            val authUser = principal<UserIdPrincipal>()?.name
            if (authUser != null) return authUser
            
            return request.headers["X-Cart-Session-Id"] // ID gerado pelo cliente
        }

        authenticate("firebase", optional = true) {
            get {
                val cartId = call.getCartIdentifier() ?: return@get call.respond(HttpStatusCode.BadRequest, "ID de sessão ausente")
                val cart = cartRepository.getCart(cartId)
                call.respond(cart)
            }

            post {
                val cartId = call.getCartIdentifier() ?: return@post call.respond(HttpStatusCode.BadRequest, "ID de sessão ausente")
                val items = call.receive<List<CartItem>>()
                cartRepository.saveCart(cartId, items)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}
