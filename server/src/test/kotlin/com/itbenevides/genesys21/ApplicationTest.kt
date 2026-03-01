package com.itbenevides.genesys21

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*

class ApplicationTest {

    @Test
    fun testRoot() = testApplication {
        application {
            module()
        }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        // Ajustado para a mensagem real do seu Application.kt
        assertTrue(response.bodyAsText().contains("API Online"))
    }

    @Test
    fun testPagesListPublic() = testApplication {
        application {
            module()
        }
        // As rotas públicas agora estão em /api/public/pages/... ou /p/{id}
        // Vamos testar a raiz da API pública
        val response = client.get("/api/public/pages/first")
        // Como o banco pode estar vazio no teste, aceitamos OK ou NotFound, mas não erro 500
        assertTrue(response.status == HttpStatusCode.OK || response.status == HttpStatusCode.NotFound)
    }

    @Test
    fun testCreatePageRequiresAuth() = testApplication {
        application {
            module()
        }
        // Rotas administrativas estão em /api/pages
        val response = client.post("/api/pages") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody("{\"id\":\"test\", \"title\":\"Test\"}")
        }
        // Deve retornar 401 porque não enviamos token Firebase
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun testPublicPageByIdRequiresId() = testApplication {
        application {
            module()
        }
        // Testando sem ID deve retornar BadRequest
        val response = client.get("/api/public/pages/")
        // Se o path não tiver ID, retorna 404 (Not Found) pois a rota não existe
        assertTrue(response.status == HttpStatusCode.NotFound)
    }

    @Test
    fun testPublicDomainRequiresDomain() = testApplication {
        application {
            module()
        }
        // Testando a rota de domínio
        val response = client.get("/api/public/domain/test.com")
        // Pode retornar 404 (domínio não existe) mas não 500 (erro interno)
        assertTrue(response.status == HttpStatusCode.OK || response.status == HttpStatusCode.NotFound)
    }

    @Test
    fun testShortLinkRedirects() = testApplication {
        application {
            module()
        }
        // Testando a rota curta /p/{id}
        val response = client.get("/p/nonexistent")
        // Deve retornar HTML de redirecionamento (mesmo para página inexistente)
        assertTrue(response.status == HttpStatusCode.OK || response.status == HttpStatusCode.NotFound)
        val body = response.bodyAsText()
        // Para páginas inexistentes, ainda retorna HTML com redirecionamento
        assertTrue(body.contains("<html>") || body.contains("Redirecionando") || response.status == HttpStatusCode.NotFound)
    }

    @Test
    fun testCorsHeaders() = testApplication {
        application {
            module()
        }
        val response = client.get("/") {
            header(HttpHeaders.Origin, "http://localhost:3000")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        // CORS middleware deve adicionar headers
        assertNotNull(response.headers[HttpHeaders.AccessControlAllowOrigin])
    }

    @Test
    fun testProductsRouteRequiresAuth() = testApplication {
        application {
            module()
        }
        val response = client.get("/api/products")
        // Deve retornar 401 porque não enviamos token
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun testUploadRequiresAuth() = testApplication {
        application {
            module()
        }
        val response = client.post("/api/upload")
        // Deve retornar 401 porque não enviamos token
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun testCartRoutesPublic() = testApplication {
        application {
            module()
        }
        // GET cart deve ser acessível (retorna 400 se falta header, mas não 500)
        val response = client.get("/api/cart")
        // Pode retornar 400 (Bad Request) se falta session ID, mas não 500
        assertTrue(response.status == HttpStatusCode.OK || response.status == HttpStatusCode.BadRequest)
    }

    @Test
    fun testOrdersRouteRequiresAuth() = testApplication {
        application {
            module()
        }
        val response = client.get("/api/orders")
        // Deve retornar 401 porque não enviamos token
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun testHealthCheck() = testApplication {
        application {
            module()
        }
        // Testando se a API está respondendo
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("API Online", response.bodyAsText())
    }

    @Test
    fun testInvalidRouteReturns404() = testApplication {
        application {
            module()
        }
        val response = client.get("/invalid/route/that/does/not/exist")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}
