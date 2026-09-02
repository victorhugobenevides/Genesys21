package com.itbenevides.genesys21.routes

import com.itbenevides.genesys21.data.service.StripeService
import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.domain.repository.OrderRepository
import com.itbenevides.genesys21.domain.repository.StoreRepository
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.*

class OrderRoutesTest {

    private val mockOrderRepo = mockk<OrderRepository>()
    private val mockStoreRepo = mockk<StoreRepository>()
    private val mockStripeService = mockk<StripeService>()

    @Test
    fun `post public order with PaymentMethod APP should return stripeClientSecret`() = testApplication {
        val order = Order(
            id = "",
            storeId = "s1",
            items = emptyList(),
            total = 100.0,
            paymentMethod = PaymentMethod.APP
        )

        val store = Store(id = "s1", ownerId = "u1", name = "Store", stripeSecretKey = "sk_test_valid_key_long_enough")

        coEvery { mockOrderRepo.createOrder(any()) } returns Result.success(OrderResponse("o1"))
        coEvery { mockStoreRepo.getStore("s1") } returns Result.success(store)
        every { mockStripeService.createPaymentIntent(any(), any(), any()) } returns "pi_secret_123"

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            install(Authentication) {
                bearer("firebase") {
                    authenticate { UserIdPrincipal("test-user") }
                }
            }
            routing {
                orderRoutes(mockOrderRepo, mockStoreRepo, mockStripeService)
            }
        }

        val response = client.post("/public/orders") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(order))
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val body = Json.decodeFromString<OrderResponse>(response.bodyAsText())
        assertEquals("pi_secret_123", body.stripeClientSecret)
    }
}
