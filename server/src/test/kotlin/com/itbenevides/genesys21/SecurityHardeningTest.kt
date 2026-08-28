package com.itbenevides.genesys21

import com.itbenevides.genesys21.data.database.DatabaseFactory
import com.itbenevides.genesys21.data.repository.SqliteUserRepository
import com.itbenevides.genesys21.data.repository.SqliteOrderRepository
import com.itbenevides.genesys21.data.service.StripeService
import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.routes.adminRoutes
import com.itbenevides.genesys21.routes.userRoutes
import com.itbenevides.genesys21.routes.orderRoutes
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.mockk
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.*

class SecurityHardeningTest {

    @BeforeTest
    fun setup() {
        DatabaseFactory.init("jdbc:sqlite:file:securitytest?mode=memory&cache=shared", rebuild = true)
    }

    @Test
    fun `regular user should not be able to escalate role via saveUserProfile`() = testApplication {
        val userRepo = SqliteUserRepository()

        application {
            install(ContentNegotiation) { json() }
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
        assertEquals(UserRole.CUSTOMER, savedProfile.role, "Security Vulnerability: Role escalation via Mass Assignment detected!")
    }

    @Test
    fun `order total should be recalculated on server to prevent price manipulation`() = testApplication {
        val userRepo = SqliteUserRepository()
        val mockBookingRepo = mockk<com.itbenevides.genesys21.domain.repository.BookingRepository>(relaxed = true)
        val orderRepo = SqliteOrderRepository(mockBookingRepo)
        val mockStoreRepo = mockk<com.itbenevides.genesys21.domain.repository.StoreRepository>(relaxed = true)
        val mockStripeService = mockk<StripeService>(relaxed = true)

        // Setup a product in the DB with a real price
        transaction {
            com.itbenevides.genesys21.data.database.ProductsTable.insert {
                it[id] = "real-prod"
                it[storeId] = "s1"
                it[name] = "Expensive Product"
                it[price] = 1000.0
                it[stock] = 10
            }
        }

        application {
            install(ContentNegotiation) { json() }
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

        client.post("/api/public/orders") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(fakeOrder))
        }

        // Verify that the saved order has the CORRECT recalculated price
        val savedOrder = orderRepo.getOrderById("evil-order").getOrThrow()
        assertEquals(1000.0, savedOrder.total, "Security Vulnerability: Price manipulation accepted!")
    }
}
