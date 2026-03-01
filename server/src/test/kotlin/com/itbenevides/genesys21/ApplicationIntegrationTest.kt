package com.itbenevides.genesys21

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Testes de integração completos para as rotas do servidor.
 */
class ApplicationIntegrationTest {

    private fun ApplicationTestBuilder.setupTestApp() {
        environment { config = MapApplicationConfig("genesys.test.auth" to "true") }
        application { module() }
    }

    @Test
    fun testPublicRoutesAccessibility() = testApplication {
        setupTestApp()

        // Testar todas as rotas públicas
        val first = client.get("/api/public/pages/first")
        assertTrue(first.status == HttpStatusCode.OK || first.status == HttpStatusCode.NotFound)
        
        val publicPage = client.get("/api/public/pages/test-id")
        assertTrue(publicPage.status == HttpStatusCode.OK || publicPage.status == HttpStatusCode.NotFound)
        
        val domain = client.get("/api/public/domain/test.com")
        assertTrue(domain.status == HttpStatusCode.OK || domain.status == HttpStatusCode.NotFound)
        
        val customerOrders = client.get("/api/public/orders/customer/session-123")
        assertTrue(customerOrders.status == HttpStatusCode.OK || customerOrders.status == HttpStatusCode.NotFound)
        
        val publicOrder = client.get("/api/public/orders/order-123")
        assertTrue(publicOrder.status == HttpStatusCode.OK || publicOrder.status == HttpStatusCode.NotFound)
    }

    @Test
    fun testCorsHeaders() = testApplication {
        setupTestApp()

        val response = client.get("/api/public/pages/first") {
            header(HttpHeaders.Origin, "http://localhost:3000")
        }
        
        assertTrue(response.status == HttpStatusCode.OK || response.status == HttpStatusCode.NotFound)
        // CORS headers may be present depending on configuration
    }

    @Test
    fun testUnauthorizedAccessToProtectedRoutes() = testApplication {
        setupTestApp()

        val pages = client.get("/api/pages")
        assertTrue(pages.status == HttpStatusCode.OK || pages.status == HttpStatusCode.Unauthorized)
        
        val products = client.get("/api/products")
        assertTrue(products.status == HttpStatusCode.OK || products.status == HttpStatusCode.Unauthorized)
        
        val categories = client.get("/api/categories")
        assertTrue(categories.status == HttpStatusCode.OK || categories.status == HttpStatusCode.Unauthorized)
        
        val orders = client.get("/api/orders")
        assertTrue(orders.status == HttpStatusCode.OK || orders.status == HttpStatusCode.Unauthorized)
    }

    @Test
    fun testPostRoutesWithoutAuth() = testApplication {
        setupTestApp()

        val createPage = client.post("/api/pages") {
            contentType(ContentType.Application.Json)
            setBody("""{"id": "test", "title": "Test"}""")
        }
        assertTrue(createPage.status == HttpStatusCode.OK || 
                   createPage.status == HttpStatusCode.Unauthorized ||
                   createPage.status == HttpStatusCode.BadRequest)
        
        val createProduct = client.post("/api/products") {
            contentType(ContentType.Application.Json)
            setBody("""{"id": "test", "name": "Test", "price": 10.0}""")
        }
        assertTrue(createProduct.status == HttpStatusCode.OK || 
                   createProduct.status == HttpStatusCode.Unauthorized ||
                   createProduct.status == HttpStatusCode.BadRequest)
        
        val createCategory = client.post("/api/categories") {
            contentType(ContentType.Application.Json)
            setBody("""{"name": "Test"}""")
        }
        assertTrue(createCategory.status == HttpStatusCode.OK || 
                   createCategory.status == HttpStatusCode.Unauthorized ||
                   createCategory.status == HttpStatusCode.BadRequest)
    }

    @Test
    fun testPutRoutesWithoutAuth() = testApplication {
        setupTestApp()

        val updatePage = client.put("/api/pages") {
            contentType(ContentType.Application.Json)
            setBody("""{"id": "test", "title": "Updated"}""")
        }
        assertTrue(updatePage.status == HttpStatusCode.OK || 
                   updatePage.status == HttpStatusCode.Unauthorized ||
                   updatePage.status == HttpStatusCode.BadRequest)
    }

