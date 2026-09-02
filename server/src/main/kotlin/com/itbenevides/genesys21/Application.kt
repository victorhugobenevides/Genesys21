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
import com.itbenevides.genesys21.domain.model.PageComponent
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
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

const val SERVER_PORT = 8080

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    val logger = LoggerFactory.getLogger("Application")
    logger.info("SERVIDOR: Genesys21 iniciando...")

    val isTesting = environment.config.propertyOrNull("ktor.testing")?.getString() == "true"
    val shouldRebuild = environment.config.propertyOrNull("ktor.db.rebuild")?.getString() == "true" || System.getenv("DB_REBUILD") == "true"

    if (shouldRebuild) {
        logger.warn("☢️ NUCLEAR RESET: Deletando banco de dados para reconstrução total!")
        val dbFile = File("data/genesys21.db")
        if (dbFile.exists()) {
            dbFile.delete()
            File("data/genesys21.db-shm").delete()
            File("data/genesys21.db-wal").delete()
            logger.info("✓ Arquivo data/genesys21.db deletado com sucesso.")
        } else {
            logger.info("⚠ Arquivo data/genesys21.db não encontrado para deletar.")
        }
    }

    // DATABASE INITIALIZATION
    if (isTesting) {
        val testDbPath = environment.config.propertyOrNull("ktor.test.db_path")?.getString()
        if (testDbPath != null) {
            println("SERVER: Inicializando banco de teste em $testDbPath")
            DatabaseFactory.init("jdbc:sqlite:$testDbPath", rebuild = false)
        } else if (!DatabaseFactory.isInitialized()) {
            DatabaseFactory.init("jdbc:sqlite:file:testdb?mode=memory&cache=shared", rebuild = true)
        }
    } else {
        DatabaseFactory.init(rebuild = shouldRebuild)
    }

    val pageRepository = SqlitePageRepository()
    val cartRepository = SqliteCartRepository()
    val bookingRepository = SqliteBookingRepository(GoogleCalendarService())
    val orderRepository = SqliteOrderRepository(bookingRepository)

    val ownerEmail = System.getenv("OWNER_EMAIL") ?: "victorkoto@gmail.com"
    logger.info("DOGMA: Owner Email configurado: $ownerEmail")
    println("[SECURITY] DOGMA: Owner Email configurado para injeção de cargo: $ownerEmail")
    val userRepository = SqliteUserRepository(ownerEmail)

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

    // DIAGNÓSTICO AMBIENTE
    val stripeKey = System.getenv("STRIPE_SECRET_KEY")
    if (stripeKey.isNullOrBlank()) {
        logger.error("🚨 SERVIDOR: STRIPE_SECRET_KEY NÃO ENCONTRADA NO AMBIENTE!")
    } else {
        logger.info("✅ SERVIDOR: STRIPE_SECRET_KEY carregada com sucesso (${stripeKey.take(7)}...).")
    }

    install(StatusPages) {
        exception<io.ktor.serialization.JsonConvertException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, "Erro no formato dos dados.")
        }
        exception<Throwable> { call, cause ->
            logger.error("ERRO CRÍTICO: ${cause.message}")
            call.respond(HttpStatusCode.InternalServerError, cause.message ?: "Erro desconhecido")
        }
    }

    // RATE LIMITING: Obrigatório instalar para que as rotas possam referenciar os nomes.
    // Em modo de teste, elevamos o limite para evitar falhas em testes de integração e carga.
    install(RateLimit) {
        register(RateLimitName("global")) {
            if (isTesting) {
                // Em testes, desativamos o limite real para evitar 429
                rateLimiter(limit = Int.MAX_VALUE, refillPeriod = 1.seconds)
            } else {
                rateLimiter(limit = 100, refillPeriod = 60.seconds)
            }
        }
        register(RateLimitName("sensitive")) {
            if (isTesting) {
                rateLimiter(limit = Int.MAX_VALUE, refillPeriod = 1.seconds)
            } else {
                rateLimiter(limit = 5, refillPeriod = 60.seconds)
            }
        }
    }

    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = true; coerceInputValues = true })
    }

    install(DefaultHeaders) {
        header("X-Frame-Options", "DENY")
        header("X-Content-Type-Options", "nosniff")
        header("Strict-Transport-Security", "max-age=31536000")
        header("Content-Security-Policy", "default-src 'self';")
    }

    install(CORS) {
        // SEGURANÇA: Lista restrita de hosts para evitar duplicidade de Access-Control-Allow-Origin (ex: *, *)
        val allowedHosts = listOf(
            "victorbenevides.dev", "www.victorbenevides.dev", "staging.victorbenevides.dev",
            "radarani.site", "www.radarani.site", "localhost", "0.0.0.0"
        )

        allowedHosts.forEach { host ->
            allowHost(host, schemes = listOf("http", "https"))
        }

        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.CacheControl)
        allowHeader("X-Cart-Session-Id")
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
        allowCredentials = true
        maxAgeInSeconds = 3600
    }

    install(Authentication) {
        bearer("firebase") {
            authenticate { credential ->
                val principal = if (isTesting && (credential.token == "dummy-token" || credential.token == "valid-token")) {
                    UserIdPrincipal(if (credential.token == "dummy-token") "attacker-id" else "test-user")
                } else {
                    try {
                        val decodedToken = FirebaseAuth.getInstance().verifyIdToken(credential.token)
                        UserIdPrincipal(decodedToken.uid)
                    } catch (e: Exception) { null }
                }

                // DETAILED LOGGING FOR LOGIN/AUTHENTICATION
                if (principal != null) {
                    launch {
                        userRepository.getUserProfile(principal.name).onSuccess { profile ->
                            logger.info("USER [${profile.id}] with email [${profile.email}] authenticated. Role assigned: [${profile.role}]")
                        }
                    }
                }

                principal
            }
        }
    }

    initFirebase(logger)
    initBackups(logger)

    routing {
        get("/") { call.respondText("API Online") }

        route("/api") {
            // Rotas individuais aplicam seus próprios rate limits
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
    } catch (e: Exception) {
        logger.error("FIREBASE ERROR: ${e.message}")
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
