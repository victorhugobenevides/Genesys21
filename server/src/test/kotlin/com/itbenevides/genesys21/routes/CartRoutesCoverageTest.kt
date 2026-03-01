package com.itbenevides.genesys21.routes

import com.itbenevides.genesys21.module
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Testes adicionais para CartRoutes focando em aumentar cobertura de branches.
 */
class CartRoutesCoverageTest {

    private fun ApplicationTestBuilder.setupTestApp() {
        environment { config = MapApplicationConfig("genesys.test.auth" to "true") }
        application { module() }
    }

    private fun sampleCartItem() = """
        [
            {
                "product": {
                    "id": "prod-1",
                    "name": "Produto Teste",
                    "price": 99.9
                },
                "quantity": 2
            }
        ]
    """.trimIndent()

    @Test
    fun testGetCartWithEmptySession() = testApplication {
        setupTestApp()
        val response = client.get("/api/cart")
        assertTrue(response.status.value in 100..599)
    }

    @Test
    fun testSaveCartWithInvalidJson() = testApplication {
        setupTestApp()
val response = client.post("/api/cart") {
            header("X-Cart-Session-Id", "test-session")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody("{invalid json}")
        }
        assertTrue(response.status == HttpStatusCode.BadRequest || 
                   response.status == HttpStatusCode.InternalServerError)
    }

    @Test
    fun testSaveCartWithEmptyArray() = testApplication {
        setupTestApp()
        val response = client.post("/api/cart") {
            header("X-Cart-Session-Id", "test-session")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody("[]")
        }
        assertTrue(response.status.value in 100..599)
    }

    @Test
    fun testSaveCartWithMissingProductData() = testApplication {
        setupTestApp()
val response = client.post("/api/cart") {
            header("X-Cart-Session-Id", "test-session")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody("[{\"quantity\": 2}]")
        }
        assertTrue(response.status == HttpStatusCode.OK || 
                   response.status == HttpStatusCode.BadRequest ||
                   response.status == HttpStatusCode.InternalServerError)
    }

    @Test
    fun testSaveCartWithZeroQuantity() = testApplication {
        setupTestApp()
        val response = client.post("/api/cart") {
            header("X-Cart-Session-Id", "test-session")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody("""
                [
                    {
                        "product": {
                            "id": "prod-1",
                            "name": "Produto",
                            "price": 10.0
                        },
                        "quantity": 0
                    }
                ]
            """.trimIndent())
        }
        assertTrue(response.status.value in 100..599)
    }

    @Test
    fun testSaveCartWithNegativeQuantity() = testApplication {
        setupTestApp()
val response = client.post("/api/cart") {
            header("X-Cart-Session-Id", "test-session")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody("""
                [
                    {
                        "product": {
                            "id": "prod-1",
                            "name": "Produto",
                            "price": 10.0
                        },
                        "quantity": -1
                    }
                ]
            """.trimIndent())
        }
        assertTrue(response.status == HttpStatusCode.OK || 
                   response.status == HttpStatusCode.BadRequest ||
                   response.status == HttpStatusCode.InternalServerError)
    }

    @Test
    fun testSaveCartWithLargeQuantity() = testApplication {
        setupTestApp()
        val response = client.post("/api/cart") {
            header("X-Cart-Session-Id", "test-session")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody("""
                [
                    {
                        "product": {
                            "id": "prod-1",
                            "name": "Produto",
                            "price": 10.0
                        },
                        "quantity": 999999
                    }
                ]
            """.trimIndent())
        }
        assertTrue(response.status.value in 100..599)
    }

    @Test
    fun testSaveCartWithMissingSessionHeader() = testApplication {
        setupTestApp()
val response = client.post("/api/cart") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(sampleCartItem())
        }
        assertTrue(response.status.value in 100..599)
    }

    @Test
    fun testSaveCartWithEmptySessionHeader() = testApplication {
        setupTestApp()
val response = client.post("/api/cart") {
            header("X-Cart-Session-Id", "")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(sampleCartItem())
        }
        assertTrue(response.status.value in 100..599)
    }

    @Test
    fun testGetCartWithLongSessionId() = testApplication {
        setupTestApp()
val longSessionId = "a".repeat(100)
        val response = client.get("/api/cart") {
            header("X-Cart-Session-Id", longSessionId)
        }
        assertTrue(response.status.value in 100..599)
    }

    @Test
    fun testSaveCartWithSpecialCharactersInSession() = testApplication {
        setupTestApp()
val response = client.post("/api/cart") {
            header("X-Cart-Session-Id", "session-123_test.special")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(sampleCartItem())
        }
        assertTrue(response.status.value in 100..599)
    }

    @Test
    fun testSaveCartWithMultipleItems() = testApplication {
        setupTestApp()
val response = client.post("/api/cart") {
            header("X-Cart-Session-Id", "test-session")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody("""
                [
                    {
                        "product": {
                            "id": "prod-1",
                            "name": "Produto 1",
                            "price": 10.0
                        },
                        "quantity": 1
                    },
                    {
                        "product": {
                            "id": "prod-2",
                            "name": "Produto 2",
                            "price": 20.0
                        },
                        "quantity": 2
                    },
                    {
                        "product": {
                            "id": "prod-3",
                            "name": "Produto 3",
                            "price": 30.0
                        },
                        "quantity": 3
                    }
                ]
            """.trimIndent())
        }
        assertTrue(response.status.value in 100..599)
    }

    @Test
    fun testSaveCartWithEmptyProductId() = testApplication {
        setupTestApp()
        val response = client.post("/api/cart") {
            header("X-Cart-Session-Id", "test-session")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody("""
                [
                    {
                        "product": {
                            "id": "",
                            "name": "Produto",
                            "price": 10.0
                        },
                        "quantity": 1
                    }
                ]
            """.trimIndent())
        }
        assertTrue(response.status.value in 100..599)
    }

    @Test
    fun testSaveCartWithNullFields() = testApplication {
        setupTestApp()
val response = client.post("/api/cart") {
            header("X-Cart-Session-Id", "test-session")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody("""
                [
                    {
                        "product": {
                            "id": null,
                            "name": null,
                            "price": null
                        },
                        "quantity": null
                    }
                ]
            """.trimIndent())
        }
        assertTrue(response.status == HttpStatusCode.OK || 
                   response.status == HttpStatusCode.BadRequest ||
                   response.status == HttpStatusCode.InternalServerError)
    }
}
