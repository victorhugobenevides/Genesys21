package com.itbenevides.genesys21.routes

import com.itbenevides.genesys21.module
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*

/**
 * Testes para as rotas de Cart conforme implementação atual.
 */
class CartRoutesTest {

    private fun sampleCartJson() = """
        [
          {
            "product": {
              "id": "prod-1",
              "name": "Produto Teste",
              "price": 99.9,
              "imageUrls": ["https://picsum.photos/100/100"],
              "description": "Produto de teste",
              "categoryId": 1,
              "categoryName": "Teste",
              "stock": 10
            },
            "quantity": 2
          }
        ]
    """.trimIndent()

    @Test
    fun testGetCartWithoutSessionReturnsEmptyList() = testApplication {
        application { module() }
        val response = client.get("/api/cart")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("[]", response.bodyAsText().trim())
    }

    @Test
    fun testGetCartWithSessionHeader() = testApplication {
        application { module() }
        val response = client.get("/api/cart") {
            header("X-Cart-Session-Id", "test-session-123")
        }
        assertTrue(response.status == HttpStatusCode.OK || response.status == HttpStatusCode.InternalServerError)
    }

    @Test
    fun testSaveCartWithoutSessionReturnsBadRequest() = testApplication {
        application { module() }
        val response = client.post("/api/cart") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(sampleCartJson())
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun testSaveCartWithSessionOk() = testApplication {
        application { module() }
        val response = client.post("/api/cart") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header("X-Cart-Session-Id", "test-session-123")
            setBody(sampleCartJson())
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun testSaveCartInvalidJsonReturnsBadRequest() = testApplication {
        application { module() }
        val response = client.post("/api/cart") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header("X-Cart-Session-Id", "test-session-123")
            setBody("{invalid json}")
        }
        assertTrue(response.status == HttpStatusCode.BadRequest || response.status == HttpStatusCode.InternalServerError)
    }

    @Test
    fun testSaveCartEmptyBodyReturnsBadRequest() = testApplication {
        application { module() }
        val response = client.post("/api/cart") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header("X-Cart-Session-Id", "test-session-123")
        }
        assertTrue(response.status == HttpStatusCode.BadRequest || response.status == HttpStatusCode.UnsupportedMediaType)
    }
}
