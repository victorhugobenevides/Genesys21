package com.itbenevides.genesys21.presentation

import com.itbenevides.genesys21.domain.model.DomainMapping
import com.itbenevides.genesys21.domain.repository.DomainRepository

class FakeDomainRepository : DomainRepository {
    private val mappings = mutableListOf<DomainMapping>()

    override suspend fun getAllMappings(): Result<List<DomainMapping>> = Result.success(mappings)

    override suspend fun getMappingByDomain(domain: String): Result<DomainMapping?> =
        Result.success(mappings.find { it.domain == domain })

    override suspend fun saveMapping(mapping: DomainMapping): Result<Unit> {
        mappings.add(mapping)
        return Result.success(Unit)
    }

    override suspend fun deleteMapping(id: String): Result<Unit> {
        mappings.removeAll { it.id == id }
        return Result.success(Unit)
    }
}