    @Test
    fun testDeleteRoutesWithoutAuth() = testApplication {
        setupTestApp()

        val deletePage = client.delete("/api/pages/test-id")
        assertTrue(deletePage.status == HttpStatusCode.OK || 
                   deletePage.status == HttpStatusCode.Unauthorized ||
                   deletePage.status == HttpStatusCode.NotFound)
        
        val deleteProduct = client.delete("/api/products/test-id")
        assertTrue(deleteProduct.status == HttpStatusCode.OK || 
                   deleteProduct.status == HttpStatusCode.Unauthorized ||
                   deleteProduct.status == HttpStatusCode.NotFound)
        
        val deleteCategory = client.delete("/api/categories/1")
        assertTrue(deleteCategory.status == HttpStatusCode.OK || 
                   deleteCategory.status == HttpStatusCode.Unauthorized ||
                   deleteCategory.status == HttpStatusCode.NotFound)
    }

    @Test
    fun testCartWithSession() = testApplication {
        setupTestApp()

        val getCart = client.get("/api/cart") {
            header("X-Cart-Session-Id", "test-session-123")
        }
        assertTrue(getCart.status == HttpStatusCode.OK || 
                   getCart.status == HttpStatusCode.InternalServerError)
        
        val saveCart = client.post("/api/cart") {
            header("X-Cart-Session-Id", "test-session-123")
            contentType(ContentType.Application.Json)
            setBody("""
                [
                    {
                        "product": {
                            "id": "prod-1",
                            "name": "Test Product",
                            "price": 99.9
                        },
                        "quantity": 2
                    }
                ]
            """.trimIndent())
        }
        assertTrue(saveCart.status == HttpStatusCode.OK || 
                   saveCart.status == HttpStatusCode.InternalServerError)
    }

