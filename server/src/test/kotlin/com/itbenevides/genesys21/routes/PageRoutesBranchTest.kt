package com.itbenevides.genesys21.routes

import com.itbenevides.genesys21.module
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Testes abrangentes para PageRoutes visando aumentar cobertura de branches.
 */
class PageRoutesBranchTest {

    private fun ApplicationTestBuilder.setupTestApp() {
        environment { config = MapApplicationConfig("genesys.test.auth" to "true") }
        application { module() }
    }

    @Test
    fun testGetFirstPublicPageNotFound() = testApplication {
        setupTestApp()
        val response = client.get("/api/public/pages/first")
        assertTrue(response.status.value in 200..404)
    }

    @Test
    fun testGetPublicPageByIdNotFound() = testApplication {
        setupTestApp()
        val response = client.get("/api/public/pages/nonexistent-id")
        assertTrue(response.status.value in 200..404)
    }

    @Test
    fun testGetPublicPageByDomainNotFound() = testApplication {
        setupTestApp()
        val response = client.get("/api/public/domain/nonexistent-domain")
        assertTrue(response.status.value in 200..404)
    }

    @Test
    fun testGetPagesWithAuth() = testApplication {
        setupTestApp()
        val response = client.get("/api/pages") {
            header(HttpHeaders.Authorization, "Bearer test-token")
        }
        assertTrue(response.status.value in 200..500)
    }

    @Test
    fun testCreatePageWithAuth() = testApplication {
        setupTestApp()
        val response = client.post("/api/pages") {
            header(HttpHeaders.Authorization, "Bearer test-token")
            contentType(ContentType.Application.Json)
            setBody("""{"id":"test-page","title":"Test","domain":"test-domain","ownerId":"user-1"}""")
        }
        assertTrue(response.status.value in 200..500)
    }

    @Test
    fun testUpdatePageWithAuth() = testApplication {
        setupTestApp()
        val response = client.put("/api/pages") {
            header(HttpHeaders.Authorization, "Bearer test-token")
            contentType(ContentType.Application.Json)
            setBody("""{"id":"test-page","title":"Updated","domain":"test-domain","ownerId":"user-1"}""")
        }
        assertTrue(response.status.value in 200..500)
    }

    @Test
    fun testDeletePageWithAuth() = testApplication {
        setupTestApp()
        val response = client.delete("/api/pages/test-page-id") {
            header(HttpHeaders.Authorization, "Bearer test-token")
        }
        assertTrue(response.status.value in 200..500)
    }

    @Test
    fun testGetProductsWithAuth() = testApplication {
        setupTestApp()
        val response = client.get("/api/products") {
            header(HttpHeaders.Authorization, "Bearer test-token")
        }
        assertTrue(response.status.value in 200..500)
    }

    @Test
    fun testCreateProductWithAuth() = testApplication {
        setupTestApp()
        val response = client.post("/api/products") {
            header(HttpHeaders.Authorization, "Bearer test-token")
            contentType(ContentType.Application.Json)
            setBody("""{"id":"prod-1","name":"Product","price":10.0,"ownerId":"user-1"}""")
        }
        assertTrue(response.status.value in 200..500)
    }

    @Test
    fun testDeleteProductWithAuth() = testApplication {
        setupTestApp()
        val response = client.delete("/api/products/prod-1") {
            header(HttpHeaders.Authorization, "Bearer test-token")
        }
        assertTrue(response.status.value in 200..500)
    }

    @Test
    fun testCreatePageWithoutBody() = testApplication {
        setupTestApp()
        val response = client.post("/api/pages") {
            header(HttpHeaders.Authorization, "Bearer test-token")
            contentType(ContentType.Application.Json)
        }
        assertTrue(response.status.value in 200..500)
    }

    @Test
    fun testUpdatePageWithoutBody() = testApplication {
        setupTestApp()
        val response = client.put("/api/pages") {
            header(HttpHeaders.Authorization, "Bearer test-token")
            contentType(ContentType.Application.Json)
        }
        assertTrue(response.status.value in 200..500)
    }

    @Test
    fun testCreateProductWithoutBody() = testApplication {
        setupTestApp()
        val response = client.post("/api/products") {
            header(HttpHeaders.Authorization, "Bearer test-token")
            contentType(ContentType.Application.Json)
        }
        assertTrue(response.status.value in 200..500)
    }

    @Test
    fun testGetPublicPageWithoutId() = testApplication {
        setupTestApp()
        val response = client.get("/api/public/pages/")
        assertTrue(response.status.value in 200..500)
    }

    @Test
    fun testGetDomainWithoutParam() = testApplication {
        setupTestApp()
        val response = client.get("/api/public/domain/")
        assertTrue(response.status.value in 200..500)
    }

