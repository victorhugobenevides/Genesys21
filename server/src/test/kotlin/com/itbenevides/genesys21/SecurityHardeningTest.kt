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
        // Inicializa o banco de dados de teste com uma URI constante.
        // O cache=shared e o nome fixo garantem que o Ktor (Application.module)
        // enxergue exatamente o mesmo banco que este código de setup.
        val testJdbcUrl = "jdbc:sqlite:file:genesys_test_db?mode=memory&cache=shared"
        DatabaseFactory.init(testJdbcUrl, rebuild = true)
        println("TEST SETUP: Database initialized at $testJdbcUrl")
    }

    @Test
    fun `regular user should not be able to escalate role via saveUserProfile`() = testApplication {
        environment {
            config = MapApplicationConfig("ktor.testing" to "true")
        }

        // 1. Criar um usuário comum via Repository (escrita direta no banco compartilhado)
        val initialProfile = UserProfile(
            id = "attacker-id",
            email = "attacker@evil.com",
            name = "Attacker",
            role = UserRole.CUSTOMER
        )
        SqliteUserRepository().saveUserProfile(initialProfile).getOrThrow()

        // 2. Ataque: Tentar se promover para SUPERADMIN via POST na API
        val evilProfile = initialProfile.copy(role = UserRole.SUPERADMIN)

        val response = client.post("/api/users/profile") {
            header(HttpHeaders.Authorization, "Bearer dummy-token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(evilProfile))
        }

        assertEquals(HttpStatusCode.OK, response.status)

        // 3. Verificação: O cargo deve continuar como CUSTOMER
        val savedProfile = SqliteUserRepository().getUserProfile("attacker-id").getOrThrow()
        println("TEST VERIFY: Role após ataque: ${savedProfile.role}")

        assertEquals(
            UserRole.CUSTOMER,
            savedProfile.role,
            "VULNERABILIDADE: Usuário conseguiu se promover para ${savedProfile.role} via API!"
        )
    }

    @Test
    fun `order total should be recalculated on server to prevent price manipulation`() = testApplication {
        environment {
            config = MapApplicationConfig("ktor.testing" to "true")
        }

        val testStoreId = "s1"
        val testProdId = "real-prod"

        // Setup: Inserir catálogo oficial no banco compartilhado
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

        // Ataque: Enviar pedido com preço manipulado de R$ 1.00
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

        assertEquals(HttpStatusCode.Created, response.status, "Order body: ${response.bodyAsText()}")

        // Verificação: O servidor deve ter forçado o preço de R$ 1000.00 do banco
        val savedOrder = SqliteOrderRepository(mockk(relaxed = true)).getOrderById("evil-order").getOrThrow()
        println("TEST VERIFY: Total salvo no banco: R$ ${savedOrder.total}")

        assertEquals(
            1000.0,
            savedOrder.total,
            "VULNERABILIDADE: Servidor aceitou preço de R$ ${savedOrder.total} em vez de R$ 1000.0!"
        )
    }
}
