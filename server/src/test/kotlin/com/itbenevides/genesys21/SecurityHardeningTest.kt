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
import org.jetbrains.exposed.sql.selectAll
import java.io.File
import kotlin.test.*

class SecurityHardeningTest {

    private val testDbPath: String = "/tmp/genesys_security_test.db"

    @BeforeTest
    fun setup() {
        // Inicializa o banco físico para o teste
        DatabaseFactory.reset()
        DatabaseFactory.init("jdbc:sqlite:$testDbPath", rebuild = true)
        println("TEST SETUP: Database initialized at $testDbPath")
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

        application {
            module() // CARREGA TUDO: Rotas, Plugins, Autenticação, etc.
        }

        val userRepo = SqliteUserRepository()

        // 1. Criar um usuário comum
        val initialProfile = UserProfile(
            id = "attacker-id",
            email = "attacker@evil.com",
            name = "Attacker",
            role = UserRole.CUSTOMER
        )
        userRepo.saveUserProfile(initialProfile).getOrThrow()

        // 2. Ataque: Tentar se promover para SUPERADMIN via POST na API
        val evilProfile = initialProfile.copy(role = UserRole.SUPERADMIN)

        val response = client.post("/api/users/profile") {
            header(HttpHeaders.Authorization, "Bearer dummy-token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(evilProfile))
        }

        assertEquals(HttpStatusCode.OK, response.status, "Update profile should return 200 OK")

        // 3. Verificação: O cargo deve continuar como CUSTOMER
        val savedProfile = userRepo.getUserProfile("attacker-id").getOrThrow()
        println("TEST VERIFY: Role após ataque: ${savedProfile.role}")

        assertEquals(
            UserRole.CUSTOMER,
            savedProfile.role,
            "VULNERABILIDADE: Usuário conseguiu se promover para ${savedProfile.role}!"
        )
    }

    @Test
    fun `order total should be recalculated on server to prevent price manipulation`() = testApplication {
        environment {
            config = MapApplicationConfig(
                "ktor.testing" to "true",
                "ktor.test.db_path" to testDbPath
            )
        }

        application {
            module() // CARREGA TUDO
        }

        val orderRepo = SqliteOrderRepository(mockk(relaxed = true))

        // Setup: Inserir catálogo oficial
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

        // Ataque: Enviar pedido com preço manipulado de R$ 1.00
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

        assertEquals(HttpStatusCode.Created, response.status, "Response: ${response.bodyAsText()}")

        // Verificação: O servidor deve ter forçado o preço de R$ 1000.00 do banco
        val savedOrder = orderRepo.getOrderById("evil-order").getOrThrow()
        println("TEST VERIFY: Total salvo no banco: R$ ${savedOrder.total}")

        assertEquals(
            1000.0,
            savedOrder.total,
            "VULNERABILIDADE: Servidor aceitou preço de R$ ${savedOrder.total}!"
        )
    }
}
