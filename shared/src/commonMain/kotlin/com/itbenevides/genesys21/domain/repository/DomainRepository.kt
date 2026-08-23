package com.itbenevides.genesys21.domain.repository

import com.itbenevides.genesys21.domain.model.DomainMapping

interface DomainRepository {
    suspend fun getAllMappings(): Result<List<DomainMapping>>
    suspend fun getMappingByDomain(domain: String): Result<DomainMapping?>
    suspend fun saveMapping(mapping: DomainMapping): Result<Unit>
    suspend fun deleteMapping(id: String): Result<Unit>
}
