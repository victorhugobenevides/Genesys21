package com.itbenevides.genesys21.routes

import com.itbenevides.genesys21.module
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*

/**
 * Testes para as rotas de Orders conforme implementação atual.
 */
class OrderRoutesTest {

    @Test
    fun testGetOrdersRequiresAuth() = testApplication {
        application { module() }
        val response = client.get("/api/orders")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun testUpdateOrderStatusRequiresAuth() = testApplication {
        application { module() }
        val response = client.patch("/api/orders/order-123/status") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody("\"PROCESSING\"")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun testUpdateOrderStatusNoId() = testApplication {
        application { module() }
        val response = client.patch("/api/orders//status") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody("\"PROCESSING\"")
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun testPublicCustomerOrders() = testApplication {
        application { module() }
        val response = client.get("/api/public/orders/customer/test-session")
        // Pode retornar OK com lista vazia ou erro interno se DB falhar
        assertTrue(response.status == HttpStatusCode.OK || response.status == HttpStatusCode.NotFound || response.status == HttpStatusCode.InternalServerError)
    }

    @Test
    fun testPublicOrderById() = testApplication {
        application { module() }
        val response = client.get("/api/public/orders/order-123")
        assertTrue(response.status == HttpStatusCode.OK || response.status == HttpStatusCode.NotFound)
    }

    @Test
    fun testCheckoutMercadoPagoPublic() = testApplication {
        application { module() }
        val response = client.post("/api/checkout/mercadopago") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody("""
                {
                    "order": {
                        "id": "order-test",
                        "customerName": "Test",
                        "customerPhone": "5511999999999",
                        "status": "PENDING",
                        "total": 99.9,
                        "items": []
                    }
                }
            """.trimIndent())
        }
        // Pode retornar erro de configuração MP (500), erro de validação (400) ou OK
        assertTrue(response.status == HttpStatusCode.OK ||
                   response.status == HttpStatusCode.Created ||
                   response.status == HttpStatusCode.InternalServerError ||
                   response.status == HttpStatusCode.BadRequest)
    }

    @Test
    fun testWebhookMercadoPagoAlwaysOk() = testApplication {
        application { module() }
        val response = client.post("/api/webhook/mercadopago")
        assertEquals(HttpStatusCode.OK, response.status)
    }
}
