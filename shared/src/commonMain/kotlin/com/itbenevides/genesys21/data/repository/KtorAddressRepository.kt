package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.model.Address
import com.itbenevides.genesys21.domain.repository.AddressRepository
import com.itbenevides.genesys21.domain.repository.AuthRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class KtorAddressRepository(
    private val client: HttpClient,
    private val baseUrl: String,
    private val authRepository: AuthRepository
) : AddressRepository {

    private suspend fun getHeaders() = buildMap {
        authRepository.getCurrentUserToken()?.let {
            put(HttpHeaders.Authorization, "Bearer $it")
        }
    }

    override suspend fun getAddresses(userId: String): List<Address> = try {
        client.get("$baseUrl/api/addresses") {
            getHeaders().forEach { (k, v) -> header(k, v) }
        }.body()
    } catch (e: Exception) {
        emptyList()
    }

    override suspend fun saveAddress(address: Address): Result<String> = try {
        val response = client.post("$baseUrl/api/addresses") {
            getHeaders().forEach { (k, v) -> header(k, v) }
            contentType(ContentType.Application.Json)
            setBody(address)
        }
        if (response.status.isSuccess()) {
            Result.success(response.body())
        } else {
            Result.failure(Exception("Erro ao salvar endereço"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteAddress(addressId: String): Result<Unit> = try {
        val response = client.delete("$baseUrl/api/addresses/$addressId") {
            getHeaders().forEach { (k, v) -> header(k, v) }
        }
        if (response.status.isSuccess()) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Erro ao excluir endereço"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
