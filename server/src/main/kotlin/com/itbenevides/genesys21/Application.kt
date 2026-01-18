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
    
    DatabaseFactory.init()
    val pageRepository = SqlitePageRepository()

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
        get("/") {
            val total = pageRepository.getPages("").size
            call.respondText("Genesys21 API Online. Pages in DB: $total")
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
                    val folder = File("uploads")
                    if (!folder.exists()) folder.mkdirs()
                    
                    val file = File(folder, fileName)
                    file.writeBytes(fileBytes!!)
                    
                    val publicHost = System.getenv("PUBLIC_HOST") ?: call.request.header("X-Forwarded-Host") ?: call.request.host()
                    val protocol = call.request.header("X-Forwarded-Proto") ?: call.request.local.scheme
                    
                    val url = "$protocol://$publicHost:8080/uploads/$fileName"
                    
                    logger.info("Upload processado para: $url")
                    call.respondText(url)
                } else {
                    call.respond(HttpStatusCode.BadRequest, "Arquivo não enviado")
                }
            }
        }

        staticFiles("/uploads", File("uploads"))
        pageRoutes(pageRepository)
    }
}

private fun Application.initFirebase(logger: org.slf4j.Logger) {
    try {
        val fileName = "firebase-adminsdk.json"
        val stream = this::class.java.classLoader.getResourceAsStream(fileName)
            ?: if (File(fileName).exists()) File(fileName).inputStream() else null

        if (stream == null) {
            logger.warn("AVISO: Arquivo $fileName não encontrado. Autenticação Firebase não funcionará.")
            return
        }

        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(stream))
            .build()

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options)
            logger.info("Firebase Admin inicializado com sucesso!")
        }
    } catch (e: Exception) {
        logger.error("Erro crítico ao inicializar Firebase: ${e.message}")
    }
}
