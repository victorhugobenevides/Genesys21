package com.itbenevides.genesys21.domain.usecase

import com.itbenevides.genesys21.domain.repository.BookingRepository
import kotlinx.datetime.*
import kotlin.time.Clock.System.now

class ValidateBookingSlotUseCase(
    private val repository: BookingRepository,
) {
    suspend operator fun invoke(
        storeId: String,
        serviceId: String,
        startTime: Instant,
        endTime: Instant,
    ): Result<Boolean> {
        // Conversão segura para LocalDate
        val date =
            try {
                startTime.toLocalDateTime(TimeZone.UTC).date
            } catch (e: Exception) {
                LocalDate.parse(startTime.toString().take(10))
            }

        // 1. Não permitir agendamentos no passado
        val currentNow = now()
        if (startTime < currentNow) {
            return Result.success(false)
        }

        // 2. Check merchant availability for that day
        val availability = repository.getAvailability(storeId)
        if (availability != null && availability.weeklyConfig.isNotEmpty()) {
            // Check blocked dates
            if (availability.blockedDates.contains(date)) {
                return Result.success(false)
            }

            // Check weekly config
            val dayOfWeekNumber = date.dayOfWeek.ordinal + 1
            val dayConfig = availability.weeklyConfig.find { it.dayOfWeek == dayOfWeekNumber }

            // Só bloqueia se houver configuração explícita dizendo que está fechado
            if (dayConfig != null && dayConfig.isClosed) {
                return Result.success(false)
            }
        }

        // 3. Check for overlapping appointments
        // Buscamos apenas os agendamentos do mercador (independente do serviço)
        // para garantir que um profissional não tenha dois clientes ao mesmo tempo.
        val existingAppointments = repository.getAppointments(null, storeId, date)
        val hasOverlap =
            existingAppointments.any {
                (startTime >= it.startTime && startTime < it.endTime) ||
                    (endTime > it.startTime && endTime <= it.endTime) ||
                    (startTime <= it.startTime && endTime >= it.endTime)
            }

        return Result.success(!hasOverlap)
    }
}
