package com.itbenevides.genesys21

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.itbenevides.genesys21.data.repository.InMemoryPageRepository
import com.itbenevides.genesys21.routes.pageRoutes
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val logger = LoggerFactory.getLogger("Application")
    
    // Injeção de Dependência Manual
    val pageRepository = InMemoryPageRepository()

    // 1. Plugins
    install(ContentNegotiation) { json() }
    
    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
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

    // 2. Inicialização do Firebase
    initFirebase(logger)

    // 3. Roteamento
    routing {
        get("/") {
            val total = pageRepository.getPages().size
            call.respondText("Genesys21 API Online. Pages in memory: $total")
        }
        
        // Injetando o repositório nas rotas
        pageRoutes(pageRepository)
    }
}

private fun Application.initFirebase(logger: org.slf4j.Logger) {
    try {
        val fileName = "genesys21-32035-firebase-adminsdk-fbsvc-d57f39d3c3.json"
        val serviceAccount = this::class.java.classLoader.getResourceAsStream(fileName)

        if (serviceAccount != null) {
            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build()

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options)
                logger.info("Firebase Admin inicializado com sucesso!")
            }
        } else {
            logger.warn("Arquivo de service account não encontrado: $fileName")
        }
    } catch (e: Exception) {
        logger.error("Erro ao inicializar Firebase: ${e.message}")
    }
}
