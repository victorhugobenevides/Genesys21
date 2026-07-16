package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.data.database.*
import com.itbenevides.genesys21.data.database.DatabaseFactory.dbQuery
import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.domain.repository.BookingRepository
import kotlinx.datetime.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import java.util.UUID

class SqliteBookingRepository : BookingRepository {

    override suspend fun getServices(): List<BookingService> = dbQuery {
        BookingServicesTable.selectAll().map { row ->
            val serviceId = row[BookingServicesTable.id]
            val imageUrls = BookingServiceImagesTable
                .selectAll().where { BookingServiceImagesTable.serviceId eq serviceId }
                .orderBy(BookingServiceImagesTable.order to SortOrder.ASC)
                .map { it[BookingServiceImagesTable.imageUrl] }

            BookingService(
                id = serviceId,
                name = row[BookingServicesTable.name],
                price = row[BookingServicesTable.price],
                description = row[BookingServicesTable.description],
                durationMinutes = row[BookingServicesTable.durationMinutes],
                bufferTimeMinutes = row[BookingServicesTable.bufferTimeMinutes],
                categoryId = row[BookingServicesTable.categoryId]?.value,
                isEnabled = row[BookingServicesTable.isEnabled],
                imageUrls = imageUrls,
            )
        }
    }

    override suspend fun getServiceById(id: String): BookingService? = dbQuery {
        BookingServicesTable.selectAll().where { BookingServicesTable.id eq id }.map { row ->
            val imageUrls = BookingServiceImagesTable
                .selectAll().where { BookingServiceImagesTable.serviceId eq id }
                .orderBy(BookingServiceImagesTable.order to SortOrder.ASC)
                .map { it[BookingServiceImagesTable.imageUrl] }

            BookingService(
                id = row[BookingServicesTable.id],
                name = row[BookingServicesTable.name],
                price = row[BookingServicesTable.price],
                description = row[BookingServicesTable.description],
                durationMinutes = row[BookingServicesTable.durationMinutes],
                bufferTimeMinutes = row[BookingServicesTable.bufferTimeMinutes],
                categoryId = row[BookingServicesTable.categoryId]?.value,
                isEnabled = row[BookingServicesTable.isEnabled],
                imageUrls = imageUrls,
            )
        }.singleOrNull()
    }

    override suspend fun saveService(service: BookingService) {
        dbQuery {
            val exists = BookingServicesTable.selectAll().where { BookingServicesTable.id eq service.id }.count() > 0
            if (exists) {
                BookingServicesTable.update({ BookingServicesTable.id eq service.id }) {
                    it[name] = service.name
                    it[price] = service.price
                    it[description] = service.description
                    it[durationMinutes] = service.durationMinutes
                    it[bufferTimeMinutes] = service.bufferTimeMinutes
                    it[categoryId] = service.categoryId
                    it[isEnabled] = service.isEnabled
                }
            } else {
                BookingServicesTable.insert {
                    it[id] = service.id
                    it[ownerId] = "admin"
                    it[name] = service.name
                    it[price] = service.price
                    it[description] = service.description
                    it[durationMinutes] = service.durationMinutes
                    it[bufferTimeMinutes] = service.bufferTimeMinutes
                    it[categoryId] = service.categoryId
                    it[isEnabled] = service.isEnabled
                }
            }

            BookingServiceImagesTable.deleteWhere { BookingServiceImagesTable.serviceId eq service.id }
            service.imageUrls.forEachIndexed { index, url ->
                BookingServiceImagesTable.insert {
                    it[serviceId] = service.id
                    it[imageUrl] = url
                    it[order] = index
                }
            }
        }
    }

    override suspend fun deleteService(id: String) {
        dbQuery {
            BookingServicesTable.deleteWhere { BookingServicesTable.id eq id }
        }
    }

