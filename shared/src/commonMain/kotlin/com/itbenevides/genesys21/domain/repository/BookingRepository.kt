package com.itbenevides.genesys21.domain.repository

import com.itbenevides.genesys21.domain.model.BookingService
import com.itbenevides.genesys21.domain.model.MerchantAvailability
import com.itbenevides.genesys21.domain.model.Appointment
import kotlinx.datetime.LocalDate

interface BookingRepository {
    suspend fun getServices(): List<BookingService>
    suspend fun getServiceById(id: String): BookingService?
    suspend fun saveService(service: BookingService)
    suspend fun deleteService(id: String)

    suspend fun getAvailability(storeId: String): MerchantAvailability?
    suspend fun saveAvailability(availability: MerchantAvailability)

    suspend fun getAppointments(serviceId: String?, storeId: String?, date: LocalDate): List<Appointment>
    suspend fun getAppointmentsByPhone(phone: String): List<Appointment>
    suspend fun createAppointment(appointment: Appointment)
    suspend fun updateAppointment(appointment: Appointment)
}
