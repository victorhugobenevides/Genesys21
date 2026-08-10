package com.itbenevides.genesys21.routes

import com.itbenevides.genesys21.domain.model.Receipt
import com.itbenevides.genesys21.domain.model.UserPermission
import com.itbenevides.genesys21.domain.model.UserRole
import com.itbenevides.genesys21.domain.repository.UserRepository
import com.itbenevides.genesys21.domain.service.ReceiptParserService
import com.itbenevides.genesys21.data.repository.SqliteReceiptRepository
import com.itbenevides.genesys21.data.service.SefazScraperService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class ParseReceiptRequest(
    val rawText: String = "",
    val imageBase64: String? = null,
    val mimeType: String? = "image/jpeg"
)

fun Route.receiptRoutes(
    parserService: ReceiptParserService,
    receiptRepository: SqliteReceiptRepository,
    userRepository: UserRepository
) {
    val scraperService = SefazScraperService()

    route("/public/receipts") {
        post("/parse") {
            try {
                val request = call.receive<ParseReceiptRequest>()
                val apiKey = System.getenv("GEMINI_API_KEY")

                println("BACKEND: Recebida requisição de parse. Imagem: ${request.imageBase64?.length ?: 0} bytes, Key configurada: ${!apiKey.isNullOrBlank()}")

                // 1. Camada Híbrida: Detectar se é uma URL da SEFAZ
                if (request.rawText.startsWith("http", true) && request.rawText.contains("fazenda", true)) {
                    val scrapedReceipt = scraperService.parseFromUrl(request.rawText)
                    if (scrapedReceipt != null) {
                        call.respond(scrapedReceipt)
                        return@post
                    }
                }

                if (apiKey.isNullOrBlank()) {
                    val result = parserService.parseReceiptFromText(request.rawText)
                    call.respond(result)
                    return@post
                }

                val receipt = parserService.parseReceiptDynamic(
                    rawText = request.rawText,
                    imageBase64 = request.imageBase64,
                    apiKey = apiKey,
                    mimeType = request.mimeType
                )
                call.respond(receipt)
            } catch (e: Exception) {
                val message = e.message ?: ""
                if (message.contains("429")) {
                    call.respond(HttpStatusCode.TooManyRequests, "Limite de uso da IA excedido por hoje. Tente novamente amanhã.")
                } else {
                    call.respond(HttpStatusCode.InternalServerError, message.ifBlank { "Erro ao processar nota" })
                }
            }
        }
    }

    authenticate("firebase") {
        route("/receipts") {
            // Middleware de permissão
            intercept(ApplicationCallPipeline.Call) {
                val principal = call.principal<UserIdPrincipal>() ?: return@intercept call.respond(HttpStatusCode.Unauthorized).also { finish() }
                val user = userRepository.getUserProfile(principal.name).getOrNull()
                val isSuperAdmin = user?.role == UserRole.SUPERADMIN
                val hasPermission = user?.permissions?.contains(UserPermission.MANAGE_RECEIPTS) == true

                if (!isSuperAdmin && !hasPermission) {
                    call.respond(HttpStatusCode.Forbidden, "Sem permissão para gerenciar notas")
                    return@intercept finish()
                }
            }

            get {
                val principal = call.principal<UserIdPrincipal>()!!
                receiptRepository.getReceiptsByUser(principal.name).onSuccess {
                    call.respond(it)
                }.onFailure {
                    call.respond(HttpStatusCode.InternalServerError, it.message ?: "Erro ao buscar notas")
                }
            }

            post {
                val principal = call.principal<UserIdPrincipal>()!!
                val receipt = call.receive<Receipt>()
                receiptRepository.saveReceiptWithUser(receipt, principal.name).onSuccess {
                    call.respond(HttpStatusCode.Created)
                }.onFailure {
                    call.respond(HttpStatusCode.InternalServerError, it.message ?: "Erro ao salvar nota")
                }
            }

            delete("/{id}") {
                val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                receiptRepository.deleteReceipt(id).onSuccess {
                    call.respond(HttpStatusCode.OK)
                }.onFailure {
                    call.respond(HttpStatusCode.InternalServerError, it.message ?: "Erro ao excluir nota")
                }
            }
        }
    }
}
