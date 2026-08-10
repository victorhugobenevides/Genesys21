package com.itbenevides.genesys21

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.itbenevides.genesys21.data.database.DatabaseFactory
import com.itbenevides.genesys21.data.repository.*
import com.itbenevides.genesys21.data.service.GoogleCalendarService
import com.itbenevides.genesys21.data.service.StripeService

import com.itbenevides.genesys21.domain.service.ReceiptParserService
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.routes.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit as rateLimitRoute
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import kotlinx.coroutines.launch
import net.coobird.thumbnailator.Thumbnails
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import kotlin.time.Duration.Companion.seconds
import kotlinx.serialization.json.Json

const val SERVER_PORT = 8080

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val logger = LoggerFactory.getLogger("Application")
    logger.info("SERVIDOR: Genesys21 v1.0.1 (Stable Gemini 2.0) iniciando...")

    val isTesting = environment.config.propertyOrNull("ktor.testing")?.getString() == "true"
    val shouldRebuild = environment.config.propertyOrNull("ktor.db.rebuild")?.getString() == "true" || System.getenv("DB_REBUILD") == "true"

    if (isTesting) {
        val testDbId = System.nanoTime()
        DatabaseFactory.init("jdbc:sqlite:file:testdb-$testDbId?mode=memory&cache=shared", rebuild = true)
    } else {
        logger.info("Inicializando Banco de Dados (rebuild=$shouldRebuild)...")
        DatabaseFactory.init(rebuild = shouldRebuild)
        logger.info("Banco de Dados inicializado com sucesso.")
    }

    val pageRepository = SqlitePageRepository()
    val cartRepository = SqliteCartRepository()
    val googleCalendarService = GoogleCalendarService()
    val bookingRepository = SqliteBookingRepository(googleCalendarService)
    val orderRepository = SqliteOrderRepository(bookingRepository)
    val userRepository = SqliteUserRepository()
    val addressRepository = SqliteAddressRepository()
    val storeRepository = SqliteStoreRepository()
    val stripeService = StripeService()

    // Log de segurança para confirmar se o Gemini está configurado (sem mostrar a chave inteira)
    val geminiKey = System.getenv("GEMINI_API_KEY")
    if (geminiKey.isNullOrBlank()) {
        logger.warn("SERVIDOR: GEMINI_API_KEY não encontrada! Verifique os Secrets do CircleCI.")
    } else {
        val mask = if (geminiKey.length > 8) geminiKey.take(4) + "..." + geminiKey.takeLast(4) else "***"
        logger.info("SERVIDOR: Gemini AI configurado. Key detectada: $mask")
    }

    val client = HttpClient(io.ktor.client.engine.java.Java)
    val receiptParserService = ReceiptParserService(client)

    val uploadPath = if (isTesting) "build/test-uploads" else "/app/uploads"
    val uploadDir = File(uploadPath).absoluteFile
    if (!uploadDir.exists()) uploadDir.mkdirs()

    install(StatusPages) {
        exception<io.ktor.serialization.JsonConvertException> { call, cause ->
            logger.error("ERRO DE SERIALIZAÇÃO (JSON): ${cause.message}")
            call.respond(HttpStatusCode.BadRequest, "Erro no formato dos dados: ${cause.message}")
        }
        exception<Throwable> { call, cause ->
            val isProd = System.getenv("PROD_MODE") == "true"
            logger.error("Erro Interno: ${cause.message}", cause)
            if (isProd) call.respond(HttpStatusCode.InternalServerError, "Ocorreu um erro inesperado.")
            else call.respond(HttpStatusCode.InternalServerError, cause.message ?: "Erro desconhecido")
        }
    }

    install(Compression) {
        gzip { priority = 1.0 }
        deflate { priority = 10.0; minimumSize(1024) }
    }

    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = true; coerceInputValues = true })
    }

    install(XForwardedHeaders) {}

    install(DefaultHeaders) {
        header(HttpHeaders.Server, "GenesysServer")
        header("X-Frame-Options", "DENY")
        header("X-Content-Type-Options", "nosniff")
        header("X-XSS-Protection", "1; mode=block")
        header("Content-Security-Policy", "default-src 'self'; script-src 'self' https://www.gstatic.com; style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; font-src 'self' https://fonts.gstatic.com; img-src 'self' data: https://picsum.photos https://ui-avatars.com;")
    }

    install(CORS) {
        allowHost("victorbenevides.dev", schemes = listOf("http", "https"))
        allowHost("www.victorbenevides.dev", schemes = listOf("http", "https"))
        allowHost("staging.victorbenevides.dev", schemes = listOf("http", "https"))
        allowHost("radarani.site", schemes = listOf("http", "https"))
        allowHost("www.radarani.site", schemes = listOf("http", "https"))
        allowHost("localhost:8080"); allowHost("localhost:8081"); allowHost("localhost:3000"); allowHost("0.0.0.0:8080")
        allowHeader(HttpHeaders.Authorization); allowHeader(HttpHeaders.ContentType); allowHeader(HttpHeaders.CacheControl)
        allowHeader("X-Cart-Session-Id"); allowMethod(HttpMethod.Options); allowMethod(HttpMethod.Get); allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put); allowMethod(HttpMethod.Patch); allowMethod(HttpMethod.Delete); allowCredentials = true; maxAgeInSeconds = 3600
    }

    install(Authentication) {
        bearer("firebase") {
            authenticate { credential ->
                try {
                    val decodedToken = FirebaseAuth.getInstance().verifyIdToken(credential.token)
                    UserIdPrincipal(decodedToken.uid)
                } catch (e: Exception) { null }
            }
        }
    }

    initFirebase(logger)

    (this as kotlinx.coroutines.CoroutineScope).launch {
        try {
            com.itbenevides.genesys21.data.service.AuditLogger.cleanupOldLogs(months = 12)
            logger.info("AUDITORIA: Limpeza de logs antigos concluída.")
        } catch (e: Exception) { logger.error("AUDITORIA: Erro ao limpar logs: ${e.message}") }
    }

    logger.info("SERVIDOR: Pronto e ouvindo na porta $SERVER_PORT")

    routing {
        authenticate("firebase") { post("/api/login_check") { call.respond(HttpStatusCode.OK) } }

        get("/uploads/{filename...}") {
            val filename = call.parameters.getAll("filename")?.joinToString("/") ?: ""
            val file = File(uploadDir, filename)
            if (file.exists() && file.isFile) {
                call.response.header(HttpHeaders.CacheControl, "public, max-age=2592000")
                call.respondFile(file)
            } else call.respond(HttpStatusCode.NotFound)
        }

        get("/p/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val page = pageRepository.getPublicPage(id).getOrNull()
            val title = page?.title ?: "Página não encontrada"
            val description = page?.components?.filterIsInstance<PageComponent.ProfileHeader>()?.firstOrNull()?.bio ?: "Confira esta vitrine incrível na Genesys21."
            val rawImage = page?.components?.filterIsInstance<PageComponent.ProfileHeader>()?.firstOrNull()?.imageUrl ?: ""
            val ogImage = if (rawImage.startsWith("/uploads/")) {
                val host = call.request.header(HttpHeaders.Host) ?: "genesys21.com"
                val scheme = if (host.contains("localhost")) "http" else "https"
                "$scheme://$host$rawImage"
            } else rawImage

            val html = """<!DOCTYPE html><html><head><meta charset="UTF-8"><title>$title</title><meta name="description" content="$description"><meta property="og:type" content="website"><meta property="og:title" content="$title"><meta property="og:description" content="$description"><meta property="og:image" content="$ogImage"><script>window.location.replace("/?pageId=$id");</script></head><body>Redirecionando para $title...</body></html>""".trimIndent()
            call.respondText(html, ContentType.Text.Html)
        }

        get("/sitemap.xml") {
            val ids = pageRepository.getAllPublicPageIds().getOrDefault(emptyList())
            val host = call.request.header(HttpHeaders.Host) ?: "genesys21.com"
            val scheme = if (host.contains("localhost")) "http" else "https"
            val baseUrl = "$scheme://$host"
            val sitemap = buildString {
                append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n")
                append("  <url><loc>$baseUrl/</loc><priority>1.0</priority></url>\n")
                ids.forEach { id -> append("  <url><loc>$baseUrl/p/$id</loc><priority>0.8</priority></url>\n") }
                append("</urlset>")
            }
            call.respondText(sitemap, ContentType.Text.Xml)
        }

        get("/") { call.respondText("API Online") }

        route("/api") {
            userRoutes(userRepository)
            adminRoutes(userRepository)
            pageRoutes(pageRepository)
            cartRoutes(cartRepository)
            orderRoutes(orderRepository, storeRepository, stripeService)
            categoryRoutes(pageRepository)
            bookingRoutes(bookingRepository)
            addressRoutes(addressRepository)
            storeRoutes(storeRepository)
            shippingRoutes(storeRepository)
            connectRoutes(userRepository, storeRepository)
            receiptRoutes(receiptParserService)

            authenticate("firebase") {
                post("/upload") {
                    val multipart = call.receiveMultipart()
                    var fileName = ""
                    var fileBytes: ByteArray? = null
                    multipart.forEachPart { part ->
                        if (part is PartData.FileItem) {
                            val contentType = part.contentType?.toString() ?: ""
                            if (contentType.startsWith("image/")) {
                                val ext = part.originalFileName?.substringAfterLast(".") ?: "jpg"
                                fileName = "${UUID.randomUUID()}.$ext"
                                fileBytes = part.provider().toByteArray()
                            }
                        }
                        part.dispose()
                    }
                    if (fileBytes != null) {
                        val file = File(uploadDir, fileName)
                        try {
                            val outputStream = ByteArrayOutputStream()
                            Thumbnails.of(ByteArrayInputStream(fileBytes)).size(1200, 1200).outputFormat("jpg").outputQuality(0.8).toOutputStream(outputStream)
                            file.writeBytes(outputStream.toByteArray())
                        } catch (e: Exception) { file.writeBytes(fileBytes!!) }
                        call.respondText("/uploads/$fileName")
                    } else call.respond(HttpStatusCode.BadRequest)
                }
            }
        }
    }
}

private fun Application.initFirebase(logger: org.slf4j.Logger) {
    try {
        val fileName = "firebase-adminsdk.json"
        val file = File(fileName)
        val stream = this::class.java.classLoader.getResourceAsStream(fileName) ?: if (file.exists()) file.inputStream() else null
        if (stream != null) {
            val options = FirebaseOptions.builder().setCredentials(GoogleCredentials.fromStream(stream)).build()
            if (FirebaseApp.getApps().isEmpty()) FirebaseApp.initializeApp(options)
        }
    } catch (e: Exception) { logger.error("Erro Crítico no Firebase: ${e.message}", e) }
}
