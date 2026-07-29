package com.itbenevides.genesys21.routes

import com.itbenevides.genesys21.domain.model.Appointment
import com.itbenevides.genesys21.domain.model.BookingService
import com.itbenevides.genesys21.domain.model.MerchantAvailability
import com.itbenevides.genesys21.domain.repository.BookingRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDate

fun Route.bookingRoutes(repository: BookingRepository) {
    route("/booking") {
        // Rotas Públicas
        get("/services") {
            val services = repository.getServices().map { it.copy(meetingLink = null) }
            call.respond(services)
        }

        get("/services/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val service = repository.getServiceById(id)?.copy(meetingLink = null)
            if (service != null) call.respond(service) else call.respond(HttpStatusCode.NotFound)
        }

        get("/availability/{storeId}") {
            val storeId = call.parameters["storeId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val availability = repository.getAvailability(storeId)
            if (availability != null) {
                call.respond(availability)
            } else {
                call.respond(HttpStatusCode.NotFound, "Disponibilidade não configurada")
            }
        }

        get("/appointments/all") {
            val storeId = call.request.queryParameters["storeId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val appointments = repository.getAllAppointments(storeId)
            call.respond(appointments)
        }

        get("/appointments/upcoming") {
            val storeId = call.request.queryParameters["storeId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val appointments = repository.getUpcomingAppointments(storeId)
            call.respond(appointments)
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
                call.respond(HttpStatusCode.InternalServerError, "Erro ao buscar agendamentos")
            }
        }

        // POST: Criar agendamento (Público, usado no checkout)
        post("/appointments") {
            try {
                val appointment = call.receive<Appointment>()
                repository.createAppointment(appointment)
                call.respond(HttpStatusCode.Created)
            } catch (e: Exception) {
                println("ERRO AO CRIAR AGENDAMENTO: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, "Erro ao criar agendamento")
            }
        }

        // Rotas Protegidas (Mercador e Cliente Autenticado)
        authenticate("firebase") {
            post("/services") {
                val principal = call.principal<UserIdPrincipal>()!!
                val service = call.receive<BookingService>()
                repository.saveService(service, principal.name)
                call.respond(HttpStatusCode.Created)
            }

            delete("/services/{id}") {
                val principal = call.principal<UserIdPrincipal>()!!
                val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                repository.deleteService(id, principal.name)
                call.respond(HttpStatusCode.OK)
            }

            post("/availability") {
                val principal = call.principal<UserIdPrincipal>()!!
                val availability = call.receive<MerchantAvailability>()
                repository.saveAvailability(availability, principal.name)
                call.respond(HttpStatusCode.OK)
            }

            put("/appointments/{id}") {
                try {
                    val principal = call.principal<UserIdPrincipal>()!!
                    val appointment = call.receive<Appointment>()
                    repository.updateAppointment(appointment, principal.name)
                    call.respond(HttpStatusCode.OK)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Erro ao atualizar agendamento")
                }
            }
        }
    }
}
