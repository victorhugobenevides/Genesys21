package com.itbenevides.genesys21.domain.usecase

import com.itbenevides.genesys21.domain.repository.OrderRepository

class GetAuditLogsUseCase(private val repository: OrderRepository) {
    suspend operator fun invoke(token: String) = repository.getAuditLogs(token)
}
