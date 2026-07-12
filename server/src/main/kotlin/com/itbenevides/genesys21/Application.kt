package com.itbenevides.genesys21

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.itbenevides.genesys21.data.database.DatabaseFactory
import com.itbenevides.genesys21.data.repository.SqliteCartRepository
import com.itbenevides.genesys21.data.repository.SqliteOrderRepository
import com.itbenevides.genesys21.data.repository.SqlitePageRepository
import com.itbenevides.genesys21.domain.model.PageComponent
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
import net.coobird.thumbnailator.Thumbnails
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import kotlinx.serialization.json.Json

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
            val siteName = "Social Bio"

            // T022: Extração dinâmica de Bio e Imagem
            val description =
                page?.components?.filterIsInstance<PageComponent.ProfileHeader>()?.firstOrNull()?.bio
                    ?: page?.components?.filterIsInstance<PageComponent.Text>()?.firstOrNull()?.content
                    ?: "Confira esta vitrine incrível na Genesys21."

            val rawImage =
                page?.components?.filterIsInstance<PageComponent.ProfileHeader>()?.firstOrNull()?.imageUrl
                    ?: page?.components?.filterIsInstance<PageComponent.Image>()?.firstOrNull()?.url
                    ?: ""

            // Resolve URL absoluta para imagem
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

        // T023: Sitemap Automático
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

                        try {
                            // Otimização: Redimensionar e comprimir imagem (max 1200px)
                            val outputStream = ByteArrayOutputStream()
                            Thumbnails.of(ByteArrayInputStream(fileBytes))
                                .size(1200, 1200)
                                .outputFormat("jpg") // WebP seria melhor mas requer bibliotecas nativas extras
                                .outputQuality(0.8)
                                .toOutputStream(outputStream)
                            file.writeBytes(outputStream.toByteArray())
                        } catch (e: Exception) {
                            // Se falhar a compressão (ex: formato não suportado), salva o original
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
