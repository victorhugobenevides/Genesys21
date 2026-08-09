package com.itbenevides.genesys21.routes

import com.itbenevides.genesys21.domain.service.ReceiptParserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class ParseReceiptRequest(
    val rawText: String = "",
    val imageBase64: String? = null
)

fun Route.receiptRoutes(parserService: ReceiptParserService) {
    val scraperService = com.itbenevides.genesys21.data.service.SefazScraperService()

    route("/public/receipts") {
        post("/parse") {
            try {
                val request = call.receive<ParseReceiptRequest>()
                val apiKey = System.getenv("GEMINI_API_KEY")

                // 1. Camada Híbrida: Detectar se é uma URL da SEFAZ
                if (request.rawText.startsWith("http", true) && request.rawText.contains("fazenda", true)) {
                    println("BACKEND: URL detectada, tentando Scraper...")
                    val scrapedReceipt = scraperService.parseFromUrl(request.rawText)
                    if (scrapedReceipt != null) {
                        call.respond(scrapedReceipt)
                        return@post
                    }
                    println("BACKEND: Scraper falhou ou não compatível, tentando IA...")
                }

                if (apiKey.isNullOrBlank()) {
                    // Fallback para parse local se a chave não estiver configurada no servidor
                    val result = parserService.parseReceiptFromText(request.rawText)
                    call.respond(result)
                    return@post
                }

                val receipt = parserService.parseReceiptDynamic(
                    rawText = request.rawText,
                    imageBase64 = request.imageBase64,
                    apiKey = apiKey
                )
                call.respond(receipt)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.message ?: "Erro ao processar nota")
            }
        }
    }
}
