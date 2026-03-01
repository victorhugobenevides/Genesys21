package com.itbenevides.genesys21

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.itbenevides.genesys21.data.database.DatabaseFactory
import com.itbenevides.genesys21.data.repository.SqliteCartRepository
import com.itbenevides.genesys21.data.repository.SqliteOrderRepository
import com.itbenevides.genesys21.data.repository.SqlitePageRepository
import com.itbenevides.genesys21.routes.*
import com.mercadopago.MercadoPagoConfig
import io.github.cdimascio.dotenv.dotenv
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

const val SERVER_PORT = 8080

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val logger = LoggerFactory.getLogger("Application")
    val dotenv = try { dotenv() } catch (e: Exception) { null }
    val isTestAuth = environment.config.propertyOrNull("genesys.test.auth")?.getString() == "true" ||
        System.getProperty("GENESYS_TEST_AUTH") == "true"

    DatabaseFactory.init()
    val jsonConfig = Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = true }
    
    // Injeção correta com o objeto JSON
    val pageRepository = SqlitePageRepository(jsonConfig)
    val cartRepository = SqliteCartRepository()
    val orderRepository = SqliteOrderRepository()

    val uploadDir = File("uploads").absoluteFile
    if (!uploadDir.exists()) uploadDir.mkdirs()

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            logger.error("Erro Interno: ${cause.message}", cause)
            call.respond(HttpStatusCode.InternalServerError, cause.message ?: "Erro desconhecido")
        }
    }

    install(Compression) {
        gzip { priority = 1.0 }
        deflate { priority = 10.0; minimumSize(1024) }
    }

    install(ContentNegotiation) {
        json(jsonConfig)
    }

    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.CacheControl)
        allowHeader("X-Cart-Session-Id")
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
        allowCredentials = true
        maxAgeInSeconds = 3600
    }

    install(Authentication) {
        bearer("firebase") {
            authenticate { credential ->
                if (isTestAuth && credential.token == "test-token") {
                    return@authenticate UserIdPrincipal("test-user")
                }
                try {
                    val decodedToken = FirebaseAuth.getInstance().verifyIdToken(credential.token)
                    UserIdPrincipal(decodedToken.uid)
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    initFirebase(logger)
    
    dotenv?.get("MERCADOPAGO_ACCESS_TOKEN")?.let {
        MercadoPagoConfig.setAccessToken(it)
    }

    routing {
        get("/uploads/{filename...}") {
            val filename = call.parameters.getAll("filename")?.joinToString("/") ?: ""
            val file = File(uploadDir, filename)
            if (file.exists() && file.isFile) {
                call.response.header(HttpHeaders.CacheControl, "public, max-age=2592000")
                call.respondFile(file)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        get("/p/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val pageResult = pageRepository.getPublicPage(id)
            
            // Corrigido: Usando a forma correta do Result padrão
            val page = pageResult.getOrNull()

            val title = page?.title ?: "Página não encontrada"
            val html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>$title</title>
                    <script>window.location.replace("/?pageId=$id");</script>
                </head>
                <body>Redirecionando para $title...</body>
                </html>
            """.trimIndent()
            call.respondText(html, ContentType.Text.Html)
        }

        // Rotas Públicas de Pedidos (Histórico do Cliente)
        publicOrderRoutes(orderRepository)

        get("/") { call.respondText("API Online") }

        route("/api") {
            pageRoutes(pageRepository)
            cartRoutes(cartRepository)
            orderRoutes(orderRepository)
            categoryRoutes(pageRepository)

            authenticate("firebase") {
                post("/upload") {
                    val multipart = call.receiveMultipart()
                    var fileName = ""
                    var fileBytes: ByteArray? = null
                    multipart.forEachPart { part ->
                        if (part is PartData.FileItem) {
                            val ext = part.originalFileName?.substringAfterLast(".") ?: "jpg"
                            fileName = "${UUID.randomUUID()}.$ext"
                            // Corrigido: Leitura de bytes no Ktor 3
                            fileBytes = part.provider().readRemaining().readBytes()
                        }
                        part.dispose()
                    }
                    if (fileBytes != null) {
                        val file = File(uploadDir, fileName)
                        file.writeBytes(fileBytes!!)
                        call.respondText("/uploads/$fileName")
                    } else {
                        call.respond(HttpStatusCode.BadRequest)
                    }
                }
            }
        }
    }
}

private fun Application.initFirebase(logger: org.slf4j.Logger) {
    try {
        val fileName = "firebase-adminsdk.json"
        val stream = this::class.java.classLoader.getResourceAsStream(fileName)
            ?: if (File(fileName).exists()) File(fileName).inputStream() else null
        if (stream != null) {
            val options = FirebaseOptions.builder().setCredentials(GoogleCredentials.fromStream(stream)).build()
            if (FirebaseApp.getApps().isEmpty()) FirebaseApp.initializeApp(options)
        }
    } catch (e: Exception) {
        logger.error("Erro Firebase: ${e.message}")
    }
}
