package com.itbenevides.genesys21.routes

import com.itbenevides.genesys21.domain.service.PageAIGeneratorService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class GeneratePageRequest(val prompt: String)

fun Route.aiRoutes(aiService: PageAIGeneratorService) {
    route("/public/ai") {
        post("/generate-page") {
            try {
                val request = call.receive<GeneratePageRequest>()
                val apiKey = System.getenv("GEMINI_API_KEY")

                if (apiKey.isNullOrBlank()) {
                    return@post call.respond(HttpStatusCode.ServiceUnavailable, "IA não configurada no servidor.")
                }

                val page = aiService.generatePage(request.prompt, apiKey)
                call.respond(page)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.message ?: "Erro na geração por IA")
            }
        }
    }
}
