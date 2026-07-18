package com.itbenevides.genesys21.domain.usecase

import com.itbenevides.genesys21.domain.model.Appointment
import com.itbenevides.genesys21.domain.model.BookingService
import com.itbenevides.genesys21.domain.model.MerchantAvailability
import com.itbenevides.genesys21.domain.repository.BookingRepository
import kotlinx.datetime.LocalDate

class SaveBookingServiceUseCase(private val repository: BookingRepository) {
    suspend operator fun invoke(service: BookingService): Result<Unit> = runCatching {
        repository.saveService(service)
    }
}

class DeleteBookingServiceUseCase(private val repository: BookingRepository) {
    suspend operator fun invoke(id: String): Result<Unit> = runCatching {
        repository.deleteService(id)
    }
}

class GetAvailabilityUseCase(private val repository: BookingRepository) {
    suspend operator fun invoke(storeId: String): MerchantAvailability? = repository.getAvailability(storeId)
}

class SaveAvailabilityUseCase(private val repository: BookingRepository) {
    suspend operator fun invoke(availability: MerchantAvailability): Result<Unit> = runCatching {
        repository.saveAvailability(availability)
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
    suspend operator fun invoke(appointment: Appointment): Result<Unit> = runCatching {
        repository.updateAppointment(appointment)
    }
}
