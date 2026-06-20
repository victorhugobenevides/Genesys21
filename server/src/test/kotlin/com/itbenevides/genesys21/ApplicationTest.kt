package com.itbenevides.genesys21

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlin.test.*

class ApplicationTest {
    @Test
    fun testRoot() =
        testApplication {
            environment {
                config = MapApplicationConfig("ktor.testing" to "true")
            }
            application {
                module()
            }
            val response = client.get("/")
            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(response.bodyAsText().contains("API Online"))
        }

    @Test
    fun testPagesListRequiresAuth() =
        testApplication {
            environment {
                config = MapApplicationConfig("ktor.testing" to "true")
            }
            application {
                module()
            }
            val response = client.get("/api/pages")
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun testCreatePageRequiresAuth() =
        testApplication {
            environment {
                config = MapApplicationConfig("ktor.testing" to "true")
            }
            application {
                module()
            }
            // Tentar criar sem token deve retornar 401
            val response =
                client.post("/api/pages") {
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    setBody("{\"id\":\"test\", \"title\":\"Test\"}")
                }
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
}
