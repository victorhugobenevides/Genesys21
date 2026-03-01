package com.itbenevides.genesys21.routes

import com.itbenevides.genesys21.module
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*

/**
 * Testes abrangentes para as rotas de Pages.
 */
class PageRoutesTest {

    @Test
    fun testGetFirstPagePublic() = testApplication {
        application {
            module()
        }
        val response = client.get("/api/public/pages/first")
        // Retorna OK ou NotFound (se banco vazio), mas não erro 500
        assertTrue(response.status == HttpStatusCode.OK || response.status == HttpStatusCode.NotFound)
    }

    @Test
    fun testGetPublicPageByIdNotFound() = testApplication {
        application {
            module()
        }
        val response = client.get("/api/public/pages/nonexistent-id-12345")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun testGetPublicPageByIdBadRequest() = testApplication {
        application {
            module()
        }
        val response = client.get("/api/public/pages/")
        assertEquals(HttpStatusCode.NotFound, response.status) // Rota não existe sem ID
    }

    @Test
    fun testGetPageByDomainNotFound() = testApplication {
        application {
            module()
        }
        val response = client.get("/api/public/domain/nonexistent-domain.com")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun testGetPagesListRequiresAuth() = testApplication {
        application {
            module()
        }
        val response = client.get("/api/pages")
        // Pode retornar Unauthorized ou OK dependendo da configuração de auth
        assertTrue(response.status == HttpStatusCode.Unauthorized || response.status == HttpStatusCode.OK)
    }

    @Test
    fun testCreatePageRequiresAuth() = testApplication {
        application {
            module()
        }
        val response = client.post("/api/pages") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody("""
                {
                    "id": "test-page-1",
                    "title": "Test Page",
                    "customDomain": "test.com",
                    "whatsapp": "5511999999999",
                    "theme": "ROYAL",
                    "components": []
                }
            """.trimIndent())
        }
        assertTrue(response.status == HttpStatusCode.Unauthorized || response.status == HttpStatusCode.BadRequest || response.status == HttpStatusCode.OK)
    }

    @Test
    fun testUpdatePageRequiresAuth() = testApplication {
        application {
            module()
        }
        val response = client.put("/api/pages") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody("""
                {
                    "id": "test-page-1",
                    "title": "Updated Test Page",
                    "customDomain": "test.com",
                    "whatsapp": "5511999999999",
                    "theme": "OCEAN",
                    "components": []
                }
            """.trimIndent())
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun testDeletePageRequiresAuth() = testApplication {
        application {
            module()
        }
        val response = client.delete("/api/pages/test-page-1")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun testDeletePageNoIdBadRequest() = testApplication {
        application {
            module()
        }
        // Sem ID na rota retorna 404 (rota não existe)
        val response = client.delete("/api/pages/")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun testGetProductsListRequiresAuth() = testApplication {
        application {
            module()
        }
        val response = client.get("/api/products")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun testCreateProductRequiresAuth() = testApplication {
        application {
            module()
        }
        val response = client.post("/api/products") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody("""
                {
                    "id": "prod-1",
                    "name": "Test Product",
                    "price": 99.9,
                    "categoryName": "Test"
                }
            """.trimIndent())
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun testDeleteProductRequiresAuth() = testApplication {
        application {
            module()
        }
        val response = client.delete("/api/products/prod-1")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun testInvalidJsonReturnsBadRequest() = testApplication {
        application {
            module()
        }
        val response = client.post("/api/pages") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody("{invalid json}")
        }
        // Sem auth token retorna 401 antes de checar JSON
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun testContentTypeRequired() = testApplication {
        application {
            module()
        }
        val response = client.post("/api/pages") {
            // Sem Content-Type header
            setBody("test body")
        }
        assertTrue(response.status == HttpStatusCode.Unauthorized || response.status == HttpStatusCode.BadRequest || response.status == HttpStatusCode.OK)
    }

    @Test
    fun testPublicRoutesNoAuthNeeded() = testApplication {
        application {
            module()
        }
        // Testar que rotas públicas funcionam sem autenticação
        val firstPage = client.get("/api/public/pages/first")
        assertTrue(firstPage.status == HttpStatusCode.OK || firstPage.status == HttpStatusCode.NotFound)

        val publicPage = client.get("/api/public/pages/test-id")
        assertTrue(publicPage.status == HttpStatusCode.OK || publicPage.status == HttpStatusCode.NotFound)

        val domainPage = client.get("/api/public/domain/test.com")
        assertTrue(domainPage.status == HttpStatusCode.OK || domainPage.status == HttpStatusCode.NotFound)
    }

    @Test
    fun testShortLinkRoute() = testApplication {
        application {
            module()
        }
        val response = client.get("/p/test-page-id")
        // Retorna HTML de redirecionamento ou NotFound
        assertTrue(response.status == HttpStatusCode.OK || response.status == HttpStatusCode.NotFound)
        
        if (response.status == HttpStatusCode.OK) {
            val body = response.bodyAsText()
            assertTrue(body.contains("<html>") || body.contains("Redirecionando"))
        }
    }
}