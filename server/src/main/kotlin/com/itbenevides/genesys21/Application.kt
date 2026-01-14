package com.itbenevides.genesys21

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
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

        // Endpoint para validar o Token do Firebase
        post("/validate-token") {
            val token = call.receiveText()
            try {
                // O Admin SDK valida o token enviado pelo App (Android/iOS/Web)
                val decodedToken = FirebaseAuth.getInstance().verifyIdToken(token)
                val uid = decodedToken.uid
                val email = decodedToken.email
                
                logger.info("Usuário validado: $email ($uid)")
                call.respond(mapOf("status" to "success", "uid" to uid, "email" to email))
            } catch (e: Exception) {
                logger.error("Erro ao validar token: ${e.message}")
                call.respond(HttpStatusCode.Unauthorized, mapOf("status" to "error", "message" to "Token inválido"))
            }
        }
    }
}
