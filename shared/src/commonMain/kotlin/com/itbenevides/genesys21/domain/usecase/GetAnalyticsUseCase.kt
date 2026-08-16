package com.itbenevides.genesys21.domain.usecase

import com.itbenevides.genesys21.domain.model.MerchantAnalytics
import com.itbenevides.genesys21.domain.repository.OrderRepository

class GetAnalyticsUseCase(private val repository: OrderRepository) {
    suspend operator fun invoke(token: String): Result<MerchantAnalytics> = repository.getAnalytics(token)
}
