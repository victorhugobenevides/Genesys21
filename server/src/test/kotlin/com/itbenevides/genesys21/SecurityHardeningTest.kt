package com.itbenevides.genesys21

import com.itbenevides.genesys21.data.database.DatabaseFactory
import com.itbenevides.genesys21.data.database.DatabaseFactory.dbQuery
import com.itbenevides.genesys21.data.database.ProductsTable
import com.itbenevides.genesys21.data.database.StoresTable
import com.itbenevides.genesys21.data.repository.SqliteUserRepository
import com.itbenevides.genesys21.data.repository.SqliteOrderRepository
import com.itbenevides.genesys21.domain.model.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import io.mockk.mockk
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.insert
import kotlin.test.*

class SecurityHardeningTest {

    @BeforeTest
    fun setup() {
        // Reset the singleton before each test to ensure isolation
        DatabaseFactory.reset()
    }

    @Test
    fun `regular user should not be able to escalate role via saveUserProfile`() = testApplication {
        val testDbId = System.nanoTime().toString()
        environment {
            config = MapApplicationConfig(
                "ktor.testing" to "true",
                "ktor.test.db_id" to testDbId
            )
        }

        // Initialize DB for setup
        val testJdbcUrl = "jdbc:sqlite:file:db_$testDbId?mode=memory&cache=shared"
        DatabaseFactory.init(testJdbcUrl, rebuild = true)

        val userRepo = SqliteUserRepository()

        // 1. Criar um usuário comum no banco
        val initialProfile = UserProfile(
            id = "attacker-id",
            email = "attacker@evil.com",
            name = "Attacker",
            role = UserRole.CUSTOMER
        )
        userRepo.saveUserProfile(initialProfile).getOrThrow()

        // 2. Ataque via API
        val evilProfile = initialProfile.copy(role = UserRole.SUPERADMIN)

        val response = client.post("/api/users/profile") {
            header(HttpHeaders.Authorization, "Bearer dummy-token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(evilProfile))
        }

        assertEquals(HttpStatusCode.OK, response.status)

        // 3. Verificação
        val savedProfile = userRepo.getUserProfile("attacker-id").getOrThrow()
        assertEquals(UserRole.CUSTOMER, savedProfile.role, "VULNERABILIDADE: O cargo mudou!")
    }

    @Test
    fun `order total should be recalculated on server to prevent price manipulation`() = testApplication {
        val testDbId = System.nanoTime().toString()
        environment {
            config = MapApplicationConfig(
                "ktor.testing" to "true",
                "ktor.test.db_id" to testDbId
            )
        }

        // Initialize DB for setup
        val testJdbcUrl = "jdbc:sqlite:file:db_$testDbId?mode=memory&cache=shared"
        DatabaseFactory.init(testJdbcUrl, rebuild = true)

        val testStoreId = "s1"
        val testProdId = "real-prod"

        // Setup CATALOGO
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

        // Verificação: O servidor deve ter ignorado o 1.0 e salvo 1000.0
        val savedOrder = SqliteOrderRepository(mockk(relaxed = true)).getOrderById("evil-order").getOrThrow()
        assertEquals(1000.0, savedOrder.total, "VULNERABILIDADE: Preço aceito!")
    }
}
