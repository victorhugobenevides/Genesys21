package com.itbenevides.genesys21.routes

import com.itbenevides.genesys21.domain.model.Appointment
import com.itbenevides.genesys21.domain.model.BookingService
import com.itbenevides.genesys21.domain.model.MerchantAvailability
import com.itbenevides.genesys21.domain.repository.BookingRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDate

fun Route.bookingRoutes(repository: BookingRepository) {
    route("/booking") {
        get("/services") {
            val services = repository.getServices()
            call.respond(services)
        }

        get("/services/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val service = repository.getServiceById(id)
            if (service != null) call.respond(service) else call.respond(HttpStatusCode.NotFound)
        }

        post("/services") {
            val service = call.receive<BookingService>()
            repository.saveService(service)
            call.respond(HttpStatusCode.Created)
        }

        delete("/services/{id}") {
            val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
            repository.deleteService(id)
            call.respond(HttpStatusCode.OK)
        }

        get("/availability/{storeId}") {
            val storeId = call.parameters["storeId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val availability = repository.getAvailability(storeId)
            if (availability != null) call.respond(availability) else call.respond(HttpStatusCode.NotFound)
        }

        post("/availability") {
            val availability = call.receive<MerchantAvailability>()
            repository.saveAvailability(availability)
            call.respond(HttpStatusCode.OK)
        }

        get("/appointments") {
            try {
                val phone = call.request.queryParameters["phone"]
                if (phone != null) {
                    val appointments = repository.getAppointmentsByPhone(phone)
                    return@get call.respond(appointments)
                }

                val serviceId = call.request.queryParameters["serviceId"]
                val storeId = call.request.queryParameters["storeId"]
                val dateStr = call.request.queryParameters["date"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val date = LocalDate.parse(dateStr)
                val appointments = repository.getAppointments(serviceId, storeId, date)
                call.respond(appointments)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Erro ao buscar agendamentos: ${e.message}")
            }
        }

        post("/appointments") {
            try {
                println("BookingRoutes: Recebendo POST /appointments...")
                val appointment = call.receive<Appointment>()
                println("BookingRoutes: Recebido agendamento para ${appointment.customerName} - Serviço: ${appointment.serviceId}")
                repository.createAppointment(appointment)
                call.respond(HttpStatusCode.Created)
            } catch (e: Exception) {
                println("BookingRoutes: ERRO ao criar agendamento (Tipo: ${e::class.simpleName}): ${e.message}")
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Erro ao criar agendamento: ${e.message}")
            }
        }

        put("/appointments/{id}") {
            try {
                val appointment = call.receive<Appointment>()
                repository.updateAppointment(appointment)
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Erro ao atualizar agendamento: ${e.message}")
            }
        }
    }
}
