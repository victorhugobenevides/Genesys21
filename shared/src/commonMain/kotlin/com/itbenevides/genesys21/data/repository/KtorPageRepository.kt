package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.repository.PageRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.utils.io.core.*

class KtorPageRepository(
    private val client: HttpClient,
    private val baseUrl: String = "http://localhost:8080"
) : PageRepository {

    override suspend fun getPages(token: String): List<Page> {
        return try {
            client.get("$baseUrl/pages") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }.body()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getPublicPage(id: String): Result<Page> {
        return try {
            val response = client.get("$baseUrl/api/public/pages/$id")
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Page not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPageByDomain(domain: String): Result<Page> {
        return try {
            val response = client.get("$baseUrl/api/public/domain/$domain")
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Domain not linked"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun savePage(page: Page, token: String, isEditing: Boolean): Result<Unit> {
        return try {
            val response = if (isEditing) {
                client.put("$baseUrl/pages") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(page)
                }
            } else {
                client.post("$baseUrl/pages") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(page)
                }
            }
            if (response.status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception("Failed to save: ${response.status}"))
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
            else Result.failure(Exception("Failed to delete: ${response.status}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadImage(bytes: ByteArray, fileName: String, token: String): Result<String> {
        return try {
            val response: String = client.submitFormWithBinaryData(
                url = "$baseUrl/upload",
                formData = formData {
                    append("image", bytes, Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                    })
                }
            ) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }.body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