    override suspend fun getAvailability(merchantId: String): MerchantAvailability = dbQuery {
        val availabilityRow = MerchantAvailabilityTable.selectAll().where { MerchantAvailabilityTable.merchantId eq merchantId }.singleOrNull()
            ?: return@dbQuery MerchantAvailability(merchantId = merchantId)

        val availabilityId = availabilityRow[MerchantAvailabilityTable.id]

        val weeklyRows = WeeklyAvailabilityTable.selectAll().where { WeeklyAvailabilityTable.availabilityId eq availabilityId }.toList()

        val weeklyConfigs = weeklyRows.groupBy { it[WeeklyAvailabilityTable.dayOfWeek] }.map { (day, rows) ->
            DayConfig(
                dayOfWeek = day,
                slots = rows.map { TimeSlotRange(it[WeeklyAvailabilityTable.startTime], it[WeeklyAvailabilityTable.endTime]) },
                isClosed = rows.any { it[WeeklyAvailabilityTable.isClosed] }
            )
        }

        val blocked = BlockedDatesTable.selectAll().where { BlockedDatesTable.merchantId eq merchantId }.map {
            it[BlockedDatesTable.date]
        }

        MerchantAvailability(
            merchantId = merchantId,
            weeklyConfig = weeklyConfigs,
            blockedDates = blocked
        )
    }

    override suspend fun saveAvailability(availability: MerchantAvailability) {
        dbQuery {
            val availabilityRow = MerchantAvailabilityTable.selectAll().where { MerchantAvailabilityTable.merchantId eq availability.merchantId }.singleOrNull()
            val availabilityId = if (availabilityRow == null) {
                MerchantAvailabilityTable.insert {
                    it[merchantId] = availability.merchantId
                }[MerchantAvailabilityTable.id]
            } else {
                availabilityRow[MerchantAvailabilityTable.id]
            }

            WeeklyAvailabilityTable.deleteWhere { WeeklyAvailabilityTable.availabilityId eq availabilityId }
            availability.weeklyConfig.forEach { config ->
                config.slots.forEach { slot ->
                    WeeklyAvailabilityTable.insert {
                        it[WeeklyAvailabilityTable.availabilityId] = availabilityId
                        it[dayOfWeek] = config.dayOfWeek
                        it[startTime] = slot.startTime
                        it[endTime] = slot.endTime
                        it[isClosed] = config.isClosed
                    }
                }
            }

            BlockedDatesTable.deleteWhere { merchantId eq availability.merchantId }
            availability.blockedDates.forEach { date ->
                BlockedDatesTable.insert {
                    it[merchantId] = availability.merchantId
                    it[BlockedDatesTable.date] = date
                }
            }
        }
    }