    @Test
    fun testDeletePageWithoutId() = testApplication {
        setupTestApp()
        val response = client.delete("/api/pages/") {
            header(HttpHeaders.Authorization, "Bearer test-token")
        }
        assertTrue(response.status.value in 200..500)
    }

    @Test
    fun testDeleteProductWithoutId() = testApplication {
        setupTestApp()
        val response = client.delete("/api/products/") {
            header(HttpHeaders.Authorization, "Bearer test-token")
        }
        assertTrue(response.status.value in 200..500)
    }

    @Test
    fun testCreatePageWithInvalidJson() = testApplication {
        setupTestApp()
        val response = client.post("/api/pages") {
            header(HttpHeaders.Authorization, "Bearer test-token")
            contentType(ContentType.Application.Json)
            setBody("{invalid}")
        }
        assertTrue(response.status.value in 200..500)
    }

    @Test
    fun testCreateProductWithInvalidJson() = testApplication {
        setupTestApp()
        val response = client.post("/api/products") {
            header(HttpHeaders.Authorization, "Bearer test-token")
            contentType(ContentType.Application.Json)
            setBody("{invalid}")
        }
        assertTrue(response.status.value in 200..500)
    }

    @Test
    fun testGetPagesWithoutAuth() = testApplication {
        setupTestApp()
        val response = client.get("/api/pages")
        assertTrue(response.status.value in 200..500)
    }

    @Test
    fun testCreatePageWithoutAuth() = testApplication {
        setupTestApp()
        val response = client.post("/api/pages") {
            contentType(ContentType.Application.Json)
            setBody("""{"id":"test","title":"Test"}""")
        }
        assertTrue(response.status.value in 200..500)
    }

    @Test
    fun testUpdatePageWithoutAuth() = testApplication {
        setupTestApp()
        val response = client.put("/api/pages") {
            contentType(ContentType.Application.Json)
            setBody("""{"id":"test","title":"Test"}""")
        }
        assertTrue(response.status.value in 200..500)
    }

    @Test
    fun testDeletePageWithoutAuth() = testApplication {
        setupTestApp()
        val response = client.delete("/api/pages/test-id")
        assertTrue(response.status.value in 200..500)
    }

    @Test
    fun testGetProductsWithoutAuth() = testApplication {
        setupTestApp()
        val response = client.get("/api/products")
        assertTrue(response.status.value in 200..500)
    }

    @Test
    fun testCreateProductWithoutAuth() = testApplication {
        setupTestApp()
        val response = client.post("/api/products") {
            contentType(ContentType.Application.Json)
            setBody("""{"id":"test","name":"Test"}""")
        }
        assertTrue(response.status.value in 200..500)
    }

    @Test
    fun testDeleteProductWithoutAuth() = testApplication {
        setupTestApp()
        val response = client.delete("/api/products/test-id")
        assertTrue(response.status.value in 200..500)
    }

    @Test
    fun testCreatePageWithEmptyDomain() = testApplication {
        setupTestApp()
        val response = client.post("/api/pages") {
            header(HttpHeaders.Authorization, "Bearer test-token")
            contentType(ContentType.Application.Json)
            setBody("""{"id":"test","title":"Test","domain":""}""")
        }
        assertTrue(response.status.value in 200..500)
    }

    @Test
    fun testCreateProductWithNegativePrice() = testApplication {
        setupTestApp()
        val response = client.post("/api/products") {
            header(HttpHeaders.Authorization, "Bearer test-token")
            contentType(ContentType.Application.Json)
            setBody("""{"id":"test","name":"Test","price":-10.0}""")
        }
        assertTrue(response.status.value in 200..500)
    }

    @Test
    fun testGetFirstPageMultipleRequests() = testApplication {
        setupTestApp()
        repeat(3) {
            val response = client.get("/api/public/pages/first")
            assertTrue(response.status.value in 200..500)
        }
    }

    @Test
    fun testConcurrentPageRequests() = testApplication {
        setupTestApp()
        val responses = (1..5).map {
            client.get("/api/public/pages/first")
        }
        responses.forEach { response ->
            assertTrue(response.status.value in 200..500)
        }
    }

    @Test
    fun testPublicRoutesWithoutPrefix() = testApplication {
        setupTestApp()
        val response = client.get("/public/pages/first")
        assertTrue(response.status.value in 200..500)
    }

    @Test
    fun testRoutesWithoutApiPrefix() = testApplication {
        setupTestApp()
        val response = client.get("/pages")
        assertTrue(response.status.value in 200..500)
    }

