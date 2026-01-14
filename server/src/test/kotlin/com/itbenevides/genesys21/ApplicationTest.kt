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
        assertTrue(response.bodyAsText().contains("Genesys21 API Online"))
    }

    @Test
    fun testPagesListPublic() = testApplication {
        application {
            module()
        }
        val response = client.get("/pages")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun testCreatePageRequiresAuth() = testApplication {
        application {
            module()
        }
        // Tentar criar sem token deve retornar 401
        val response = client.post("/pages") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody("{\"id\":\"test\", \"title\":\"Test\"}")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