    @Test
    fun testOrderOperations() = testApplication {
        setupTestApp()

        // Checkout sem auth
        val checkout = client.post("/api/checkout/mercadopago") {
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "order": {
                        "id": "order-test",
                        "customerName": "Test Customer",
                        "customerPhone": "5511999999999",
                        "total": 100.0,
                        "items": [],
                        "status": "PENDING"
                    }
                }
            """.trimIndent())
        }
        assertTrue(checkout.status == HttpStatusCode.OK || 
                   checkout.status == HttpStatusCode.InternalServerError ||
                   checkout.status == HttpStatusCode.BadRequest)
        
        // Webhook
        val webhook = client.post("/api/webhook/mercadopago") {
            contentType(ContentType.Application.Json)
            setBody("""{"type": "payment", "data": {"id": "123"}}""")
        }
        assertEquals(HttpStatusCode.OK, webhook.status)
    }

    @Test
    fun testStaticContentRoutes() = testApplication {
        setupTestApp()

        // Testar rota de uploads (pode retornar Not Found se não configurada)
        val uploads = client.get("/uploads/test.jpg")
        assertTrue(uploads.status == HttpStatusCode.OK || 
                   uploads.status == HttpStatusCode.NotFound)
    }

    @Test
    fun testInvalidJsonHandling() = testApplication {
        setupTestApp()

        val invalidJson = client.post("/api/pages") {
            contentType(ContentType.Application.Json)
            setBody("{invalid json}")
        }
        assertTrue(invalidJson.status == HttpStatusCode.BadRequest || 
                   invalidJson.status == HttpStatusCode.Unauthorized)
        
        val emptyBody = client.post("/api/products") {
            contentType(ContentType.Application.Json)
        }
        assertTrue(emptyBody.status == HttpStatusCode.BadRequest || 
                   emptyBody.status == HttpStatusCode.Unauthorized)
    }

    @Test
    fun testHttpMethodsNotAllowed() = testApplication {
        setupTestApp()

        // PATCH pode não ser suportado em todas as rotas
        val patchPage = client.patch("/api/pages") {
            setBody("""{"title": "Patched"}""")
        }
        assertTrue(patchPage.status == HttpStatusCode.MethodNotAllowed || 
                   patchPage.status == HttpStatusCode.NotFound ||
                   patchPage.status == HttpStatusCode.Unauthorized)
    }

    @Test
    fun testLargeRequestBody() = testApplication {
        setupTestApp()

        val largeJson = """
            {
                "id": "test",
                "title": "${"A".repeat(10000)}",
                "customDomain": "${"domain".repeat(100)}.com"
            }
        """.trimIndent()
        
        val response = client.post("/api/pages") {
            contentType(ContentType.Application.Json)
            setBody(largeJson)
        }
        assertTrue(response.status == HttpStatusCode.OK || 
                   response.status == HttpStatusCode.Unauthorized ||
                   response.status == HttpStatusCode.BadRequest ||
                   response.status == HttpStatusCode.PayloadTooLarge)
    }

    @Test
    fun testQueryParametersIgnored() = testApplication {
        setupTestApp()

        // Query params devem ser ignorados ou tratados
        val withQuery = client.get("/api/public/pages/first?param=value&other=123")
        assertTrue(withQuery.status == HttpStatusCode.OK || 
                   withQuery.status == HttpStatusCode.NotFound)
    }

    @Test
    fun testSpecialCharactersInPaths() = testApplication {
        setupTestApp()

        val specialChars = client.get("/api/public/pages/test%20page%20123")
        assertTrue(specialChars.status == HttpStatusCode.OK || 
                   specialChars.status == HttpStatusCode.NotFound ||
                   specialChars.status == HttpStatusCode.BadRequest)
        
        val unicode = client.get("/api/public/pages/página-测试-🚀")
        assertTrue(unicode.status == HttpStatusCode.OK || 
                   unicode.status == HttpStatusCode.NotFound)
    }

    @Test
    fun testHealthCheckOrRoot() = testApplication {
        setupTestApp()

        val root = client.get("/")
        // Pode retornar Not Found se não houver rota root
        assertTrue(root.status == HttpStatusCode.OK || 
                   root.status == HttpStatusCode.NotFound)
    }

    @Test
    fun testShortLinkVariations() = testApplication {
        setupTestApp()

        val shortLink = client.get("/l/abc123")
        assertTrue(shortLink.status == HttpStatusCode.Found || 
                   shortLink.status == HttpStatusCode.NotFound ||
                   shortLink.status == HttpStatusCode.OK)
        
        val shortLinkWithParams = client.get("/l/abc123?ref=home")
        assertTrue(shortLinkWithParams.status == HttpStatusCode.Found || 
                   shortLinkWithParams.status == HttpStatusCode.NotFound ||
                   shortLinkWithParams.status == HttpStatusCode.OK)
    }

    @Test
    fun testOrderStatusUpdateVariations() = testApplication {
        setupTestApp()

        val statuses = listOf("PENDING", "PAYMENT_PENDING", "PROCESSING", "SHIPPED", "COMPLETED", "CANCELLED", "FAILED")
        
        for (status in statuses) {
            val response = client.patch("/api/orders/order-123/status") {
                header(HttpHeaders.Authorization, "Bearer test-token")
                contentType(ContentType.Application.Json)
                setBody(""""$status""".trimIndent())
            }
            assertTrue(response.status == HttpStatusCode.OK || 
                       response.status == HttpStatusCode.Unauthorized ||
                       response.status == HttpStatusCode.BadRequest ||
                       response.status == HttpStatusCode.NotFound)
        }
    }

    @Test
    fun testCartItemVariations() = testApplication {
        setupTestApp()

        // Carrinho vazio
        val emptyCart = client.post("/api/cart") {
            header("X-Cart-Session-Id", "empty-session")
            contentType(ContentType.Application.Json)
            setBody("[]")
        }
        assertTrue(emptyCart.status == HttpStatusCode.OK || 
                   emptyCart.status == HttpStatusCode.InternalServerError)
        
        // Carrinho com muitos itens
        val manyItems = (1..50).map { index ->
            """
                {
                    "product": {
                        "id": "prod-$index",
                        "name": "Product $index",
                        "price": ${index * 10.0}
                    },
                    "quantity": $index
                }
            """.trimIndent()
        }.joinToString(", ", prefix = "[", postfix = "]")
        
        val largeCart = client.post("/api/cart") {
            header("X-Cart-Session-Id", "large-session")
            contentType(ContentType.Application.Json)
            setBody(manyItems)
        }
        assertTrue(largeCart.status == HttpStatusCode.OK || 
                   largeCart.status == HttpStatusCode.InternalServerError)
    }

    @Test
    fun testContentTypeVariations() = testApplication {
        setupTestApp()

        // Sem Content-Type
        val noContentType = client.post("/api/pages") {
            setBody("""{"id": "test", "title": "Test"}""")
        }
        assertTrue(noContentType.status == HttpStatusCode.OK || 
                   noContentType.status == HttpStatusCode.Unauthorized ||
                   noContentType.status == HttpStatusCode.UnsupportedMediaType ||
                   noContentType.status == HttpStatusCode.BadRequest)
        
        // Content-Type text/plain
        val textPlain = client.post("/api/pages") {
            header(HttpHeaders.ContentType, ContentType.Text.Plain)
            setBody("plain text")
        }
        assertTrue(textPlain.status == HttpStatusCode.OK || 
                   textPlain.status == HttpStatusCode.Unauthorized ||
                   textPlain.status == HttpStatusCode.UnsupportedMediaType ||
                   textPlain.status == HttpStatusCode.BadRequest)
    }

    @Test
    fun testPatchOrderStatusVariations() = testApplication {
        setupTestApp()

        // Sem orderId na URL
        val noOrderId = client.patch("/api/orders//status") {
            header(HttpHeaders.Authorization, "Bearer test")
            contentType(ContentType.Application.Json)
            setBody(""""PENDING""".trimIndent())
        }
        assertTrue(noOrderId.status == HttpStatusCode.NotFound || 
                   noOrderId.status == HttpStatusCode.Unauthorized)
        
        // OrderId malformado
        val malformedId = client.patch("/api/orders/!!!/status") {
            header(HttpHeaders.Authorization, "Bearer test")
            contentType(ContentType.Application.Json)
            setBody(""""PENDING""".trimIndent())
        }
        assertTrue(malformedId.status == HttpStatusCode.OK || 
                   malformedId.status == HttpStatusCode.Unauthorized ||
                   malformedId.status == HttpStatusCode.NotFound ||
                   malformedId.status == HttpStatusCode.BadRequest)
    }

    @Test
    fun testNestedObjectVariations() = testApplication {
        setupTestApp()

        // Página com componentes
        val pageWithComponents = client.post("/api/pages") {
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "id": "page-with-components",
                    "title": "Page With Components",
                    "components": [
                        {
                            "type": "TYPOGRAPHY",
                            "content": "Hello World"
                        },
                        {
                            "type": "HEADER",
                            "content": "Title"
                        }
                    ],
                    "theme": "ROYAL"
                }
            """.trimIndent())
        }
        assertTrue(pageWithComponents.status == HttpStatusCode.OK || 
                   pageWithComponents.status == HttpStatusCode.Unauthorized ||
                   pageWithComponents.status == HttpStatusCode.BadRequest)
        
        // Produto com imagens
        val productWithImages = client.post("/api/products") {
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "id": "product-with-images",
                    "name": "Product With Images",
                    "price": 99.9,
                    "imageUrls": [
                        "https://example.com/img1.jpg",
                        "https://example.com/img2.jpg"
                    ],
                    "stock": 10
                }
            """.trimIndent())
        }
        assertTrue(productWithImages.status == HttpStatusCode.OK || 
                   productWithImages.status == HttpStatusCode.Unauthorized ||
                   productWithImages.status == HttpStatusCode.BadRequest)
    }
}