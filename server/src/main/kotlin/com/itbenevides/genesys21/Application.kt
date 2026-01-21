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

    val uploadDir = File("uploads").absoluteFile
    if (!uploadDir.exists()) uploadDir.mkdirs()
    
    logger.info(">>> BACKEND GENESYS21 OTIMIZADO")

    // 1. OTIMIZAÇÃO: COMPRESSÃO DE DADOS (GZIP)
    // Reduz drasticamente o tamanho das respostas JSON e HTML
    install(Compression) {
        gzip {
            priority = 1.0
        }
        deflate {
            priority = 10.0
            minimumSize(1024) // Só comprime se for maior que 1KB
        }
    }

    // 2. OTIMIZAÇÃO: CACHE DE IMAGENS E ARQUIVOS ESTÁTICOS
    install(CachingHeaders) {
        options { call, outgoingContent ->
            when (outgoingContent.contentType?.withoutParameters()) {
                ContentType.Image.JPEG, 
                ContentType.Image.PNG,
                ContentType.Image.GIF -> CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 7 * 24 * 60 * 60)) // 7 Dias de Cache
                ContentType.Text.CSS,
                ContentType.Application.JavaScript,
                ContentType.Text.Html -> CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 24 * 60 * 60)) // 1 Dia para Assets Web
                else -> null
            }
        }
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
                    logger.error("Auth Failure: ${e.message}")
                    null
                }
            }
        }
    }

    initFirebase(logger)

    routing {
        // ROTA DE IMAGENS OTIMIZADA
        get("/uploads/{filename...}") {
            val pathParams = call.parameters.getAll("filename")
            val filename = pathParams?.joinToString("/") ?: ""
            val file = File(uploadDir, filename).absoluteFile
            
            if (file.exists() && file.isFile) {
                // Define o Content-Type explicitamente para ativar o Cache
                val contentType = when (file.extension.lowercase()) {
                    "jpg", "jpeg" -> ContentType.Image.JPEG
                    "png" -> ContentType.Image.PNG
                    "webp" -> ContentType.parse("image/webp")
                    "gif" -> ContentType.Image.GIF
                    else -> ContentType.Application.OctetStream
                }
                
                // O plugin CachingHeaders cuidará do header automaticamente baseado no ContentType
                call.respondFile(file)
            } else {
                call.respond(HttpStatusCode.NotFound, "Imagem não encontrada")
            }
        }

        get("/favicon.ico") {
            call.respondBytes(ByteArray(0), ContentType.Image.XIcon)
        }

        get("/api/debug/files") {
            val fileNames = uploadDir.listFiles()?.map { it.name } ?: emptyList()
            val response = "DIR: ${uploadDir.absolutePath}\nFILES: ${fileNames.joinToString(", ")}"
            call.respondText(response)
        }

        get("/") {
            call.respondText("Genesys21 API Online (GZIP Active)")
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
                        // Redimensiona para no máximo 1200px e reduz qualidade para 80% (Equilíbrio visual/tamanho)
                        Thumbnails.of(ByteArrayInputStream(fileBytes))
                            .size(1200, 1200)
                            .keepAspectRatio(true)
                            .outputFormat("jpg")
                            .outputQuality(0.80) 
                            .toOutputStream(outputStream)
                        file.writeBytes(outputStream.toByteArray())
                    } catch (e: Exception) {
                        file.writeBytes(fileBytes!!)
                    }
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
