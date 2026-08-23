package com.itbenevides.genesys21.domain.usecase

import com.itbenevides.genesys21.domain.model.DomainMapping
import com.itbenevides.genesys21.domain.repository.DomainRepository

class GetDomainMappingsUseCase(private val repository: DomainRepository) {
    suspend operator fun invoke() = repository.getAllMappings()
}

class SaveDomainMappingUseCase(private val repository: DomainRepository) {
    suspend operator fun invoke(mapping: DomainMapping) = repository.saveMapping(mapping)
}

class DeleteDomainMappingUseCase(private val repository: DomainRepository) {
    suspend operator fun invoke(id: String) = repository.deleteMapping(id)
}
