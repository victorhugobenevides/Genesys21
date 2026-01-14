package com.itbenevides.genesys21

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import io.ktor.http.*
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
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

// Movemos o DB para fora da função module para que ele persista enquanto o processo estiver vivo, 
// mesmo se o module for recarregado em desenvolvimento.
val pagesDB = ConcurrentHashMap<String, Page>()

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val logger = LoggerFactory.getLogger("Application")

    install(ContentNegotiation) {
        json()
    }

    // Configuração de CORS (Essencial para Web funcionar)
    install(CORS) {
        anyHost() 
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
    }

    // Configuração de Autenticação
    install(Authentication) {
        bearer("firebase") {
            authenticate { credential ->
                try {
                    val decodedToken = FirebaseAuth.getInstance().verifyIdToken(credential.token)
                    UserIdPrincipal(decodedToken.uid)
                } catch (e: Exception) {
                    logger.error("Falha ao validar token: ${e.message}")
                    null
                }
            }
        }
    }

    // Inicialização do Firebase Admin
    try {
        val serviceAccount = this::class.java.classLoader
            .getResourceAsStream("genesys21-32035-firebase-adminsdk-fbsvc-d57f39d3c3.json")

        if (serviceAccount != null) {
            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build()

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options)
                logger.info("Firebase Admin inicializado!")
            }
        }
    } catch (e: Exception) {
        logger.error("Erro Firebase: ${e.message}")
    }

    routing {
        get("/") {
            call.respondText("Genesys21 API Online. Total pages: ${pagesDB.size}")
        }

        route("/pages") {
            get {
                val list = pagesDB.values.toList()
                logger.info("Retornando ${list.size} páginas")
                call.respond(list)
            }

            authenticate("firebase") {
                post {
                    val page = call.receive<Page>()
                    pagesDB[page.id] = page
                    logger.info("Página criada: ${page.id}")
                    call.respond(HttpStatusCode.Created, page)
                }
                put {
                    val page = call.receive<Page>()
                    if (pagesDB.containsKey(page.id)) {
                        pagesDB[page.id] = page
                        logger.info("Página editada: ${page.id}")
                        call.respond(HttpStatusCode.OK, page)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }
                delete("/{id}") {
                    val id = call.parameters["id"]
                    if (id != null && pagesDB.remove(id) != null) {
                        logger.info("Página removida: $id")
                        call.respond(HttpStatusCode.OK)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }
            }
        }
    }
}
