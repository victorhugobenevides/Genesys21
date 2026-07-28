package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.model.ShippingOption
import com.itbenevides.genesys21.domain.repository.ShippingRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class KtorShippingRepository(
    private val client: HttpClient,
    private val baseUrl: String
) : ShippingRepository {
    override suspend fun calculateShipping(storeId: String, zipCode: String): Result<List<ShippingOption>> = try {
        val response = client.get("$baseUrl/api/shipping/calculate") {
            parameter("storeId", storeId)
            parameter("zipCode", zipCode)
        }
        if (response.status.isSuccess()) {
            Result.success(response.body())
        } else {
            Result.failure(Exception("Falha ao calcular frete"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
