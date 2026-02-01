package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.model.Category
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.domain.repository.PageRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.utils.io.core.*
import kotlinx.datetime.Clock

/**
 * Implementação do repositório de páginas usando Ktor Client.
 * Otimizado para evitar cache indesejado do navegador.
 */
class KtorPageRepository(
    private val client: HttpClient,
    private val baseUrl: String = "http://localhost:8080"
) : PageRepository {

    private fun getTimestamp() = Clock.System.now().toEpochMilliseconds()

    override suspend fun getPages(token: String): List<Page> {
        val url = if (token.isBlank()) "$baseUrl/api/public/pages/first" else "$baseUrl/api/pages"
        return try {
            val response = client.get(url) {
                parameter("t", getTimestamp()) // Cache-busting
                header(HttpHeaders.CacheControl, "no-cache")
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
            val response = client.get("$baseUrl/api/public/pages/$id") {
                parameter("t", getTimestamp()) // Cache-busting
                header(HttpHeaders.CacheControl, "no-cache")
            }
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
            val response = client.get("$baseUrl/api/public/domain/$domain") {
                parameter("t", getTimestamp())
                header(HttpHeaders.CacheControl, "no-cache")
            }
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
                client.put("$baseUrl/api/pages") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(page)
                }
            } else {
                client.post("$baseUrl/api/pages") {
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
            val response = client.delete("$baseUrl/api/pages/$id") {
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
                url = "$baseUrl/api/upload",
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

    override suspend fun getAllProducts(token: String): Result<List<Product>> {
        if (token.isBlank()) return Result.failure(Exception("Não autenticado"))
        return try {
            val response = client.get("$baseUrl/api/products") {
                parameter("t", getTimestamp())
                header(HttpHeaders.CacheControl, "no-cache")
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Falha ao buscar produtos"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCategories(token: String): Result<List<Category>> {
        if (token.isBlank()) return Result.failure(Exception("Não autenticado"))
        return try {
            val response = client.get("$baseUrl/api/categories") {
                parameter("t", getTimestamp())
                header(HttpHeaders.CacheControl, "no-cache")
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Falha ao buscar categorias"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveCategory(category: Category, token: String): Result<Unit> {
        if (token.isBlank()) return Result.failure(Exception("Não autenticado"))
        return try {
            val response = if (category.id != null) {
                client.put("$baseUrl/api/categories") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(category)
                }
            } else {
                client.post("$baseUrl/api/categories") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(category)
                }
            }
            if (response.status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception("Erro ao salvar categoria"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCategory(id: Int, token: String): Result<Unit> {
        if (token.isBlank()) return Result.failure(Exception("Não autenticado"))
        return try {
            val response = client.delete("$baseUrl/api/categories/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            if (response.status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception("Erro ao excluir categoria"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
