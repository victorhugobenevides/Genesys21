package com.itbenevides.genesys21.data.service

import com.itbenevides.genesys21.domain.model.Appointment
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.*

class GoogleCalendarService(
    private val client: HttpClient,
    private val json: Json
) {
    suspend fun createEvent(accessToken: String, appointment: Appointment): Result<String> {
        return try {
            val startTime = appointment.startTime.toLocalDateTime(TimeZone.currentSystemDefault()).toString()
            val endTime = appointment.endTime.toLocalDateTime(TimeZone.currentSystemDefault()).toString()

            val response = client.post("https://www.googleapis.com/calendar/v3/calendars/primary/events") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                contentType(ContentType.Application.Json)
                setBody(
                    buildJsonObject {
                        put("summary", "Agendamento: ${appointment.customerName}")
                        val notesSummary = appointment.notes.filter { !it.isPrivate }.joinToString("\n") { it.content }
                        put("description", "Serviço agendado via Genesys21. Notas: ${notesSummary.ifBlank { "Nenhuma" }}")
                        put("start", buildJsonObject {
                            put("dateTime", startTime)
                            put("timeZone", TimeZone.currentSystemDefault().id)
                        })
                        put("end", buildJsonObject {
                            put("dateTime", endTime)
                            put("timeZone", TimeZone.currentSystemDefault().id)
                        })
                    }
                )
            }

            if (response.status.isSuccess()) {
                Result.success("Event created")
            } else {
                Result.failure(Exception("Failed to create event: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
