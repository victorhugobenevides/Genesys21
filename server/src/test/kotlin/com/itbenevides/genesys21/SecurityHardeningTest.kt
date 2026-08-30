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
import org.jetbrains.exposed.sql.selectAll
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds

class SecurityHardeningTest {

    @BeforeTest
    fun setup() {
        // Forçar isolamento total usando um banco em memória PRIVATE por teste.
        // Como os testes de rota usam o servidor embutido, precisamos garantir que
        // a conexão aberta no setup seja a MESMA que o servidor usará.
        val testId = System.nanoTime()
        DatabaseFactory.init("jdbc:sqlite:file:test_$testId?mode=memory&cache=shared", rebuild = true)
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

        // 1. Criar um usuário comum no banco
        val initialProfile = UserProfile(
            id = "attacker-id",
            email = "attacker@evil.com",
            name = "Attacker",
            role = UserRole.CUSTOMER
        )
        userRepo.saveUserProfile(initialProfile).getOrThrow()

        // 2. Tentar se promover para SUPERADMIN via POST (Mass Assignment Attack)
        val evilProfile = initialProfile.copy(role = UserRole.SUPERADMIN)

        val response = client.post("/api/users/profile") {
            header(HttpHeaders.Authorization, "Bearer dummy-token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(evilProfile))
        }

        assertEquals(HttpStatusCode.OK, response.status, "Update profile should succeed but ignore sensitive fields")

        // 3. Verificar se o cargo CONTINUA como CUSTOMER no banco
        val savedProfile = userRepo.getUserProfile("attacker-id").getOrThrow()
        assertEquals(
            UserRole.CUSTOMER,
            savedProfile.role,
            "VULNERABILIDADE: O usuário conseguiu mudar seu cargo para ${savedProfile.role} via API!"
        )
    }

    @Test
    fun `order total should be recalculated on server to prevent price manipulation`() = testApplication {
        val mockBookingRepo = mockk<com.itbenevides.genesys21.domain.repository.BookingRepository>(relaxed = true)
        val orderRepo = SqliteOrderRepository(mockBookingRepo)
        val mockStoreRepo = mockk<com.itbenevides.genesys21.domain.repository.StoreRepository>(relaxed = true)
        val mockStripeService = mockk<StripeService>(relaxed = true)

        val testStoreId = "s1"
        val testProdId = "real-prod"

        // Setup de dados reais diretamente no banco compartilhado
        dbQuery {
            StoresTable.insert {
                it[id] = testStoreId
                it[ownerId] = "u1"
                it[name] = "Test Store"
                it[paymentGateway] = "STRIPE"
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

        // Atacante envia um pedido com preço forjado de R$ 1.00
        val fakeOrder = Order(
            id = "evil-order",
            storeId = testStoreId,
            items = listOf(
                CartItem(
                    product = Product(id = testProdId, storeId = testStoreId, name = "Expensive Product", price = 1000.0),
                    quantity = 1
                )
            ),
            total = 1.0, // Tentativa de pagar R$ 1.00
            paymentMethod = PaymentMethod.LOCAL
        )

        // Manipulamos o DTO para simular a interceptação do front-end
        val manipulatedOrder = Json.decodeFromString<Order>(
            Json.encodeToString(fakeOrder).replace("1000.0", "1.0")
        )

        val response = client.post("/api/public/orders") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(manipulatedOrder))
        }

        assertEquals(HttpStatusCode.Created, response.status, "Order should be created. Body: ${response.bodyAsText()}")

        // VERIFICAÇÃO: O servidor deve ter ignorado o preço do DTO e usado o do banco
        val savedOrder = orderRepo.getOrderById("evil-order").getOrThrow()

        assertEquals(
            1000.0,
            savedOrder.total,
            "VULNERABILIDADE: O servidor aceitou o preço forjado de ${savedOrder.total}!"
        )

        assertEquals(
            1000.0,
            savedOrder.items.first().price,
            "VULNERABILIDADE: O preço individual do item não foi corrigido!"
        )
    }
}
