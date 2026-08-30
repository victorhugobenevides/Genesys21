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
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds

class SecurityHardeningTest {

    @BeforeTest
    fun setup() {
        // Garantir banco limpo e isolado por teste
        val dbName = "securitytest_${System.nanoTime()}"
        DatabaseFactory.init("jdbc:sqlite:file:$dbName?mode=memory&cache=shared", rebuild = true)
    }

    @Test
    fun `regular user should not be able to escalate role via saveUserProfile`() = testApplication {
        val userRepo = SqliteUserRepository()

        application {
            install(ContentNegotiation) { json() }
            install(RateLimit) {
                register(RateLimitName("global")) {
                    rateLimiter(limit = 100, refillPeriod = 60.seconds)
                }
                register(RateLimitName("sensitive")) {
                    rateLimiter(limit = 100, refillPeriod = 60.seconds)
                }
            }
            install(Authentication) {
                bearer("firebase") {
                    authenticate { UserIdPrincipal("attacker-id") }
                }
            }
            routing { userRoutes(userRepo) }
        }

        // 1. Create a regular customer
        val initialProfile = UserProfile(
            id = "attacker-id",
            email = "attacker@evil.com",
            name = "Attacker",
            role = UserRole.CUSTOMER
        )
        userRepo.saveUserProfile(initialProfile)

        // 2. Attempt to promote self to SUPERADMIN
        val evilProfile = initialProfile.copy(role = UserRole.SUPERADMIN)

        val response = client.post("/api/users/profile") {
            header(HttpHeaders.Authorization, "Bearer dummy-token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(evilProfile))
        }

        assertEquals(HttpStatusCode.OK, response.status)

        // 3. Verify that the role remains CUSTOMER in the database
        val savedProfile = userRepo.getUserProfile("attacker-id").getOrThrow()
        println("DEBUG: User role after attack: ${savedProfile.role}")
        assertEquals(UserRole.CUSTOMER, savedProfile.role, "Security Vulnerability: Role escalation via Mass Assignment detected! Actual role: ${savedProfile.role}")
    }

    @Test
    fun `order total should be recalculated on server to prevent price manipulation`() = testApplication {
        val mockBookingRepo = mockk<com.itbenevides.genesys21.domain.repository.BookingRepository>(relaxed = true)
        val orderRepo = SqliteOrderRepository(mockBookingRepo)
        val mockStoreRepo = mockk<com.itbenevides.genesys21.domain.repository.StoreRepository>(relaxed = true)
        val mockStripeService = mockk<StripeService>(relaxed = true)

        // Setup a product and store in the DB
        dbQuery {
            StoresTable.insert {
                it[id] = "s1"
                it[ownerId] = "u1"
                it[name] = "Test Store"
                it[paymentGateway] = "STRIPE"
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
                register(RateLimitName("global")) {
                    rateLimiter(limit = 100, refillPeriod = 60.seconds)
                }
                register(RateLimitName("sensitive")) {
                    rateLimiter(limit = 100, refillPeriod = 60.seconds)
                }
            }
            install(Authentication) {
                bearer("firebase") {
                    authenticate { UserIdPrincipal("test-user") }
                }
            }
            routing { orderRoutes(orderRepo, mockStoreRepo, mockStripeService) }
        }

        // Attacker sends an order with a fake cheap price
        val fakeOrder = Order(
            id = "evil-order",
            storeId = "s1",
            items = listOf(
                CartItem(
                    product = Product(id = "real-prod", storeId = "s1", name = "Expensive Product", price = 1000.0),
                    quantity = 1
                )
            ),
            total = 1.0, // Manipulation attempt: $1 instead of $1000
            paymentMethod = PaymentMethod.LOCAL
        )

        val response = client.post("/api/public/orders") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(fakeOrder))
        }

        assertEquals(HttpStatusCode.Created, response.status, "Order creation should succeed but with corrected price. Body: ${response.bodyAsText()}")

        // Verify that the saved order has the CORRECT recalculated price
        val savedOrder = orderRepo.getOrderById("evil-order").getOrThrow()
        assertEquals(1000.0, savedOrder.total, "Security Vulnerability: Price manipulation accepted! Expected 1000.0 but found ${savedOrder.total}")
    }
}
