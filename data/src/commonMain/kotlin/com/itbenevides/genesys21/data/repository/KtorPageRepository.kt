package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.repository.PageRepository
import com.itbenevides.genesys21.getWebBaseUrl
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class KtorPageRepository(private val httpClient: HttpClient) : PageRepository {
    // CORREÇÃO: Usa a URL dinâmica do ambiente (WasmJs ou Android) com o prefixo /api
    private val apiBaseUrl get() = "${getWebBaseUrl()}/api"

    override suspend fun getPages(token: String): List<Page> {
        return try {
            httpClient.get("$apiBaseUrl/pages") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }.body()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getPublicPage(id: String): Result<Page> {
        return try {
            // Rotas públicas já estão sob /api/public no servidor
            val page = httpClient.get("$apiBaseUrl/public/pages/$id").body<Page>()
            Result.success(page)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun savePage(page: Page, token: String, isEditing: Boolean): Result<Unit> {
        return try {
            val response = if (isEditing) {
                httpClient.put("$apiBaseUrl/pages") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(page)
                }
            } else {
                httpClient.post("$apiBaseUrl/pages") {
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
            val response = httpClient.delete("$apiBaseUrl/pages/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            if (response.status.isSuccess()) Result.success(Unit) 
            else Result.failure(Exception("Error deleting"))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // ADICIONADO: Implementação faltante para busca por domínio
    override suspend fun getPageByDomain(domain: String): Result<Page> {
        return try {
            val page = httpClient.get("$apiBaseUrl/public/domain/$domain").body<Page>()
            Result.success(page)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
