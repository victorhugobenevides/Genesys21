package com.itbenevides.genesys21.routes

import com.itbenevides.genesys21.data.repository.SqliteOrderRepository
import com.itbenevides.genesys21.domain.model.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.*
import kotlinx.serialization.json.Json
import kotlin.test.*

class AnalyticsRoutesTest {

    private val mockOrderRepo = mockk<SqliteOrderRepository>()

    @Test
    fun `get analytics summary should return analytics data`() = testApplication {
        val mockAnalytics = MerchantAnalytics(
            dailyRevenue = listOf(DailyRevenue("2025-01-01", 100.0)),
            topProducts = listOf(TopProduct("Product 1", 5, 100.0)),
            bookingSummary = BookingSummary(0, 1, 0, 1),
            totalOrders = 1,
            averageTicket = 100.0
        )

        coEvery { mockOrderRepo.getAnalytics("test-user") } returns Result.success(mockAnalytics)

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            install(Authentication) {
                bearer("firebase") {
                    authenticate { UserIdPrincipal("test-user") }
                }
            }
            routing {
                route("/api") {
                    analyticsRoutes(mockOrderRepo)
                }
            }
        }

        val response = client.get("/api/admin/analytics/summary") {
            header(HttpHeaders.Authorization, "Bearer valid-token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.decodeFromString<MerchantAnalytics>(response.bodyAsText())
        assertEquals(1, body.totalOrders)
        assertEquals(100.0, body.averageTicket)
    }
}
