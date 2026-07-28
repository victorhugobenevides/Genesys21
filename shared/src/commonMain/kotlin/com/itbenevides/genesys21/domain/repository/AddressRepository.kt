package com.itbenevides.genesys21.domain.repository

import com.itbenevides.genesys21.domain.model.Address

interface AddressRepository {
    suspend fun getAddresses(userId: String): List<Address>
    suspend fun saveAddress(address: Address): Result<String>
    suspend fun deleteAddress(addressId: String): Result<Unit>
}
