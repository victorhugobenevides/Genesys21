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
        assertTrue(response.bodyAsText().contains("Firebase Admin: Ativo"))
    }

    @Test
    fun testValidateTokenInvalid() = testApplication {
        application {
            module()
        }
        // Testamos enviando um token inválido, o servidor deve retornar Unauthorized (401)
        val response = client.post("/validate-token") {
            setBody("invalid-token")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
