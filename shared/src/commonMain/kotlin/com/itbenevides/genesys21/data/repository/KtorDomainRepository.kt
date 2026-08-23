package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.model.DomainMapping
import com.itbenevides.genesys21.domain.repository.DomainRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class KtorDomainRepository(private val client: HttpClient) : DomainRepository {

    override suspend fun getAllMappings(): Result<List<DomainMapping>> = try {
        val mappings = client.get("/api/admin/system/domains").body<List<DomainMapping>>()
        Result.success(mappings)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getMappingByDomain(domain: String): Result<DomainMapping?> = try {
        // Not used by the app, but interface requires it
        Result.success(null)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun saveMapping(mapping: DomainMapping): Result<Unit> = try {
        val response = client.post("/api/admin/system/domains") {
            contentType(ContentType.Application.Json)
            setBody(mapping)
        }
        if (response.status.isSuccess()) Result.success(Unit)
        else Result.failure(Exception("Erro ao salvar mapeamento"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteMapping(id: String): Result<Unit> = try {
        val response = client.delete("/api/admin/system/domains/$id")
        if (response.status.isSuccess()) Result.success(Unit)
        else Result.failure(Exception("Erro ao excluir mapeamento"))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
