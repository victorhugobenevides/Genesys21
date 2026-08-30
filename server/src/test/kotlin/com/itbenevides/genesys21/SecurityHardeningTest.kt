package com.itbenevides.genesys21

import com.itbenevides.genesys21.data.database.*
import com.itbenevides.genesys21.data.database.DatabaseFactory.dbQuery
import com.itbenevides.genesys21.data.repository.*
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
import java.io.File
import kotlin.test.*

class SecurityHardeningTest {

    private var testDbPath: String = ""

    @BeforeTest
    fun setup() {
        // Cria um arquivo físico de banco para este teste específico
        val testId = System.nanoTime()
        testDbPath = "build/test-db/security_$testId.db"
        File("build/test-db").mkdirs()

        // Inicializa o banco (FLYWAY criará tabelas)
        DatabaseFactory.reset()
        DatabaseFactory.init("jdbc:sqlite:$testDbPath", rebuild = true)
        println("TEST SETUP: Database file created at $testDbPath")
    }

    @AfterTest
    fun tearDown() {
        DatabaseFactory.reset()
        File(testDbPath).delete()
    }

    @Test
    fun `regular user should not be able to escalate role via saveUserProfile`() = testApplication {
        environment {
            config = MapApplicationConfig(
                "ktor.testing" to "true",
                "ktor.test.db_path" to testDbPath
            )
        }

        val userRepo = SqliteUserRepository()

        // 1. Criar usuário comum
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
        assertEquals(UserRole.CUSTOMER, savedProfile.role, "VULNERABILIDADE: Cargo alterado!")
    }

    @Test
    fun `order total should be recalculated on server to prevent price manipulation`() = testApplication {
        environment {
            config = MapApplicationConfig(
                "ktor.testing" to "true",
                "ktor.test.db_path" to testDbPath
            )
        }

        val orderRepo = SqliteOrderRepository(mockk(relaxed = true))

        // Setup CATALOGO
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

        // Ataque: JSON com R$ 1.00
        val fakeOrder = Order(
            id = "evil-order",
            storeId = "s1",
            items = listOf(
                CartItem(
                    product = Product(id = "real-prod", storeId = "s1", name = "Expensive Product", price = 1.0),
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

        assertEquals(HttpStatusCode.Created, response.status, response.bodyAsText())

        // Verificação final
        val savedOrder = orderRepo.getOrderById("evil-order").getOrThrow()
        assertEquals(1000.0, savedOrder.total, "VULNERABILIDADE: Preço aceito!")
    }
}
