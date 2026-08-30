package com.itbenevides.genesys21

import com.itbenevides.genesys21.data.database.DatabaseFactory
import com.itbenevides.genesys21.data.database.DatabaseFactory.dbQuery
import com.itbenevides.genesys21.data.database.ProductsTable
import com.itbenevides.genesys21.data.database.StoresTable
import com.itbenevides.genesys21.data.repository.SqliteUserRepository
import com.itbenevides.genesys21.data.repository.SqliteOrderRepository
import com.itbenevides.genesys21.data.service.StripeService
import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.routes.userRoutes
import com.itbenevides.genesys21.routes.orderRoutes
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.mockk
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.insert
import java.io.File
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds

class SecurityHardeningTest {

    private var currentDbFile: String? = null

    @BeforeTest
    fun setup() {
        // Forçar isolamento total usando um arquivo físico único por teste
        val testId = System.nanoTime()
        val dbFile = "build/security_test_$testId.db"
        currentDbFile = dbFile

        // Inicializa o banco (Flyway rodará e criará as tabelas)
        DatabaseFactory.init("jdbc:sqlite:$dbFile", rebuild = true)
    }

    @AfterTest
    fun tearDown() {
        // Limpa o arquivo após o teste
        currentDbFile?.let { File(it).delete() }
    }

    @Test
    fun `regular user should not be able to escalate role via saveUserProfile`() = testApplication {
        val userRepo = SqliteUserRepository()

        application {
            install(ContentNegotiation) { json() }
            install(RateLimit) {
                register(RateLimitName("global")) { rateLimiter(limit = 100, refillPeriod = 60.seconds) }
                register(RateLimitName("sensitive")) { rateLimiter(limit = 100, refillPeriod = 60.seconds) }
            }
            install(Authentication) {
                bearer("firebase") {
                    authenticate { UserIdPrincipal("attacker-id") }
                }
            }
            routing { userRoutes(userRepo) }
        }

        // 1. Criar um usuário comum
        val initialProfile = UserProfile(
            id = "attacker-id",
            email = "attacker@evil.com",
            name = "Attacker",
            role = UserRole.CUSTOMER
        )
        userRepo.saveUserProfile(initialProfile)

        // 2. Tentar se promover para SUPERADMIN via POST
        val evilProfile = initialProfile.copy(role = UserRole.SUPERADMIN)

        val response = client.post("/api/users/profile") {
            header(HttpHeaders.Authorization, "Bearer dummy-token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(evilProfile))
        }

        assertEquals(HttpStatusCode.OK, response.status, "Update profile should return 200 OK")

        // 3. Verificar se o cargo CONTINUA como CUSTOMER no banco
        val savedProfile = userRepo.getUserProfile("attacker-id").getOrThrow()
        assertEquals(
            UserRole.CUSTOMER,
            savedProfile.role,
            "Security Vulnerability: User was able to escalate role to ${savedProfile.role} via Mass Assignment!"
        )
    }

    @Test
    fun `order total should be recalculated on server to prevent price manipulation`() = testApplication {
        val mockBookingRepo = mockk<com.itbenevides.genesys21.domain.repository.BookingRepository>(relaxed = true)
        val orderRepo = SqliteOrderRepository(mockBookingRepo)
        val mockStoreRepo = mockk<com.itbenevides.genesys21.domain.repository.StoreRepository>(relaxed = true)
        val mockStripeService = mockk<StripeService>(relaxed = true)

        // Setup de dados reais no banco de teste
        dbQuery {
            StoresTable.insert {
                it[id] = "s1"
                it[ownerId] = "u1"
                it[name] = "Test Store"
            }
            ProductsTable.insert {
                it[id] = "real-prod"
                it[storeId] = "s1"
                it[name] = "Expensive Product"
                it[price] = 1000.0
                it[stock] = 10
            }
        }

        application {
            install(ContentNegotiation) { json() }
            install(RateLimit) {
                register(RateLimitName("global")) { rateLimiter(limit = 100, refillPeriod = 60.seconds) }
            }
            install(Authentication) {
                bearer("firebase") { authenticate { UserIdPrincipal("test-user") } }
            }
            routing { orderRoutes(orderRepo, mockStoreRepo, mockStripeService) }
        }

        // Atacante envia um pedido com preço forjado de R$ 1.00
        val fakeOrder = Order(
            id = "evil-order",
            storeId = "s1",
            items = listOf(
                CartItem(
                    product = Product(id = "real-prod", storeId = "s1", name = "Expensive Product", price = 1000.0),
                    quantity = 1,
                    customPrice = 1.0 // Tentativa de manipulação no DTO
                ).copy(customPrice = 1.0) // Garante que o campo price do CartItem também seja 1.0
            ),
            total = 1.0, // Tentativa de manipulação no total do pedido
            paymentMethod = PaymentMethod.LOCAL
        )

        // Precisamos garantir que o CartItem.price seja 1.0 para o teste ser efetivo
        val manipulatedOrder = fakeOrder.copy(
            items = fakeOrder.items.map { it.copy(customPrice = 1.0) }
        )

        val response = client.post("/api/public/orders") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(manipulatedOrder))
        }

        assertEquals(HttpStatusCode.Created, response.status, "Order should be created even with manipulated price. Response: ${response.bodyAsText()}")

        // VERIFICAÇÃO CRÍTICA: O pedido salvo deve ter o valor de 1000.0 (do banco) e não 1.0 (do atacante)
        val savedOrder = orderRepo.getOrderById("evil-order").getOrThrow()

        assertEquals(
            1000.0,
            savedOrder.total,
            "Security Vulnerability: Price manipulation accepted! Server saved total as ${savedOrder.total} instead of 1000.0"
        )

        // Verifica se cada item individual também teve o preço corrigido
        assertEquals(
            1000.0,
            savedOrder.items.first().price,
            "Security Vulnerability: Individual item price manipulation accepted!"
        )
    }
}
