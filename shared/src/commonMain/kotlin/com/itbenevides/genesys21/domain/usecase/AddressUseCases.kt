package com.itbenevides.genesys21.domain.usecase

import com.itbenevides.genesys21.domain.model.Address
import com.itbenevides.genesys21.domain.repository.AddressRepository

class GetAddressesUseCase(private val repository: AddressRepository) {
    suspend operator fun invoke(userId: String) = repository.getAddresses(userId)
}

class SaveAddressUseCase(private val repository: AddressRepository) {
    suspend operator fun invoke(address: Address) = repository.saveAddress(address)
}

class DeleteAddressUseCase(private val repository: AddressRepository) {
    suspend operator fun invoke(addressId: String) = repository.deleteAddress(addressId)
}
