package com.itbenevides.genesys21

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.itbenevides.genesys21.data.database.DatabaseFactory
import com.itbenevides.genesys21.data.repository.*
import com.itbenevides.genesys21.data.service.BackupService
import com.itbenevides.genesys21.data.service.GoogleCalendarService
import com.itbenevides.genesys21.data.service.StripeService
import com.itbenevides.genesys21.domain.service.PageAIGeneratorService
import com.itbenevides.genesys21.domain.service.ReceiptParserService
import com.itbenevides.genesys21.routes.*
import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit as rateLimitRoute
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    val logger = LoggerFactory.getLogger("Application")

    val isTesting = environment.config.propertyOrNull("ktor.testing")?.getString() == "true"
    val shouldRebuild = environment.config.propertyOrNull("ktor.db.rebuild")?.getString() == "true" || System.getenv("DB_REBUILD") == "true"

    // SINCRONIA DE BANCO: Em teste, usamos o arquivo fornecido pelo runner do teste
    if (isTesting) {
        val testDbPath = environment.config.propertyOrNull("ktor.test.db_path")?.getString() ?: "build/test-default.db"
        DatabaseFactory.init("jdbc:sqlite:$testDbPath", rebuild = false)
    } else {
        DatabaseFactory.init(rebuild = shouldRebuild)
    }

    val pageRepository = SqlitePageRepository()
    val cartRepository = SqliteCartRepository()
    val bookingRepository = SqliteBookingRepository(GoogleCalendarService())
    val orderRepository = SqliteOrderRepository(bookingRepository)
    val userRepository = SqliteUserRepository()
    val addressRepository = SqliteAddressRepository()
    val storeRepository = SqliteStoreRepository()
    val stripeService = StripeService()
    val receiptRepository = SqliteReceiptRepository()
    val domainRepository = SqliteDomainRepository()
    val chatRepository = SqliteChatRepository()
    val draftRepository = SqliteDraftRepository()

    val client = HttpClient(Java)
    val receiptParserService = ReceiptParserService(client)
    val pageAIGeneratorService = PageAIGeneratorService(client)

    install(StatusPages) {
        exception<io.ktor.serialization.JsonConvertException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, "Erro no formato dos dados.")
        }
        exception<Throwable> { call, cause ->
            logger.error("ERRO: ${cause.message}")
            call.respond(HttpStatusCode.InternalServerError, cause.message ?: "Erro desconhecido")
        }
    }

    install(RateLimit) {
        register(RateLimitName("global")) { rateLimiter(limit = 1000, refillPeriod = 60.seconds) }
    }

    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = true })
    }

    install(DefaultHeaders) {
        header("X-Frame-Options", "DENY")
        header("X-Content-Type-Options", "nosniff")
        header("Strict-Transport-Security", "max-age=31536000")
        header("Content-Security-Policy", "default-src 'self';")
    }

    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
    }

    install(Authentication) {
        bearer("firebase") {
            authenticate { credential ->
                if (isTesting && (credential.token == "dummy-token" || credential.token == "valid-token")) {
                    UserIdPrincipal(if (credential.token == "dummy-token") "attacker-id" else "test-user")
                } else {
                    try {
                        val decodedToken = FirebaseAuth.getInstance().verifyIdToken(credential.token)
                        UserIdPrincipal(decodedToken.uid)
                    } catch (e: Exception) { null }
                }
            }
        }
    }

    routing {
        get("/") { call.respondText("API Online") }
        route("/api") {
            userRoutes(userRepository)
            adminRoutes(userRepository)
            systemRoutes(domainRepository, userRepository)
            pageRoutes(pageRepository)
            cartRoutes(cartRepository)
            orderRoutes(orderRepository, storeRepository, stripeService)
            analyticsRoutes(orderRepository)
            categoryRoutes(pageRepository)
            bookingRoutes(bookingRepository)
            chatRoutes(chatRepository)
            draftRoutes(draftRepository)
            addressRoutes(addressRepository)
            storeRoutes(storeRepository)
            shippingRoutes(storeRepository)
            connectRoutes(userRepository, storeRepository)
            receiptRoutes(receiptParserService, receiptRepository, userRepository)
            aiRoutes(pageAIGeneratorService)
        }
    }
}

private fun Application.initFirebase(logger: org.slf4j.Logger) {
    try {
        val file = File("firebase-adminsdk.json")
        if (file.exists()) {
            val options = FirebaseOptions.builder().setCredentials(GoogleCredentials.fromStream(file.inputStream())).build()
            if (FirebaseApp.getApps().isEmpty()) FirebaseApp.initializeApp(options)
        }
    } catch (e: Exception) { logger.error("FIREBASE ERROR: ${e.message}") }
}
