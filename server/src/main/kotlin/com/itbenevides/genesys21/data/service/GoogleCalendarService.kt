package com.itbenevides.genesys21.data.service

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.*
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import com.itbenevides.genesys21.domain.model.Appointment
import kotlinx.datetime.Instant
import java.io.File
import java.io.FileInputStream
import java.util.*

class GoogleCalendarService {
    private val jsonFactory = GsonFactory.getDefaultInstance()
    private val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
    private val credentialsFile = "firebase-adminsdk.json"

    private fun getService(): Calendar? {
        val file = File(credentialsFile)
        if (!file.exists()) return null

        val credentials = GoogleCredentials.fromStream(FileInputStream(file))
            .createScoped(listOf(CalendarScopes.CALENDAR_EVENTS))

        return Calendar.Builder(httpTransport, jsonFactory, HttpCredentialsAdapter(credentials))
            .setApplicationName("Genesys21")
            .build()
    }

    suspend fun createMeetLink(appointment: Appointment, serviceName: String): String? {
        val calendarService = getService() ?: return null

        val event = Event().apply {
            summary = "Agendamento: $serviceName"
            description = "Reserva realizada via Genesys21\nCliente: ${appointment.customerName}\nTelefone: ${appointment.customerPhone}"

            start = EventDateTime().setDateTime(DateTime(appointment.startTime.toEpochMilliseconds()))
            end = EventDateTime().setDateTime(DateTime(appointment.endTime.toEpochMilliseconds()))

            // Configuração para gerar link do Meet
            conferenceData = ConferenceData().apply {
                createRequest = CreateConferenceRequest().apply {
                    requestId = UUID.randomUUID().toString()
                    conferenceSolutionKey = ConferenceSolutionKey().setType("hangoutsMeet")
                }
            }
        }

        return try {
            val createdEvent = calendarService.events().insert("primary", event)
                .setConferenceDataVersion(1)
                .execute()

            // Extrai o link do Meet da resposta
            createdEvent.conferenceData?.entryPoints?.firstOrNull { it.entryPointType == "video" }?.uri
        } catch (e: Exception) {
            println("GOOGLE CALENDAR ERROR: ${e.message}")
            null
        }
    }
}
