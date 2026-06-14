package com.itbenevides.genesys21

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.itbenevides.genesys21.data.database.DatabaseFactory
import com.itbenevides.genesys21.data.repository.SqliteCartRepository
import com.itbenevides.genesys21.data.repository.SqliteOrderRepository
import com.itbenevides.genesys21.data.repository.SqlitePageRepository
import com.itbenevides.genesys21.routes.cartRoutes
import com.itbenevides.genesys21.routes.categoryRoutes
import com.itbenevides.genesys21.routes.orderRoutes
import com.itbenevides.genesys21.routes.pageRoutes
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
    
    // CORREÇÃO: Usar banco em memória se estiver em ambiente de teste
    val isTesting = environment.config.propertyOrNull("ktor.testing")?.getString() == "true"
    if (isTesting) {
        DatabaseFactory.init("jdbc:sqlite::memory:?cache=shared")
    } else {
        DatabaseFactory.init()
    }

    val pageRepository = SqlitePageRepository()
    val cartRepository = SqliteCartRepository()
    val orderRepository = SqliteOrderRepository()

    // CORREÇÃO: Usar pasta de uploads relativa em ambiente local/teste
    val uploadPath = if (isTesting) "build/test-uploads" else "/app/uploads"
    val uploadDir = File(uploadPath).absoluteFile
    if (!uploadDir.exists()) uploadDir.mkdirs()
    
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            logger.error("Erro Interno: ${cause.message}", cause)
            call.respond(HttpStatusCode.InternalServerError, cause.message ?: "Erro desconhecido")
        }
    }

    // Otimização: Compressão Gzip agressiva para reduzir transferência de dados (JS/JSON)
    install(Compression) {
        gzip { priority = 1.0 }
        deflate { priority = 10.0; minimumSize(1024) }
    }

    install(ContentNegotiation) { 
        json(Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = true }) 
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

    routing {
        // Otimização: Cache agressivo para imagens de upload (30 dias)
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

        // Rota para Metadados Dinâmicos (SEO/Compartilhamento) com Redirecionamento
        get("/p/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val page = pageRepository.getPublicPage(id).getOrNull()
            
            val title = page?.title ?: "Página não encontrada"
            val description = "Confira esta página incrível."
            val siteName = "Social Bio"
            
            val html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>$title</title>
                    <meta property="og:title" content="$title">
                    <meta property="og:description" content="$description">
                    <meta property="og:site_name" content="$siteName">
                    <meta property="og:type" content="website">
                    <meta name="twitter:card" content="summary_large_image">
                    <meta name="twitter:title" content="$title">
                    <meta name="twitter:description" content="$description">
                    <script>
                        window.location.replace("/?pageId=$id");
                    </script>
                </head>
                <body>
                    Redirecionando para $title...
                </body>
                </html>
            """.trimIndent()
            
            call.respondText(html, ContentType.Text.Html)
        }

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
                            val channel = part.provider()
                            fileBytes = channel.toByteArray()
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
