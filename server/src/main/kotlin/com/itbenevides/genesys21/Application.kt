package com.itbenevides.genesys21

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.itbenevides.genesys21.data.database.DatabaseFactory
import com.itbenevides.genesys21.data.repository.*
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.routes.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
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

    val isTesting = environment.config.propertyOrNull("ktor.testing")?.getString() == "true"
    val shouldRebuild = environment.config.propertyOrNull("ktor.db.rebuild")?.getString() == "true" || System.getenv("DB_REBUILD") == "true"

    if (isTesting) {
        DatabaseFactory.init("jdbc:sqlite::memory:?cache=shared", rebuild = true)
    } else {
        logger.info("Inicializando Banco de Dados (rebuild=$shouldRebuild)...")
        DatabaseFactory.init(rebuild = shouldRebuild)
        logger.info("Banco de Dados inicializado com sucesso.")
    }

    val pageRepository = SqlitePageRepository()
    val cartRepository = SqliteCartRepository()
    val orderRepository = SqliteOrderRepository()
    val bookingRepository = SqliteBookingRepository()
    val userRepository = SqliteUserRepository()

    val uploadPath = if (isTesting) "build/test-uploads" else "/app/uploads"
    val uploadDir = File(uploadPath).absoluteFile
    if (!uploadDir.exists()) uploadDir.mkdirs()

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            val isProd = System.getenv("PROD_MODE") == "true"
            logger.error("Erro Interno: ${cause.message}", cause)

            if (isProd) {
                call.respond(HttpStatusCode.InternalServerError, "Ocorreu um erro inesperado no servidor.")
            } else {
                call.respond(HttpStatusCode.InternalServerError, cause.message ?: "Erro desconhecido")
            }
        }
    }

    install(Compression) {
        gzip { priority = 1.0 }
        deflate {
            priority = 10.0
            minimumSize(1024)
        }
    }

    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            },
        )
    }

    install(DefaultHeaders) {
        header(HttpHeaders.Server, "GenesysServer")
        header("X-Frame-Options", "DENY")
        header("X-Content-Type-Options", "nosniff")
        header("X-XSS-Protection", "1; mode=block")
        header("Content-Security-Policy", "default-src 'self'; script-src 'self' https://www.gstatic.com; style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; font-src 'self' https://fonts.gstatic.com; img-src 'self' data: https://picsum.photos;")
    }

    /*
    install(RateLimit) {
        global {
            rateLimit(limit = 100, period = 60.seconds)
        }
        register(RateLimitName("login")) {
            rateLimit(limit = 5, period = 60.seconds)
        }
    }
    */

    install(CORS) {
        val prodHosts = listOf("radarani.site", "victorbenevides.dev")
        val stagingHosts = listOf("localhost:8081", "localhost:8080")

        prodHosts.forEach { allowHost(it, schemes = listOf("https")) }
        stagingHosts.forEach { allowHost(it, schemes = listOf("http", "https")) }

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
        // Aplica rate limit na rota de login e upload
        authenticate("firebase") {
            // rateLimit(RateLimitName("login")) {
                post("/api/login_check") { call.respond(HttpStatusCode.OK) }
            // }
        }

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
            val page = pageRepository.getPublicPage(id).getOrNull()

            val title = page?.title ?: "Página não encontrada"
            val siteName = "Social Bio"

            val description =
                page?.components?.filterIsInstance<PageComponent.ProfileHeader>()?.firstOrNull()?.bio
                    ?: page?.components?.filterIsInstance<PageComponent.Text>()?.firstOrNull()?.content
                    ?: "Confira esta vitrine incrível na Genesys21."

            val rawImage =
                page?.components?.filterIsInstance<PageComponent.ProfileHeader>()?.firstOrNull()?.imageUrl
                    ?: page?.components?.filterIsInstance<PageComponent.Image>()?.firstOrNull()?.url
                    ?: ""

            val ogImage =
                if (rawImage.startsWith("/uploads/")) {
                    val host = call.request.header(HttpHeaders.Host) ?: "genesys21.com"
                    val scheme = if (host.contains("localhost")) "http" else "https"
                    "$scheme://$host$rawImage"
                } else {
                    rawImage
                }

            val html =
                """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>$title</title>
                    <meta name="description" content="$description">

                    <!-- Open Graph / Facebook -->
                    <meta property="og:type" content="website">
                    <meta property="og:url" content="${call.request.uri}">
                    <meta property="og:title" content="$title">
                    <meta property="og:description" content="$description">
                    <meta property="og:image" content="$ogImage">
                    <meta property="og:site_name" content="$siteName">

                    <!-- Twitter -->
                    <meta name="twitter:card" content="summary_large_image">
                    <meta name="twitter:url" content="${call.request.uri}">
                    <meta name="twitter:title" content="$title">
                    <meta name="twitter:description" content="$description">
                    <meta name="twitter:image" content="$ogImage">

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

        get("/sitemap.xml") {
            val ids = pageRepository.getAllPublicPageIds().getOrDefault(emptyList())
            val host = call.request.header(HttpHeaders.Host) ?: "genesys21.com"
            val scheme = if (host.contains("localhost")) "http" else "https"
            val baseUrl = "$scheme://$host"

            val sitemap =
                buildString {
                    append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                    append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n")
                    append("  <url><loc>$baseUrl/</loc><priority>1.0</priority></url>\n")
                    ids.forEach { id ->
                        append("  <url><loc>$baseUrl/p/$id</loc><priority>0.8</priority></url>\n")
                    }
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
            orderRoutes(orderRepository)
            categoryRoutes(pageRepository)
            bookingRoutes(bookingRepository)

            authenticate("firebase") {
                post("/upload") {
                    val multipart = call.receiveMultipart()
                    var fileName = ""
                    var fileBytes: ByteArray? = null

                    multipart.forEachPart { part ->
                        if (part is PartData.FileItem) {
                            // Validação de Tipo de Arquivo
                            val contentType = part.contentType?.toString() ?: ""
                            if (!contentType.startsWith("image/")) {
                                return@forEachPart call.respond(HttpStatusCode.UnsupportedMediaType, "Apenas imagens são permitidas.")
                            }

                            val ext = part.originalFileName?.substringAfterLast(".") ?: "jpg"
                            fileName = "${UUID.randomUUID()}.$ext"
                            val channel = part.provider()
                            fileBytes = channel.toByteArray()

                            // Validação de Tamanho (Max 10MB)
                            if (fileBytes != null && fileBytes!!.size > 10 * 1024 * 1024) {
                                fileBytes = null
                                return@forEachPart call.respond(HttpStatusCode.PayloadTooLarge, "Imagem muito grande (máximo 10MB).")
                            }
                        }
                        part.dispose()
                    }

                    if (fileBytes != null) {
                        val file = File(uploadDir, fileName)

                        try {
                            val outputStream = ByteArrayOutputStream()
                            Thumbnails.of(ByteArrayInputStream(fileBytes))
                                .size(1200, 1200)
                                .outputFormat("jpg")
                                .outputQuality(0.8)
                                .toOutputStream(outputStream)
                            file.writeBytes(outputStream.toByteArray())
                        } catch (e: Exception) {
                            file.writeBytes(fileBytes!!)
                        }

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
        val stream =
            this::class.java.classLoader.getResourceAsStream(fileName)
                ?: if (File(fileName).exists()) File(fileName).inputStream() else null
        if (stream != null) {
            val options = FirebaseOptions.builder().setCredentials(GoogleCredentials.fromStream(stream)).build()
            if (FirebaseApp.getApps().isEmpty()) FirebaseApp.initializeApp(options)
        }
    } catch (e: Exception) {
        logger.error("Erro Firebase: ${e.message}")
    }
}
