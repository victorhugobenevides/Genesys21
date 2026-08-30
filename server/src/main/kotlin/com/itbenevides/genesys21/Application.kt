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

import com.itbenevides.genesys21.domain.service.ReceiptParserService
import com.itbenevides.genesys21.domain.service.PageAIGeneratorService
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.routes.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit as rateLimitRoute
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import kotlinx.coroutines.launch
import net.coobird.thumbnailator.Thumbnails
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import kotlinx.serialization.json.Json

const val SERVER_PORT = 8080

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val logger = LoggerFactory.getLogger("Application")
    logger.info("SERVIDOR: Genesys21 iniciando...")

    val isTesting = environment.config.propertyOrNull("ktor.testing")?.getString() == "true"
    val shouldRebuild = environment.config.propertyOrNull("ktor.db.rebuild")?.getString() == "true" || System.getenv("DB_REBUILD") == "true"

    // UNIFICAÇÃO DE BANCO: Se o teste já inicializou o DatabaseFactory, o servidor REUTILIZA a conexão.
    if (!DatabaseFactory.isInitialized()) {
        if (isTesting) {
            val testJdbcUrl = "jdbc:sqlite:file:genesys_test_db?mode=memory&cache=shared"
            DatabaseFactory.init(testJdbcUrl, rebuild = true)
        } else {
            DatabaseFactory.init(rebuild = shouldRebuild)
        }
    }

    val pageRepository = SqlitePageRepository()
    val cartRepository = SqliteCartRepository()
    val googleCalendarService = GoogleCalendarService()
    val bookingRepository = SqliteBookingRepository(googleCalendarService)
    val orderRepository = SqliteOrderRepository(bookingRepository)
    val userRepository = SqliteUserRepository()
    val addressRepository = SqliteAddressRepository()
    val storeRepository = SqliteStoreRepository()
    val stripeService = StripeService()
    val receiptRepository = SqliteReceiptRepository()
    val domainRepository = SqliteDomainRepository()
    val chatRepository = SqliteChatRepository()
    val draftRepository = SqliteDraftRepository()

    val client = HttpClient(io.ktor.client.engine.java.Java)
    val receiptParserService = ReceiptParserService(client)
    val pageAIGeneratorService = PageAIGeneratorService(client)

    val uploadPath = if (isTesting) "build/test-uploads" else "/app/uploads"
    val uploadDir = File(uploadPath).absoluteFile
    if (!uploadDir.exists()) uploadDir.mkdirs()

    install(StatusPages) {
        exception<io.ktor.serialization.JsonConvertException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, "Erro no formato dos dados.")
        }
        exception<Throwable> { call, cause ->
            logger.error("Erro Interno: ${cause.message}", cause)
            call.respond(HttpStatusCode.InternalServerError, cause.message ?: "Erro desconhecido")
        }
    }

    install(RateLimit) {
        register(RateLimitName("global")) { rateLimiter(limit = 1000, refillPeriod = 60.seconds) }
        register(RateLimitName("sensitive")) { rateLimiter(limit = 1000, refillPeriod = 60.seconds) }
    }

    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = true; coerceInputValues = true })
    }

    install(Authentication) {
        bearer("firebase") {
            authenticate { credential ->
                // Mock simplificado para ambiente de teste se o token for dummy
                if (isTesting && credential.token == "dummy-token") {
                    UserIdPrincipal("attacker-id")
                } else if (isTesting && credential.token == "valid-token") {
                    UserIdPrincipal("test-user")
                } else {
                    try {
                        val decodedToken = FirebaseAuth.getInstance().verifyIdToken(credential.token)
                        UserIdPrincipal(decodedToken.uid)
                    } catch (e: Exception) { null }
                }
            }
        }
    }

    initFirebase(logger)
    initBackups(logger)

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
        val fileName = "firebase-adminsdk.json"
        if (File(fileName).exists()) {
            val options = FirebaseOptions.builder().setCredentials(GoogleCredentials.fromStream(File(fileName).inputStream())).build()
            if (FirebaseApp.getApps().isEmpty()) FirebaseApp.initializeApp(options)
        }
    } catch (e: Exception) {
        logger.error("FIREBASE: Erro na inicialização: ${e.message}")
    }
}

private fun Application.initBackups(logger: org.slf4j.Logger) {
    val jdbcUrl = System.getenv("DATABASE_URL") ?: "jdbc:sqlite:data/genesys21.db"
    if (!jdbcUrl.startsWith("jdbc:sqlite:")) return
    val dbPath = jdbcUrl.substringAfter("jdbc:sqlite:").substringBefore("?")
    (this as kotlinx.coroutines.CoroutineScope).launch {
        while (true) {
            try { BackupService.performBackup(dbPath) } catch (e: Exception) { }
            kotlinx.coroutines.delay(24.hours)
        }
    }
}
