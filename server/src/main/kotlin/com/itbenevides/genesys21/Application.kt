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
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.http.content.*
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

    // RESOLVE O DIRETÓRIO DE UPLOADS DE FORMA ABSOLUTA
    val uploadDir = File("uploads").absoluteFile
    if (!uploadDir.exists()) {
        uploadDir.mkdirs()
        logger.info("Criando diretório de uploads em: ${uploadDir.absolutePath}")
    } else {
        logger.info("Diretório de uploads detectado em: ${uploadDir.absolutePath}")
    }

    install(ContentNegotiation) { 
        json(Json {
            ignoreUnknownKeys = true 
            isLenient = true
            encodeDefaults = true
        }) 
    }
    
    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowCredentials = true
    }

    install(Authentication) {
        bearer("firebase") {
            authenticate { credential ->
                try {
                    val decodedToken = FirebaseAuth.getInstance().verifyIdToken(credential.token)
                    UserIdPrincipal(decodedToken.uid)
                } catch (e: Exception) {
                    logger.error("Auth Failure: ${e.message}")
                    null
                }
            }
        }
    }

    initFirebase(logger)

    routing {
        // 1. PRIORIDADE: FAVICON (Silencia erro 404 no navegador)
        get("/favicon.ico") {
            call.respond(HttpStatusCode.NoContent)
        }

        // 2. SERVIR ARQUIVOS ESTÁTICOS
        staticFiles("/uploads", uploadDir)

        get("/") {
            val total = pageRepository.getPages("").size
            call.respondText("Genesys21 API Online. Pages in DB: $total | Uploads: ${uploadDir.listFiles()?.size ?: 0}")
        }

        authenticate("firebase") {
            post("/upload") {
                val multipart = call.receiveMultipart()
                var fileName = ""
                var fileBytes: ByteArray? = null

                multipart.forEachPart { part ->
                    if (part is PartData.FileItem) {
                        fileName = "${UUID.randomUUID()}.${part.originalFileName?.substringAfterLast(".") ?: "jpg"}"
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
                            .outputFormat("jpg")
                            .outputQuality(0.85)
                            .toOutputStream(outputStream)
                        file.writeBytes(outputStream.toByteArray())
                    } catch (e: Exception) {
                        file.writeBytes(fileBytes!!)
                    }
                    
                    // Retorna o caminho relativo que será resolvido pelo staticFiles
                    call.respondText("/uploads/$fileName")
                } else {
                    call.respond(HttpStatusCode.BadRequest, "Arquivo não enviado")
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

        if (stream == null) return

        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(stream))
            .build()

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options)
            logger.info("Firebase Admin inicializado!")
        }
    } catch (e: Exception) {
        logger.error("Erro Firebase: ${e.message}")
    }
}
