package com.itbenevides.genesys21.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class MerchantAnalytics(
    val dailyRevenue: List<DailyRevenue>,
    val topProducts: List<TopProduct>,
    val bookingSummary: BookingSummary,
    val totalOrders: Int,
    val averageTicket: Double
)

@Serializable
data class DailyRevenue(
    val date: String, // ISO format
    val amount: Double
)

@Serializable
data class TopProduct(
    val name: String,
    val quantity: Int,
    val revenue: Double
)

@Serializable
data class BookingSummary(
    val pending: Int,
    val confirmed: Int,
    val cancelled: Int,
    val upcoming: Int
)
