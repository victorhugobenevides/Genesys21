package com.itbenevides.genesys21.domain.usecase

import com.itbenevides.genesys21.domain.repository.ShippingRepository

class CalculateShippingUseCase(private val repository: ShippingRepository) {
    suspend operator fun invoke(storeId: String, zipCode: String) =
        repository.calculateShipping(storeId, zipCode)
}
