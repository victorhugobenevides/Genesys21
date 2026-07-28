package com.itbenevides.genesys21.domain.repository

import com.itbenevides.genesys21.domain.model.ShippingOption

interface ShippingRepository {
    suspend fun calculateShipping(storeId: String, zipCode: String): Result<List<ShippingOption>>
}
