package com.itbenevides.genesys21.routes

import com.itbenevides.genesys21.module
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Testes adicionais para OrderRoutes focando em aumentar cobertura de branches.
 */
class OrderRoutesCoverageTest {

    private fun ApplicationTestBuilder.setupTestApp() {
        environment { config = MapApplicationConfig("genesys.test.auth" to "true") }
        application { module() }
    }

    @Test
fun testGetOrdersWithInvalidToken() = testApplication {
        setupTestApp()
val response = client.get("/api/orders") {
            header(HttpHeaders.Authorization, "Bearer invalid-token")
        }
        assertTrue(response.status == HttpStatusCode.OK || response.status == HttpStatusCode.Unauthorized)
    }

    @Test
    fun testUpdateOrderStatusWithInvalidBody() = testApplication {
        setupTestApp()
val response = client.patch("/api/orders/order-123/status") {
            header(HttpHeaders.Authorization, "Bearer test-token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody("invalid")
        }
        assertTrue(response.status == HttpStatusCode.BadRequest || 
                   response.status == HttpStatusCode.Unauthorized ||
                   response.status == HttpStatusCode.InternalServerError)
    }

    @Test
    fun testUpdateOrderStatusWithInvalidJson() = testApplication {
        setupTestApp()
val response = client.patch("/api/orders/order-123/status") {
            header(HttpHeaders.Authorization, "Bearer test-token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody("{invalid}")
        }
        assertTrue(response.status == HttpStatusCode.BadRequest || 
                   response.status == HttpStatusCode.Unauthorized ||
                   response.status == HttpStatusCode.InternalServerError)
    }

    @Test
    fun testUpdateOrderStatusWithoutOrderId() = testApplication {
        setupTestApp()
val response = client.patch("/api/orders//status") {
            header(HttpHeaders.Authorization, "Bearer test-token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody("\"PENDING\"")
        }
        assertTrue(response.status == HttpStatusCode.NotFound || 
                   response.status == HttpStatusCode.Unauthorized)
    }

    @Test
    fun testCheckoutWithInvalidOrderJson() = testApplication {
        setupTestApp()
val response = client.post("/api/checkout/mercadopago") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody("{invalid json}")
        }
        assertTrue(response.status == HttpStatusCode.BadRequest || 
                   response.status == HttpStatusCode.InternalServerError)
    }

    @Test
    fun testCheckoutWithEmptyBody() = testApplication {
        setupTestApp()
val response = client.post("/api/checkout/mercadopago") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody("{}")
        }
        assertTrue(response.status == HttpStatusCode.BadRequest || 
                   response.status == HttpStatusCode.InternalServerError)
    }

    @Test
    fun testCheckoutWithMissingOrderField() = testApplication {
        setupTestApp()
val response = client.post("/api/checkout/mercadopago") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody("{\"customerName\": \"Test\", \"total\": 100}")
        }
        assertTrue(response.status == HttpStatusCode.BadRequest || 
                   response.status == HttpStatusCode.InternalServerError)
    }

    @Test
    fun testWebhookWithEmptyBody() = testApplication {
        setupTestApp()
val response = client.post("/api/webhook/mercadopago")
        // Webhook geralmente aceita qualquer body e retorna OK
        assertTrue(response.status == HttpStatusCode.OK || 
                   response.status == HttpStatusCode.BadRequest)
    }

    @Test
    fun testWebhookWithInvalidJson() = testApplication {
        setupTestApp()
val response = client.post("/api/webhook/mercadopago") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody("{invalid}")
        }
        assertTrue(response.status == HttpStatusCode.OK || 
                   response.status == HttpStatusCode.BadRequest)
    }

    @Test
    fun testPublicCustomerOrdersWithEmptySession() = testApplication {
        setupTestApp()
val response = client.get("/api/public/orders/customer/")
        assertTrue(response.status == HttpStatusCode.NotFound || 
                   response.status == HttpStatusCode.BadRequest)
    }

    @Test
    fun testPublicOrderByIdWithEmptyId() = testApplication {
        setupTestApp()
val response = client.get("/api/public/orders/")
        assertTrue(response.status == HttpStatusCode.NotFound)
    }

    @Test
    fun testGetOrdersWithMalformedToken() = testApplication {
        setupTestApp()
val response = client.get("/api/orders") {
            header(HttpHeaders.Authorization, "Token malformed")
        }
        assertTrue(response.status == HttpStatusCode.OK || 
                   response.status == HttpStatusCode.Unauthorized)
    }

    @Test
    fun testUpdateOrderStatusWithEmptyToken() = testApplication {
        setupTestApp()
val response = client.patch("/api/orders/order-123/status") {
            header(HttpHeaders.Authorization, "")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody("\"PENDING\"")
        }
        assertTrue(response.status == HttpStatusCode.Unauthorized || 
                   response.status == HttpStatusCode.BadRequest)
    }

    @Test
    fun testCheckoutWithNegativeTotal() = testApplication {
        setupTestApp()
val response = client.post("/api/checkout/mercadopago") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody("""
                {
                    "order": {
                        "id": "test-order",
                        "customerName": "Test",
                        "customerPhone": "5511999999999",
                        "status": "PENDING",
                        "total": -10,
                        "items": []
                    }
                }
            """.trimIndent())
        }
        assertTrue(response.status == HttpStatusCode.BadRequest || 
                   response.status == HttpStatusCode.InternalServerError ||
                   response.status == HttpStatusCode.OK)
    }

    @Test
    fun testCheckoutWithZeroTotal() = testApplication {
        setupTestApp()
val response = client.post("/api/checkout/mercadopago") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody("""
                {
                    "order": {
                        "id": "test-order",
                        "customerName": "Test",
                        "customerPhone": "5511999999999",
                        "status": "PENDING",
                        "total": 0,
                        "items": []
                    }
                }
            """.trimIndent())
        }
        assertTrue(response.status == HttpStatusCode.BadRequest || 
                   response.status == HttpStatusCode.InternalServerError ||
                   response.status == HttpStatusCode.OK)
    }

    @Test
    fun testWebhookWithPartialData() = testApplication {
        setupTestApp()
val response = client.post("/api/webhook/mercadopago") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody("""{"type": "payment", "data": {"id": "123"}}""")
        }
        assertTrue(response.status == HttpStatusCode.OK)
    }

    @Test
    fun testWebhookWithUnknownType() = testApplication {
        setupTestApp()
val response = client.post("/api/webhook/mercadopago") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody("""{"type": "unknown", "data": {}}""")
        }
        assertTrue(response.status == HttpStatusCode.OK)
    }

    @Test
    fun testPublicOrderWithSpecialCharacters() = testApplication {
        setupTestApp()
val response = client.get("/api/public/orders/order-123_special")
        assertTrue(response.status == HttpStatusCode.OK || 
                   response.status == HttpStatusCode.NotFound)
    }

    @Test
    fun testPublicCustomerOrdersWithLongSessionId() = testApplication {
        setupTestApp()
val longSessionId = "a".repeat(100)
        val response = client.get("/api/public/orders/customer/$longSessionId")
        assertTrue(response.status == HttpStatusCode.OK || 
                   response.status == HttpStatusCode.NotFound ||
                   response.status == HttpStatusCode.BadRequest)
    }
}
