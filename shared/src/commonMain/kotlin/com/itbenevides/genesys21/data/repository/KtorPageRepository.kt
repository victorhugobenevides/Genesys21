package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.repository.PageRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class KtorPageRepository(
    private val client: HttpClient,
    private val baseUrl: String
) : PageRepository {

    override suspend fun getPages(): List<Page> {
        return try {
            client.get("$baseUrl/pages").body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun savePage(page: Page, token: String): Result<Page> {
        return try {
            val response = client.post("$baseUrl/pages") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(page)
            }
            if (response.status.isSuccess()) Result.success(response.body())
            else Result.failure(Exception("Error: ${response.status}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePage(page: Page, token: String): Result<Page> {
        return try {
            val response = client.put("$baseUrl/pages") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(page)
            }
            if (response.status.isSuccess()) Result.success(response.body())
            else Result.failure(Exception("Error: ${response.status}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePage(id: String, token: String): Result<Unit> {
        return try {
            val response = client.delete("$baseUrl/pages/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            if (response.status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception("Error: ${response.status}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
