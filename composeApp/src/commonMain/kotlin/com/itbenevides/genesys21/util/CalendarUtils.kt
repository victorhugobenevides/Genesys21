package com.itbenevides.genesys21.util

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object CalendarUtils {

    /**
     * Gera um link para adicionar um evento ao Google Calendar.
     */
    fun generateGoogleCalendarLink(
        title: String,
        description: String,
        startTime: Instant,
        endTime: Instant,
        location: String? = null
    ): String {
        val st = startTime.toLocalDateTime(TimeZone.UTC)
        val et = endTime.toLocalDateTime(TimeZone.UTC)

        val startStr = formatDateTimeForStripe(st)
        val endStr = formatDateTimeForStripe(et)

        val baseUrl = "https://www.google.com/calendar/render?action=TEMPLATE"
        val params = mutableListOf<String>()
        params.add("text=${title.encodeUrl()}")
        params.add("details=${description.encodeUrl()}")
        params.add("dates=$startStr/$endStr")
        if (location != null) {
            params.add("location=${location.encodeUrl()}")
        }

        return "$baseUrl&${params.joinToString("&")}"
    }

    private fun formatDateTimeForStripe(dt: kotlinx.datetime.LocalDateTime): String {
        return "${dt.year}${dt.monthNumber.toString().padStart(2, '0')}${dt.dayOfMonth.toString().padStart(2, '0')}T" +
               "${dt.hour.toString().padStart(2, '0')}${dt.minute.toString().padStart(2, '0')}${dt.second.toString().padStart(2, '0')}Z"
    }

    private fun String.encodeUrl(): String {
        return this.replace(" ", "%20")
            .replace("\n", "%0A")
            .replace("&", "%26")
            .replace("?", "%3F")
    }
}
