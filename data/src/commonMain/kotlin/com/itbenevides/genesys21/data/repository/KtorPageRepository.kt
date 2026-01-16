package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.repository.PageRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class KtorPageRepository(private val httpClient: HttpClient) : PageRepository {
    private val baseUrl = "http://localhost:8080"

    override suspend fun getPages(token: String): List<Page> {
        return try {
            httpClient.get("$baseUrl/pages") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }.body()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getPublicPage(id: String): Result<Page> {
        return try {
            val page = httpClient.get("$baseUrl/api/public/pages/$id").body<Page>()
            Result.success(page)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun savePage(page: Page, token: String, isEditing: Boolean): Result<Unit> {
        return try {
            val response = if (isEditing) {
                httpClient.put("$baseUrl/pages") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(page)
                }
            } else {
                httpClient.post("$baseUrl/pages") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(page)
                }
            }
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to save page: ${response.status}"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun deletePage(id: String, token: String): Result<Unit> {
        return try {
            httpClient.delete("$baseUrl/pages/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
