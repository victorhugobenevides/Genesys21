package com.itbenevides.genesys21.domain.usecase

import com.itbenevides.genesys21.domain.model.Appointment
import com.itbenevides.genesys21.domain.model.BookingService
import com.itbenevides.genesys21.domain.model.MerchantAvailability
import com.itbenevides.genesys21.domain.repository.BookingRepository
import kotlinx.datetime.LocalDate

class SaveBookingServiceUseCase(private val repository: BookingRepository) {
    suspend operator fun invoke(service: BookingService, token: String): Result<Unit> = runCatching {
        repository.saveService(service, token)
    }
}

class DeleteBookingServiceUseCase(private val repository: BookingRepository) {
    suspend operator fun invoke(id: String, token: String): Result<Unit> = runCatching {
        repository.deleteService(id, token)
    }
}

class GetAvailabilityUseCase(private val repository: BookingRepository) {
    suspend operator fun invoke(storeId: String): MerchantAvailability? = repository.getAvailability(storeId)
}

class SaveAvailabilityUseCase(private val repository: BookingRepository) {
    suspend operator fun invoke(availability: MerchantAvailability, token: String): Result<Unit> = runCatching {
        repository.saveAvailability(availability, token)
    }
}

class GetAppointmentsUseCase(private val repository: BookingRepository) {
    suspend operator fun invoke(serviceId: String?, storeId: String?, date: LocalDate): List<Appointment> =
        repository.getAppointments(serviceId, storeId, date)

    suspend fun byPhone(phone: String): List<Appointment> = repository.getAppointmentsByPhone(phone)
}

class CreateAppointmentUseCase(private val repository: BookingRepository) {
    suspend operator fun invoke(appointment: Appointment): Result<Unit> = runCatching {
        repository.createAppointment(appointment)
    }
}

class UpdateAppointmentUseCase(private val repository: BookingRepository) {
    suspend operator fun invoke(appointment: Appointment, token: String): Result<Unit> = runCatching {
        repository.updateAppointment(appointment, token)
    }
}
