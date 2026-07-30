package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.model.CartItem
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.domain.repository.AuthRepository
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class CartRepositoryTest {

    private val json = Json { ignoreUnknownKeys = true }

    class TestCartRepository(
        httpClient: HttpClient,
        authRepository: AuthRepository,
        json: Json
    ) : BaseCartRepository(httpClient, "https://api.example.com", json, authRepository) {
        var localItems = mutableListOf<CartItem>()
        var sessionId: String? = "test-session"

        public override suspend fun saveToLocal(items: List<CartItem>) {
            localItems.clear()
            localItems.addAll(items)
        }

        override suspend fun loadFromLocal(): List<CartItem> = localItems

        override suspend fun saveSessionId(id: String) {
            sessionId = id
        }

        override suspend fun loadSessionId(): String? = sessionId

        // Helper para o teste injetar estado interno diretamente
        fun setInternalItems(items: List<CartItem>) {
            _cartItems.value = items
        }
    }

    class FakeAuthRepository : AuthRepository {
        override val authState = MutableStateFlow<String?>(null)
        var token: String? = null
        var userId: String? = null

        override suspend fun signIn(email: String, password: String) = Result.success(token)
        override suspend fun signIn(idToken: String, accessToken: String?, provider: String) = Result.success(token)
        override suspend fun signUp(email: String, password: String) = Result.success(token)
        override suspend fun getCurrentUserToken(): String? = token
        override suspend fun getCurrentUserId(): String? = userId
        override fun initializeOneTap() {}
        override suspend fun signOut() { authState.value = null }
    }

    @Test
    fun testMergeLogic() = runTest {
        val product1 = Product(id = "p1", storeId = "s1", name = "Product 1", price = 10.0)
        val product2 = Product(id = "p2", storeId = "s1", name = "Product 2", price = 20.0)

        // Visitante tem 2 unidades do p1
        val guestItems = listOf(CartItem(product = product1, quantity = 2))

        // Servidor já tem 3 unidades do p1 e 1 do p2
        val serverItems = listOf(
            CartItem(product = product1, quantity = 3),
            CartItem(product = product2, quantity = 1)
        )

        val mockEngine = MockEngine { request ->
            if (request.url.encodedPath == "/api/cart") {
                if (request.method == HttpMethod.Get) {
                    respond(
                        content = json.encodeToString(serverItems),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                } else if (request.method == HttpMethod.Post) {
                    respond("", HttpStatusCode.OK)
                } else {
                    respond("", HttpStatusCode.NotFound)
                }
            } else {
                respond("", HttpStatusCode.NotFound)
            }
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }

        val authRepository = FakeAuthRepository()
        val repository = TestCartRepository(httpClient, authRepository, json)

        // 1. Simular estado de visitante manual
        repository.setInternalItems(guestItems)

        assertEquals(1, repository.cartItems.value.size)
        assertEquals(2, repository.cartItems.value[0].quantity)

        // 2. Simular Login (atribuir token)
        authRepository.token = "user-token"

        // 3. Executar Merge
        repository.mergeWithServer()

        val finalItems = repository.cartItems.value
        assertEquals(2, finalItems.size)

        val p1 = finalItems.find { it.product?.id == "p1" }
        val p2 = finalItems.find { it.product?.id == "p2" }

        // Esperado: p1(2 do local + 3 do servidor) = 5
        assertEquals(5, p1?.quantity)
        // Esperado: p2(apenas do servidor) = 1
        assertEquals(1, p2?.quantity)
    }
}
