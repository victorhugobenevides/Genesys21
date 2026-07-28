package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.model.Store
import com.itbenevides.genesys21.domain.repository.AuthRepository
import com.itbenevides.genesys21.domain.repository.StoreRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class KtorStoreRepository(
    private val client: HttpClient,
    private val baseUrl: String,
    private val authRepository: AuthRepository
) : StoreRepository {

    private suspend fun getHeaders() = buildMap {
        authRepository.getCurrentUserToken()?.let {
            put(HttpHeaders.Authorization, "Bearer $it")
        }
    }

    override suspend fun getStore(id: String): Result<Store> = try {
        val response = client.get("$baseUrl/api/stores/$id")
        if (response.status.isSuccess()) {
            Result.success(response.body())
        } else {
            Result.failure(Exception("Loja não encontrada"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun saveStore(store: Store, token: String): Result<Unit> = try {
        val response = client.post("$baseUrl/api/stores") {
            getHeaders().forEach { (k, v) -> header(k, v) }
            contentType(ContentType.Application.Json)
            setBody(store)
        }
        if (response.status.isSuccess()) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Erro ao salvar loja"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
