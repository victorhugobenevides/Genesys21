package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.model.Order
import com.itbenevides.genesys21.domain.model.OrderStatus
import com.itbenevides.genesys21.domain.repository.OrderRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class KtorOrderRepository(
    private val client: HttpClient,
    private val baseUrl: String
) : OrderRepository {

    override fun getOrders(token: String): Flow<List<Order>> = flow {
        try {
            val response = client.get("$baseUrl/orders") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            if (response.status.isSuccess()) {
                emit(response.body())
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun createOrder(order: Order): Result<Unit> {
        return try {
            val response = client.post("$baseUrl/api/public/orders") {
                contentType(ContentType.Application.Json)
                setBody(order)
            }
            if (response.status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception("Erro ao criar pedido: ${response.status}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateOrderStatus(token: String, orderId: String, status: OrderStatus): Result<Unit> {
        return try {
            val response = client.patch("$baseUrl/orders/$orderId/status") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(status)
            }
            if (response.status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception("Erro ao atualizar status: ${response.status}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
