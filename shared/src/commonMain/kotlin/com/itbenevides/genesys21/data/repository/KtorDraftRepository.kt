package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.repository.DraftRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class KtorDraftRepository(
    private val client: HttpClient,
    private val baseUrl: String
) : DraftRepository {

    override suspend fun saveDraft(page: Page, token: String): Result<Unit> = try {
        val response = client.post("$baseUrl/api/drafts") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(page)
        }
        if (response.status.isSuccess()) Result.success(Unit)
        else Result.failure(Exception("Erro ao salvar rascunho no servidor"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getDraft(pageId: String, token: String): Result<Page?> = try {
        val response = client.get("$baseUrl/api/drafts/$pageId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        if (response.status == HttpStatusCode.NotFound) Result.success(null)
        else if (response.status.isSuccess()) Result.success(response.body<Page>())
        else Result.failure(Exception("Erro ao buscar rascunho no servidor"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteDraft(pageId: String, token: String): Result<Unit> = try {
        val response = client.delete("$baseUrl/api/drafts/$pageId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        if (response.status.isSuccess()) Result.success(Unit)
        else Result.failure(Exception("Erro ao excluir rascunho no servidor"))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
