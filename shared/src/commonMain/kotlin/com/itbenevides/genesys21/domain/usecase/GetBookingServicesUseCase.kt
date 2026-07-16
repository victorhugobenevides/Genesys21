package com.itbenevides.genesys21.domain.usecase

import com.itbenevides.genesys21.domain.model.BookingService
import com.itbenevides.genesys21.domain.repository.BookingRepository

class GetBookingServicesUseCase(
    private val repository: BookingRepository
) {
    suspend operator fun invoke(): List<BookingService> {
        return repository.getServices()
    }
}
