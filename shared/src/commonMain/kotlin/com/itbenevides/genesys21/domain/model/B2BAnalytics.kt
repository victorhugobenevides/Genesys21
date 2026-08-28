package com.itbenevides.genesys21.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class B2BAnalytics(
    val totalMerchants: Int,
    val platformGMV: Double,
    val globalAverageTicket: Double,
    val topMerchants: List<MerchantPerformance>,
    val globalDailyRevenue: List<DailyRevenue>
)

@Serializable
data class MerchantPerformance(
    val merchantName: String,
    val totalRevenue: Double,
    val orderCount: Int
)
