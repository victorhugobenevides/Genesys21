package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.model.Page
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KtorPageRepositoryTest {

    private fun createMockClient(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): HttpClient {
        return HttpClient(MockEngine) {
            install(ContentNegotiation) {
                json()
            }
            engine {
                addHandler { request ->
                    handler(this, request)
                }
            }
        }
    }

    @Test
    fun getPages_should_return_list_when_server_returns_success() = runTest {
        val mockPages = listOf(Page("1", "Página 1"), Page("2", "Página 2"))
        val client = createMockClient { _ ->
            respond(
                content = Json.encodeToString(mockPages),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        
        val repository = KtorPageRepository(client, "http://localhost")
        val result = repository.getPages()
        
        assertEquals(2, result.size)
        assertEquals("Página 1", result[0].title)
    }

    @Test
    fun savePage_should_send_correct_data_and_return_success() = runTest {
        val pageToSave = Page("new", "Nova")
        val client = createMockClient { request ->
            assertEquals(HttpMethod.Post, request.method)
            assertTrue(request.headers[HttpHeaders.Authorization]?.contains("Bearer token") == true)
            
            respond(
                content = Json.encodeToString(pageToSave),
                status = HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        
        val repository = KtorPageRepository(client, "http://localhost")
        val result = repository.savePage(pageToSave, "token")
        
        assertTrue(result.isSuccess)
        assertEquals("Nova", result.getOrNull()?.title)
    }

    @Test
    fun getPages_should_return_empty_list_on_server_error() = runTest {
        val client = createMockClient { _ ->
            respond("", status = HttpStatusCode.InternalServerError)
        }
        
        val repository = KtorPageRepository(client, "http://localhost")
        val result = repository.getPages()
        
        assertTrue(result.isEmpty())
    }
}
