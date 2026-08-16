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
    fun testPublicServicesList() =
        testApplication {
            environment { config = MapApplicationConfig("ktor.testing" to "true") }
            application { module() }
            val response = client.get("/api/booking/services")
            assertEquals(HttpStatusCode.OK, response.status)
        }

    @Test
    fun testPublicPagesFirst() =
        testApplication {
            environment { config = MapApplicationConfig("ktor.testing" to "true") }
            application { module() }
            // Como o banco está limpo, deve retornar 404 ou 200 vazio dependendo da lógica
            val response = client.get("/api/public/pages/first")
            assertTrue(response.status == HttpStatusCode.OK || response.status == HttpStatusCode.NotFound)
        }

    @Test
    fun testCartRequiresSessionOrAuth() =
        testApplication {
            environment { config = MapApplicationConfig("ktor.testing" to "true") }
            application { module() }
            // GET sem session id deve retornar vazio
            val response = client.get("/api/cart")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("[]", response.bodyAsText())
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

    @Test
    fun testSecurityHeaders() =
        testApplication {
            environment { config = MapApplicationConfig("ktor.testing" to "true") }
            application { module() }

            val response = client.get("/")

            assertEquals("DENY", response.headers["X-Frame-Options"])
            assertEquals("nosniff", response.headers["X-Content-Type-Options"])
            assertNotNull(response.headers["Strict-Transport-Security"])
            assertTrue(response.headers["Content-Security-Policy"]?.contains("default-src 'self'") == true)
        }
}
