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

    @BeforeTest
    fun setup() {
        // Banco de dados único por classe de teste para evitar colisões
        val testId = "security_final"
        DatabaseFactory.init("jdbc:sqlite:file:$testId?mode=memory&cache=shared", rebuild = true)
    }

    @Test
    fun `regular user should not be able to escalate role via saveUserProfile`() = testApplication {
        val userRepo = SqliteUserRepository()

        application {
            install(ContentNegotiation) { json() }
            install(RateLimit) {
                register(RateLimitName("global")) { rateLimiter(limit = 1000, refillPeriod = 60.seconds) }
                register(RateLimitName("sensitive")) { rateLimiter(limit = 1000, refillPeriod = 60.seconds) }
            }
            install(Authentication) {
                bearer("firebase") {
                    authenticate { UserIdPrincipal("attacker-id") }
                }
            }
            routing { userRoutes(userRepo) }
        }

        // 1. Criar usuário no banco
        val initialProfile = UserProfile(
            id = "attacker-id",
            email = "attacker@evil.com",
            name = "Attacker",
            role = UserRole.CUSTOMER
        )
        userRepo.saveUserProfile(initialProfile).getOrThrow()

        // 2. Ataque
        val evilProfile = initialProfile.copy(role = UserRole.SUPERADMIN)

        val response = client.post("/api/users/profile") {
            header(HttpHeaders.Authorization, "Bearer dummy-token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(evilProfile))
        }

        assertEquals(HttpStatusCode.OK, response.status)

        // 3. Verificação
        val savedProfile = userRepo.getUserProfile("attacker-id").getOrThrow()
        assertEquals(UserRole.CUSTOMER, savedProfile.role, "VULNERABILIDADE: O cargo foi alterado para ${savedProfile.role}!")
    }

    @Test
    fun `order total should be recalculated on server to prevent price manipulation`() = testApplication {
        val mockBookingRepo = mockk<com.itbenevides.genesys21.domain.repository.BookingRepository>(relaxed = true)
        val orderRepo = SqliteOrderRepository(mockBookingRepo)
        val mockStoreRepo = mockk<com.itbenevides.genesys21.domain.repository.StoreRepository>(relaxed = true)
        val mockStripeService = mockk<StripeService>(relaxed = true)

        val testStoreId = "s1"
        val testProdId = "real-prod"

        // Setup CATALOGO
        dbQuery {
            StoresTable.insert {
                it[id] = testStoreId
                it[ownerId] = "u1"
                it[name] = "Test Store"
            }
            ProductsTable.insert {
                it[id] = testProdId
                it[storeId] = testStoreId
                it[name] = "Expensive Product"
                it[price] = 1000.0
                it[stock] = 10
            }
        }

        application {
            install(ContentNegotiation) { json() }
            install(RateLimit) {
                register(RateLimitName("global")) { rateLimiter(limit = 1000, refillPeriod = 60.seconds) }
            }
            install(Authentication) {
                bearer("firebase") { authenticate { UserIdPrincipal("test-user") } }
            }
            routing { orderRoutes(orderRepo, mockStoreRepo, mockStripeService) }
        }

        // Atacante envia 1.0 no JSON
        val fakeOrder = Order(
            id = "evil-order",
            storeId = testStoreId,
            items = listOf(
                CartItem(
                    product = Product(id = testProdId, storeId = testStoreId, name = "Expensive Product", price = 1.0),
                    quantity = 1
                )
            ),
            total = 1.0,
            paymentMethod = PaymentMethod.LOCAL
        )

        val response = client.post("/api/public/orders") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(fakeOrder))
        }

        assertEquals(HttpStatusCode.Created, response.status)

        // Verificação final
        val savedOrder = orderRepo.getOrderById("evil-order").getOrThrow()
        assertEquals(1000.0, savedOrder.total, "VULNERABILIDADE: O servidor aceitou o preço forjado de ${savedOrder.total}!")
    }
}
