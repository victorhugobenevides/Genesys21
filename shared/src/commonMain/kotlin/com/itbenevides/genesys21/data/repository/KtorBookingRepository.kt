package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.model.Appointment
import com.itbenevides.genesys21.domain.model.BookingService
import com.itbenevides.genesys21.domain.model.MerchantAvailability
import com.itbenevides.genesys21.domain.repository.BookingRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.datetime.LocalDate

class KtorBookingRepository(
    private val client: HttpClient,
    private val baseUrl: String,
) : BookingRepository {

    override suspend fun getServices(): List<BookingService> {
        return client.get("$baseUrl/api/booking/services").body()
    }

    override suspend fun getServiceById(id: String): BookingService? {
        val response = client.get("$baseUrl/api/booking/services/$id")
        if (response.status == HttpStatusCode.NotFound) return null
        return response.body()
    }

    override suspend fun saveService(service: BookingService, token: String) {
        val response = client.post("$baseUrl/api/booking/services") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(service)
        }
        if (!response.status.isSuccess()) {
            throw Exception("Erro ao salvar serviço: ${response.status}")
        }
    }

    override suspend fun deleteService(id: String, token: String) {
        val response = client.delete("$baseUrl/api/booking/services/$id") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        if (!response.status.isSuccess()) {
            throw Exception("Erro ao excluir serviço")
        }
    }

    override suspend fun getAvailability(storeId: String): MerchantAvailability? {
        return try {
            val response = client.get("$baseUrl/api/booking/availability/$storeId")
            if (response.status == HttpStatusCode.NotFound) {
                return MerchantAvailability(storeId = storeId)
            }
            response.body()
        } catch (e: Exception) {
            MerchantAvailability(storeId = storeId)
        }
    }

    override suspend fun saveAvailability(availability: MerchantAvailability, token: String) {
        val response = client.post("$baseUrl/api/booking/availability") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(availability)
        }
        if (!response.status.isSuccess()) {
            throw Exception("Erro ao salvar disponibilidade")
        }
    }

    override suspend fun getAppointments(serviceId: String?, storeId: String?, date: LocalDate): List<Appointment> {
        return try {
            val response = client.get("$baseUrl/api/booking/appointments") {
                if (serviceId != null) parameter("serviceId", serviceId)
                if (storeId != null) parameter("storeId", storeId)
                parameter("date", date.toString())
            }
            if (response.status.isSuccess()) {
                response.body()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getAppointmentsByPhone(phone: String): List<Appointment> {
        return try {
            val response = client.get("$baseUrl/api/booking/appointments") {
                parameter("phone", phone)
            }
            if (response.status.isSuccess()) {
                response.body()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun createAppointment(appointment: Appointment) {
        val response = client.post("$baseUrl/api/booking/appointments") {
            contentType(ContentType.Application.Json)
            setBody(appointment)
        }
        if (!response.status.isSuccess()) {
            val errorBody = try { response.bodyAsText() } catch (e: Exception) { response.status.toString() }
            println("KtorBookingRepository: ERRO SERVIDOR ($response.status): $errorBody")
            throw Exception("Erro ao criar agendamento: $errorBody")
        }
    }

    override suspend fun updateAppointment(appointment: Appointment, token: String) {
        val response = client.put("$baseUrl/api/booking/appointments/${appointment.id}") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(appointment)
        }
        if (!response.status.isSuccess()) {
            throw Exception("Erro ao atualizar agendamento")
        }
    }
}
