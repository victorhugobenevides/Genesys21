package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.repository.PageRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.utils.io.core.*
import kotlinx.browser.window

class KtorPageRepository(
    private val client: HttpClient,
    private var baseUrl: String = "http://localhost:8080"
) : PageRepository {

    init {
        // Ajuste dinâmico da base URL para WasmJs/Web
        val hostname = window.location.hostname
        if (hostname != "localhost" && hostname != "127.0.0.1") {
            // Em produção (AWS), assume que a API está no mesmo domínio/porta padrão
            baseUrl = "${window.location.protocol}//$hostname"
            // Se sua API rodar em uma subpasta ou porta diferente na AWS, ajuste aqui:
            // baseUrl = "https://$hostname/api" 
        }
    }

    override suspend fun getPages(token: String): List<Page> {
        val url = if (token.isBlank()) "$baseUrl/api/public/pages/first" else "$baseUrl/pages"
        return try {
            val response = client.get(url) {
                if (token.isNotBlank()) {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            }
            if (response.status.isSuccess()) {
                if (token.isBlank()) listOf(response.body<Page>())
                else response.body()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getPublicPage(id: String): Result<Page> {
        return try {
            val response = client.get("$baseUrl/api/public/pages/$id")
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Página não encontrada"))
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
                Result.failure(Exception("Domínio não vinculado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun savePage(page: Page, token: String, isEditing: Boolean): Result<Unit> {
        if (token.isBlank()) return Result.failure(Exception("Não autenticado"))
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
            else Result.failure(Exception("Erro ao salvar: ${response.status}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePage(id: String, token: String): Result<Unit> {
        if (token.isBlank()) return Result.failure(Exception("Não autenticado"))
        return try {
            val response = client.delete("$baseUrl/pages/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            if (response.status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception("Erro ao excluir"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadImage(bytes: ByteArray, fileName: String, token: String): Result<String> {
        if (token.isBlank()) return Result.failure(Exception("Não autenticado"))
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
