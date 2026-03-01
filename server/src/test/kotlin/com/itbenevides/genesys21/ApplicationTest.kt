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
}