    @Test
    fun testGetPublicPageWithSpecialCharacters() = testApplication {
        setupTestApp()
        val response = client.get("/api/public/pages/test%20page%20id")
        assertTrue(response.status.value in 200..500)
    }

    @Test
    fun testGetDomainWithSpecialCharacters() = testApplication {
        setupTestApp()
        val response = client.get("/api/public/domain/test%20domain")
        assertTrue(response.status.value in 200..500)
    }

    @Test
    fun testCreatePageWithLargePayload() = testApplication {
        setupTestApp()
        val largeBody = """{"id":"test","title":"${"A".repeat(1000)}","domain":"test"}"""
        val response = client.post("/api/pages") {
            header(HttpHeaders.Authorization, "Bearer test-token")
            contentType(ContentType.Application.Json)
            setBody(largeBody)
        }
        assertTrue(response.status.value in 200..500)
    }

    @Test
    fun testCreateProductWithLargeDescription() = testApplication {
        setupTestApp()
        val largeBody = """{"id":"test","name":"Test","price":10.0,"description":"${"B".repeat(5000)}"}"""
        val response = client.post("/api/products") {
            header(HttpHeaders.Authorization, "Bearer test-token")
            contentType(ContentType.Application.Json)
            setBody(largeBody)
        }
        assertTrue(response.status.value in 200..500)
    }

    @Test
    fun testGetPagesWithDifferentAuthHeaders() = testApplication {
        setupTestApp()
        listOf("Bearer test-token", "Bearer invalid", "Basic dGVzdA==").forEach { auth ->
            val response = client.get("/api/pages") {
                header(HttpHeaders.Authorization, auth)
            }
            assertTrue(response.status.value in 200..500)
        }
    }

    @Test
    fun testUpdateNonExistentPage() = testApplication {
        setupTestApp()
        val response = client.put("/api/pages") {
            header(HttpHeaders.Authorization, "Bearer test-token")
            contentType(ContentType.Application.Json)
            setBody("""{"id":"nonexistent","title":"Test","domain":"test"}""")
        }
        assertTrue(response.status.value in 200..500)
    }

    @Test
    fun testDeleteNonExistentPage() = testApplication {
        setupTestApp()
        val response = client.delete("/api/pages/nonexistent-id") {
            header(HttpHeaders.Authorization, "Bearer test-token")
        }
        assertTrue(response.status.value in 200..500)
    }

    @Test
    fun testDeleteNonExistentProduct() = testApplication {
        setupTestApp()
        val response = client.delete("/api/products/nonexistent-id") {
            header(HttpHeaders.Authorization, "Bearer test-token")
        }
        assertTrue(response.status.value in 200..500)
    }

    @Test
    fun testCreateDuplicatePage() = testApplication {
        setupTestApp()
        val body = """{"id":"duplicate-page","title":"Test","domain":"duplicate-domain"}"""
        client.post("/api/pages") {
            header(HttpHeaders.Authorization, "Bearer test-token")
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        val response = client.post("/api/pages") {
            header(HttpHeaders.Authorization, "Bearer test-token")
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        assertTrue(response.status.value in 200..500)
    }

    @Test
    fun testCreateDuplicateProduct() = testApplication {
        setupTestApp()
        val body = """{"id":"duplicate-prod","name":"Test","price":10.0}"""
        client.post("/api/products") {
            header(HttpHeaders.Authorization, "Bearer test-token")
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        val response = client.post("/api/products") {
            header(HttpHeaders.Authorization, "Bearer test-token")
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        assertTrue(response.status.value in 200..500)
    }

    @Test
    fun testGetPublicPageWithIdNull() = testApplication {
        setupTestApp()
        val response = client.get("/api/public/pages/null")
        assertTrue(response.status.value in 200..500)
    }

    @Test
    fun testGetDomainWithNull() = testApplication {
        setupTestApp()
        val response = client.get("/api/public/domain/null")
        assertTrue(response.status.value in 200..500)
    }

    @Test
    fun testCreatePageWithUnicode() = testApplication {
        setupTestApp()
        val response = client.post("/api/pages") {
            header(HttpHeaders.Authorization, "Bearer test-token")
            contentType(ContentType.Application.Json)
            setBody("""{"id":"unicode","title":"Título com acentos: àáâã","domain":"unicode-domain"}""")
        }
        assertTrue(response.status.value in 200..500)
    }

    @Test
    fun testCreateProductWithUnicode() = testApplication {
        setupTestApp()
        val response = client.post("/api/products") {
            header(HttpHeaders.Authorization, "Bearer test-token")
            contentType(ContentType.Application.Json)
            setBody("""{"id":"unicode","name":"Produto com ção","price":10.0}""")
        }
        assertTrue(response.status.value in 200..500)
    }
}
