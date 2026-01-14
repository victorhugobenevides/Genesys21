package com.itbenevides.genesys21

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val logger = LoggerFactory.getLogger("Application")
    
    // Inicialização do Firebase Admin
    try {
        val serviceAccount = this::class.java.classLoader
            .getResourceAsStream("genesys21-32035-firebase-adminsdk-fbsvc-d57f39d3c3.json")

        if (serviceAccount == null) {
            logger.error("Arquivo de credenciais do Firebase não encontrado no resources.")
        } else {
            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build()

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options)
                logger.info("Firebase Admin inicializado com sucesso!")
            }
        }
    } catch (e: Exception) {
        logger.error("Erro ao inicializar Firebase Admin: ${e.message}")
    }

    routing {
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()} | Firebase Admin: Ativo")
        }
    }
}
