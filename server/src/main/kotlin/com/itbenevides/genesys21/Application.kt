package com.itbenevides.genesys21

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.itbenevides.genesys21.data.database.DatabaseFactory
import com.itbenevides.genesys21.data.repository.SqlitePageRepository
import com.itbenevides.genesys21.routes.pageRoutes
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import net.coobird.thumbnailator.Thumbnails
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*

const val SERVER_PORT = 8080

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val logger = LoggerFactory.getLogger("Application")
    
    DatabaseFactory.init()
    val pageRepository = SqlitePageRepository()

    // FORÇAR CAMINHO ABSOLUTO PARA O DOCKER
    val uploadDir = File("/app/uploads").absoluteFile
    if (!uploadDir.exists()) {
        val created = uploadDir.mkdirs()
        logger.info("Diretório de uploads criado em ${uploadDir.absolutePath}: $created")
    }
    
    logger.info(">>> BACKEND GENESYS21 INICIADO EM: ${uploadDir.absolutePath}")

    install(Compression) {
        gzip { priority = 1.0 }
        deflate { priority = 10.0; minimumSize(1024) }
    }

    install(CachingHeaders) {
        options { call, outgoingContent ->
            when (outgoingContent.contentType?.withoutParameters()) {
                ContentType.Image.JPEG, ContentType.Image.PNG, ContentType.Image.GIF -> 
                    CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 7 * 24 * 60 * 60))
                else -> null
            }
        }
    }

    install(ContentNegotiation) { 
        json(Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = true }) 
    }
    
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowCredentials = true
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
        // ROTA DE IMAGENS - Servindo do caminho absoluto
        get("/uploads/{filename...}") {
            val filename = call.parameters.getAll("filename")?.joinToString("/") ?: ""
            val file = File(uploadDir, filename)
            
            if (file.exists() && file.isFile) {
                call.respondFile(file)
            } else {
                call.respond(HttpStatusCode.NotFound, "Arquivo não encontrado no servidor: $filename")
            }
        }

        get("/api/debug/files") {
            val files = uploadDir.listFiles()?.map { "${it.name} (${it.length()} bytes)" } ?: emptyList()
            call.respondText("DIR: ${uploadDir.absolutePath}\nFILES:\n${files.joinToString("\n")}")
        }

        get("/") { call.respondText("Genesys21 API Online") }

        authenticate("firebase") {
            post("/upload") {
                val multipart = call.receiveMultipart()
                var fileName = ""
                var fileBytes: ByteArray? = null

                multipart.forEachPart { part ->
                    if (part is PartData.FileItem) {
                        val ext = part.originalFileName?.substringAfterLast(".") ?: "jpg"
                        fileName = "${UUID.randomUUID()}.$ext"
                        fileBytes = part.streamProvider().readBytes()
                    }
                    part.dispose()
                }

                if (fileBytes != null) {
                    val file = File(uploadDir, fileName)
                    try {
                        val outputStream = ByteArrayOutputStream()
                        Thumbnails.of(ByteArrayInputStream(fileBytes))
                            .size(1200, 1200)
                            .keepAspectRatio(true)
                            .outputQuality(0.80) 
                            .toOutputStream(outputStream)
                        file.writeBytes(outputStream.toByteArray())
                    } catch (e: Exception) {
                        file.writeBytes(fileBytes!!)
                    }
                    call.respondText("/uploads/$fileName")
                } else {
                    call.respond(HttpStatusCode.BadRequest, "Arquivo inválido")
                }
            }
        }

        pageRoutes(pageRepository)
    }
}

private fun Application.initFirebase(logger: org.slf4j.Logger) {
    try {
        val fileName = "firebase-adminsdk.json"
        val stream = this::class.java.classLoader.getResourceAsStream(fileName)
            ?: if (File(fileName).exists()) File(fileName).inputStream() else null

        if (stream != null) {
            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(stream))
                .build()
            if (FirebaseApp.getApps().isEmpty()) FirebaseApp.initializeApp(options)
        }
    } catch (e: Exception) {
        logger.error("Erro Firebase: ${e.message}")
    }
}
