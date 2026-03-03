package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.model.Category
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.domain.repository.PageRepository
import com.itbenevides.genesys21.util.Analytics
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.utils.io.core.*
import kotlinx.datetime.Clock

/**
 * Implementação do repositório de páginas usando Ktor Client.
 * Otimizado para evitar cache indesejado do navegador e com logs de rede.
 */
class KtorPageRepository(
    private val client: HttpClient,
    private val baseUrl: String = "http://localhost:8080"
) : PageRepository {

    private fun getTimestamp() = Clock.System.now().toEpochMilliseconds()

    private fun logNetworkError(method: String, url: String, e: Exception) {
        println("NETWORK_ERROR [$method] $url: ${e.message}")
        Analytics.logException(e, "Network Error: $method $url", mapOf(
            "url" to url,
            "method" to method
        ))
    }

    override suspend fun getPages(token: String): List<Page> {
        val url = if (token.isBlank()) "$baseUrl/api/public/pages/first" else "$baseUrl/api/pages"
        return try {
            val response = client.get(url) {
                parameter("t", getTimestamp())
                header(HttpHeaders.CacheControl, "no-cache")
                if (token.isNotBlank()) {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            }
            if (response.status.isSuccess()) {
                if (token.isBlank()) listOf(response.body<Page>())
                else response.body()
            } else {
                println("NETWORK_WARNING: getPages returned ${response.status}")
                emptyList()
            }
        } catch (e: Exception) {
            logNetworkError("GET", url, e)
            emptyList()
        }
    }

    override suspend fun getPublicPage(id: String): Result<Page> {
        val url = "$baseUrl/api/public/pages/$id"
        return try {
            val response = client.get(url) {
                parameter("t", getTimestamp())
                header(HttpHeaders.CacheControl, "no-cache")
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                val errorMsg = "Página não encontrada: ${response.status}"
                println("NETWORK_WARNING: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            logNetworkError("GET", url, e)
            Result.failure(e)
        }
    }

    override suspend fun getPageByDomain(domain: String): Result<Page> {
        val url = "$baseUrl/api/public/domain/$domain"
        return try {
            val response = client.get(url) {
                parameter("t", getTimestamp())
                header(HttpHeaders.CacheControl, "no-cache")
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Domínio não vinculado: ${response.status}"))
            }
        } catch (e: Exception) {
            logNetworkError("GET", url, e)
            Result.failure(e)
        }
    }

    override suspend fun savePage(page: Page, token: String, isEditing: Boolean): Result<Unit> {
        if (token.isBlank()) return Result.failure(Exception("Não autenticado"))
        val url = "$baseUrl/api/pages"
        return try {
            val response = if (isEditing) {
                client.put(url) {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(page)
                }
            } else {
                client.post(url) {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(page)
                }
            }
            
            if (response.status.isSuccess()) {
                println("NETWORK_SUCCESS: Page ${page.id} saved.")
                Result.success(Unit)
            } else {
                val errorBody = try { response.body<String>() } catch(e: Exception) { "No body" }
                val errorMsg = "Erro ao salvar página: ${response.status} - $errorBody"
                println("NETWORK_ERROR: $errorMsg")
                Analytics.logEvent("network_save_page_error", mapOf(
                    "status" to response.status.value,
                    "page_id" to page.id
                ))
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            logNetworkError(if (isEditing) "PUT" else "POST", url, e)
            Result.failure(e)
        }
    }

    override suspend fun deletePage(id: String, token: String): Result<Unit> {
        if (token.isBlank()) return Result.failure(Exception("Não autenticado"))
        val url = "$baseUrl/api/pages/$id"
        return try {
            val response = client.delete(url) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            if (response.status.isSuccess()) Result.success(Unit)
            else {
                println("NETWORK_ERROR: deletePage returned ${response.status}")
                Result.failure(Exception("Erro ao excluir: ${response.status}"))
            }
        } catch (e: Exception) {
            logNetworkError("DELETE", url, e)
            Result.failure(e)
        }
    }

    override suspend fun uploadImage(bytes: ByteArray, fileName: String, token: String): Result<String> {
        if (token.isBlank()) return Result.failure(Exception("Não autenticado"))
        val url = "$baseUrl/api/upload"
        return try {
            val response: String = client.submitFormWithBinaryData(
                url = url,
                formData = formData {
                    append("image", bytes, Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                    })
                }
            ) {
                header(HttpHeaders.Authorization, "Bearer $token")
                onUpload { bytesSentTotal, contentLength ->
                    if (contentLength != null && contentLength > 0) {
                        // println("UPLOAD_PROGRESS: ${(bytesSentTotal * 100f / contentLength)}%")
                    }
                }
            }.body()
            Result.success(response)
        } catch (e: Exception) {
            logNetworkError("POST_UPLOAD", url, e)
            Result.failure(e)
        }
    }

    override suspend fun getAllProducts(token: String): Result<List<Product>> {
        if (token.isBlank()) return Result.failure(Exception("Não autenticado"))
        val url = "$baseUrl/api/products"
        return try {
            val response = client.get(url) {
                parameter("t", getTimestamp())
                header(HttpHeaders.CacheControl, "no-cache")
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Falha ao buscar produtos: ${response.status}"))
            }
        } catch (e: Exception) {
            logNetworkError("GET", url, e)
            Result.failure(e)
        }
    }

    override suspend fun saveProduct(product: Product, token: String): Result<Unit> {
        if (token.isBlank()) return Result.failure(Exception("Não autenticado"))
        val url = "$baseUrl/api/products"
        return try {
            val response = client.post(url) {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(product)
            }
            if (response.status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception("Erro ao salvar produto: ${response.status}"))
        } catch (e: Exception) {
            logNetworkError("POST_PRODUCT", url, e)
            Result.failure(e)
        }
    }

    override suspend fun deleteProduct(id: String, token: String): Result<Unit> {
        if (token.isBlank()) return Result.failure(Exception("Não autenticado"))
        val url = "$baseUrl/api/products/$id"
        return try {
            val response = client.delete(url) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            if (response.status.isSuccess()) Result.success(Unit)
            else {
                Result.failure(Exception("Erro ao excluir produto: ${response.status}"))
            }
        } catch (e: Exception) {
            logNetworkError("DELETE_PRODUCT", url, e)
            Result.failure(e)
        }
    }

    override suspend fun getCategories(token: String): Result<List<Category>> {
        if (token.isBlank()) return Result.failure(Exception("Não autenticado"))
        val url = "$baseUrl/api/categories"
        return try {
            val response = client.get(url) {
                parameter("t", getTimestamp())
                header(HttpHeaders.CacheControl, "no-cache")
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Falha ao buscar categorias: ${response.status}"))
            }
        } catch (e: Exception) {
            logNetworkError("GET", url, e)
            Result.failure(e)
        }
    }

    override suspend fun saveCategory(category: Category, token: String): Result<Unit> {
        if (token.isBlank()) return Result.failure(Exception("Não autenticado"))
        val url = "$baseUrl/api/categories"
        return try {
            val response = if (category.id != null) {
                client.put(url) {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(category)
                }
            } else {
                client.post(url) {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(category)
                }
            }
            if (response.status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception("Erro ao salvar categoria: ${response.status}"))
        } catch (e: Exception) {
            logNetworkError("SAVE_CAT", url, e)
            Result.failure(e)
        }
    }

    override suspend fun deleteCategory(id: Int, token: String): Result<Unit> {
        if (token.isBlank()) return Result.failure(Exception("Não autenticado"))
        val url = "$baseUrl/api/categories/$id"
        return try {
            val response = client.delete(url) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            if (response.status.isSuccess()) Result.success(Unit)
            else {
                Result.failure(Exception("Erro ao excluir categoria: ${response.status}"))
            }
        } catch (e: Exception) {
            logNetworkError("DELETE_CAT", url, e)
            Result.failure(e)
        }
    }
}
