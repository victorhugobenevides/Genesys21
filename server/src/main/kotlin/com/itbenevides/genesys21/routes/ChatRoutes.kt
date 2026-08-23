package com.itbenevides.genesys21.routes

import com.itbenevides.genesys21.domain.model.ChatMessage
import com.itbenevides.genesys21.domain.repository.ChatRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.chatRoutes(chatRepository: ChatRepository) {
    route("/chat") {
        get("/{refId}") {
            val refId = call.parameters["refId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            chatRepository.getMessagesByRefId(refId).onSuccess {
                call.respond(it)
            }.onFailure {
                call.respond(HttpStatusCode.InternalServerError, it.message ?: "Erro ao buscar mensagens")
            }
        }

        post {
            val message = call.receive<ChatMessage>()
            chatRepository.sendMessage(message).onSuccess {
                call.respond(HttpStatusCode.Created)
            }.onFailure {
                call.respond(HttpStatusCode.InternalServerError, it.message ?: "Erro ao enviar mensagem")
            }
        }
    }
}
