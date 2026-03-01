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
import kotlinx.serialization.Serializable

@Serializable
data class MercadoPagoCheckoutRequest(
    val order: Order
)

class KtorOrderRepository(private val httpClient: HttpClient, private val baseUrl: String) : OrderRepository {

    override suspend fun createOrder(order: Order): Result<String> = runCatching {
        httpClient.post("$baseUrl/public/orders") {
            contentType(ContentType.Application.Json)
            setBody(order)
        }.body<String>()
    }

    override suspend fun createMercadoPagoCheckout(order: Order, token: String): Result<String> = runCatching {
        httpClient.post("$baseUrl/api/checkout/mercadopago") {
            contentType(ContentType.Application.Json)
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
            setBody(MercadoPagoCheckoutRequest(order))
        }.body<String>()
    }
    
    override suspend fun getOrderById(orderId: String): Result<Order> = runCatching {
        httpClient.get("$baseUrl/public/orders/$orderId").body<Order>()
    }

    override suspend fun getCustomerOrders(sessionId: String): Result<List<Order>> = runCatching {
        httpClient.get("$baseUrl/public/orders/customer/$sessionId").body<List<Order>>()
    }

    override fun getOrders(token: String): Flow<List<Order>> = flow {
        emit(
            httpClient.get("$baseUrl/api/orders") {
                headers { append(HttpHeaders.Authorization, "Bearer $token") }
            }.body<List<Order>>()
        )
    }

    override suspend fun updateOrderStatus(token: String, orderId: String, status: OrderStatus): Result<Unit> = runCatching {
        httpClient.patch("$baseUrl/api/orders/$orderId/status") {
            contentType(ContentType.Application.Json)
            headers { append(HttpHeaders.Authorization, "Bearer $token") }
            setBody(status)
        }
    }
}