    override suspend fun getAppointments(serviceId: String?, merchantId: String?, date: LocalDate): List<Appointment> = dbQuery {
        if (serviceId.isNullOrBlank() && merchantId.isNullOrBlank()) return@dbQuery emptyList()

        try {
            val javaDate = java.time.LocalDate.of(date.year, date.month.number, date.day)
            val startOfDay = javaDate.atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
            val margin = 12 * 60 * 60 * 1000L

            val queryStart = startOfDay - margin
            val queryEnd = startOfDay + (24 * 60 * 60 * 1000) + margin

            var conditions = (AppointmentsTable.startTime greaterEq queryStart) and (AppointmentsTable.startTime lessEq queryEnd)

            if (!serviceId.isNullOrBlank()) {
                conditions = conditions and (AppointmentsTable.serviceId eq serviceId)
            }

            if (!merchantId.isNullOrBlank()) {
                conditions = conditions and (AppointmentsTable.merchantId eq merchantId)
            }

            AppointmentsTable.selectAll().where { conditions }
                .map { row ->
                    val apptId = row[AppointmentsTable.id]
                    val notes = AppointmentNotesTable.selectAll()
                        .where { AppointmentNotesTable.appointmentId eq apptId }
                        .orderBy(AppointmentNotesTable.createdAt to SortOrder.ASC)
                        .map { noteRow ->
                            BookingNote(
                                id = noteRow[AppointmentNotesTable.id],
                                content = noteRow[AppointmentNotesTable.content],
                                createdAt = noteRow[AppointmentNotesTable.createdAt],
                                authorName = noteRow[AppointmentNotesTable.authorName],
                                isPrivate = noteRow[AppointmentNotesTable.isPrivate]
                            )
                        }

                    Appointment(
                        id = apptId,
                        serviceId = row[AppointmentsTable.serviceId],
                        merchantId = row[AppointmentsTable.merchantId],
                        customerName = row[AppointmentsTable.customerName],
                        customerPhone = row[AppointmentsTable.customerPhone],
                        startTime = Instant.fromEpochMilliseconds(row[AppointmentsTable.startTime]),
                        endTime = Instant.fromEpochMilliseconds(row[AppointmentsTable.endTime]),
                        status = try {
                            BookingStatus.valueOf(row[AppointmentsTable.status])
                        } catch (e: Exception) {
                            BookingStatus.PENDING
                        },
                        notes = notes
                    )
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getAppointmentsByPhone(phone: String): List<Appointment> = dbQuery {
        AppointmentsTable.selectAll()
            .where { AppointmentsTable.customerPhone eq phone }
            .orderBy(AppointmentsTable.startTime to SortOrder.DESC)
            .map { row ->
                val apptId = row[AppointmentsTable.id]
                val notes = AppointmentNotesTable.selectAll()
                    .where { AppointmentNotesTable.appointmentId eq apptId }
                    .orderBy(AppointmentNotesTable.createdAt to SortOrder.ASC)
                    .map { noteRow ->
                        BookingNote(
                            id = noteRow[AppointmentNotesTable.id],
                            content = noteRow[AppointmentNotesTable.content],
                            createdAt = noteRow[AppointmentNotesTable.createdAt],
                            authorName = noteRow[AppointmentNotesTable.authorName],
                            isPrivate = noteRow[AppointmentNotesTable.isPrivate]
                        )
                    }

                Appointment(
                    id = apptId,
                    serviceId = row[AppointmentsTable.serviceId],
                    merchantId = row[AppointmentsTable.merchantId],
                    customerName = row[AppointmentsTable.customerName],
                    customerPhone = row[AppointmentsTable.customerPhone],
                    startTime = Instant.fromEpochMilliseconds(row[AppointmentsTable.startTime]),
                    endTime = Instant.fromEpochMilliseconds(row[AppointmentsTable.endTime]),
                    status = try {
                        BookingStatus.valueOf(row[AppointmentsTable.status])
                    } catch (e: Exception) {
                        BookingStatus.PENDING
                    },
                    notes = notes
                )
            }
    }

    override suspend fun createAppointment(appointment: Appointment) {
        dbQuery {
            try {
                val mid = appointment.merchantId.ifBlank { "admin" }
                val aid = appointment.id.ifBlank { UUID.randomUUID().toString() }

                AppointmentsTable.insert {
                    it[id] = aid
                    it[serviceId] = appointment.serviceId
                    it[merchantId] = mid
                    it[customerName] = appointment.customerName
                    it[customerPhone] = appointment.customerPhone
                    it[startTime] = appointment.startTime.toEpochMilliseconds()
                    it[endTime] = appointment.endTime.toEpochMilliseconds()
                    it[status] = appointment.status.name
                }

                appointment.notes.forEach { note ->
                    AppointmentNotesTable.insert {
                        it[id] = note.id.ifBlank { UUID.randomUUID().toString() }
                        it[appointmentId] = aid
                        it[content] = note.content
                        it[createdAt] = note.createdAt
                        it[authorName] = note.authorName
                        it[isPrivate] = note.isPrivate
                    }
                }
            } catch (e: Exception) {
                throw e
            }
        }
    }

    override suspend fun updateAppointment(appointment: Appointment) {
        dbQuery {
            AppointmentsTable.update({ AppointmentsTable.id eq appointment.id }) {
                it[status] = appointment.status.name
            }

            val existingNoteIds = AppointmentNotesTable.selectAll()
                .where { AppointmentNotesTable.appointmentId eq appointment.id }
                .map { it[AppointmentNotesTable.id] }.toSet()

            appointment.notes.filter { it.id !in existingNoteIds }.forEach { note ->
                AppointmentNotesTable.insert {
                    it[id] = note.id.ifBlank { UUID.randomUUID().toString() }
                    it[appointmentId] = appointment.id
                    it[content] = note.content
                    it[createdAt] = note.createdAt
                    it[authorName] = note.authorName
                    it[isPrivate] = note.isPrivate
                }
            }
        }
    }
}
