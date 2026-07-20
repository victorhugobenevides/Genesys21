package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.data.database.*
import com.itbenevides.genesys21.data.database.DatabaseFactory.dbQuery
import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.domain.repository.BookingRepository
import kotlinx.datetime.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import java.util.UUID

class SqliteBookingRepository : BookingRepository {

    override suspend fun getServices(): List<BookingService> = dbQuery {
        BookingServicesTable.selectAll()
            .where { BookingServicesTable.deletedAt.isNull() }
            .map { row ->
            val serviceId = row[BookingServicesTable.id]
            val imageUrls = BookingServiceImagesTable
                .selectAll().where { BookingServiceImagesTable.serviceId eq serviceId }
                .orderBy(BookingServiceImagesTable.updatedAt to SortOrder.ASC)
                .map { it[BookingServiceImagesTable.imageUrl] }

            BookingService(
                id = serviceId,
                storeId = row[BookingServicesTable.storeId],
                name = row[BookingServicesTable.name],
                price = row[BookingServicesTable.price],
                description = row[BookingServicesTable.description],
                durationMinutes = row[BookingServicesTable.durationMinutes],
                bufferTimeMinutes = row[BookingServicesTable.bufferTimeMinutes],
                categoryId = row[BookingServicesTable.categoryId],
                isEnabled = row[BookingServicesTable.isEnabled],
                imageUrls = imageUrls,
                createdAt = row[BookingServicesTable.createdAt],
                updatedAt = row[BookingServicesTable.updatedAt],
                deletedAt = row[BookingServicesTable.deletedAt]
            )
        }
    }

    override suspend fun getServiceById(id: String): BookingService? = dbQuery {
        BookingServicesTable.selectAll().where { (BookingServicesTable.id eq id) and (BookingServicesTable.deletedAt.isNull()) }.map { row ->
            val imageUrls = BookingServiceImagesTable
                .selectAll().where { BookingServiceImagesTable.serviceId eq id }
                .orderBy(BookingServiceImagesTable.updatedAt to SortOrder.ASC)
                .map { it[BookingServiceImagesTable.imageUrl] }

            BookingService(
                id = row[BookingServicesTable.id],
                storeId = row[BookingServicesTable.storeId],
                name = row[BookingServicesTable.name],
                price = row[BookingServicesTable.price],
                description = row[BookingServicesTable.description],
                durationMinutes = row[BookingServicesTable.durationMinutes],
                bufferTimeMinutes = row[BookingServicesTable.bufferTimeMinutes],
                categoryId = row[BookingServicesTable.categoryId],
                isEnabled = row[BookingServicesTable.isEnabled],
                imageUrls = imageUrls,
                createdAt = row[BookingServicesTable.createdAt],
                updatedAt = row[BookingServicesTable.updatedAt],
                deletedAt = row[BookingServicesTable.deletedAt]
            )
        }.singleOrNull()
    }

    override suspend fun saveService(service: BookingService, token: String) {
        dbQuery {
            // Validação de Posse
            val isOwner = StoresTable.selectAll()
                .where { (StoresTable.id eq service.storeId) and (StoresTable.ownerId eq token) }
                .count() > 0
            if (!isOwner) throw Exception("Acesso negado: Você não é o dono desta loja.")

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
                    it[updatedAt] = System.currentTimeMillis()
                }
            } else {
                BookingServicesTable.insert {
                    it[id] = service.id.ifBlank { java.util.UUID.randomUUID().toString() }
                    it[storeId] = service.storeId
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
                    it[id] = java.util.UUID.randomUUID().toString()
                    it[serviceId] = service.id
                    it[imageUrl] = url
                    it[order] = index
                }
            }
        }
    }

    override suspend fun deleteService(id: String, token: String) {
        dbQuery {
            // Busca storeId do serviço para validar posse
            val storeId = BookingServicesTable.select(BookingServicesTable.storeId)
                .where { BookingServicesTable.id eq id }
                .firstOrNull()?.get(BookingServicesTable.storeId)

            if (storeId == null) throw Exception("Serviço não encontrado.")

            val isOwner = StoresTable.selectAll()
                .where { (StoresTable.id eq storeId) and (StoresTable.ownerId eq token) }
                .count() > 0
            if (!isOwner) throw Exception("Acesso negado.")

            BookingServicesTable.update({ BookingServicesTable.id eq id }) {
                it[deletedAt] = System.currentTimeMillis()
            }
        }
    }

    override suspend fun getAvailability(storeId: String): MerchantAvailability = dbQuery {
        val availabilityRow = MerchantAvailabilityTable.selectAll().where { MerchantAvailabilityTable.storeId eq storeId }.singleOrNull()
            ?: return@dbQuery MerchantAvailability(storeId = storeId)

        val availabilityId = availabilityRow[MerchantAvailabilityTable.id]

        val weeklyRows = WeeklyAvailabilityTable.selectAll().where { WeeklyAvailabilityTable.availabilityId eq availabilityId }.toList()

        val weeklyConfigs = weeklyRows.groupBy { it[WeeklyAvailabilityTable.dayOfWeek] }.map { (day, rows) ->
            DayConfig(
                dayOfWeek = day,
                slots = rows.map { TimeSlotRange(it[WeeklyAvailabilityTable.startTime], it[WeeklyAvailabilityTable.endTime]) },
                isClosed = rows.any { it[WeeklyAvailabilityTable.isClosed] }
            )
        }

        val blocked = BlockedDatesTable.selectAll().where { BlockedDatesTable.storeId eq storeId }.map {
            it[BlockedDatesTable.date]
        }

        MerchantAvailability(
            storeId = storeId,
            weeklyConfig = weeklyConfigs,
            blockedDates = blocked,
            updatedAt = availabilityRow[MerchantAvailabilityTable.updatedAt]
        )
    }

    override suspend fun saveAvailability(availability: MerchantAvailability, token: String) {
        dbQuery {
            // Validação de Posse
            val isOwner = StoresTable.selectAll()
                .where { (StoresTable.id eq availability.storeId) and (StoresTable.ownerId eq token) }
                .count() > 0
            if (!isOwner) throw Exception("Acesso negado.")

            val availabilityRow = MerchantAvailabilityTable.selectAll().where { MerchantAvailabilityTable.storeId eq availability.storeId }.singleOrNull()
            val availabilityId = if (availabilityRow == null) {
                val newId = java.util.UUID.randomUUID().toString()
                MerchantAvailabilityTable.insert {
                    it[id] = newId
                    it[storeId] = availability.storeId
                }
                newId
            } else {
                val aid = availabilityRow[MerchantAvailabilityTable.id]
                MerchantAvailabilityTable.update({ MerchantAvailabilityTable.id eq aid }) {
                    it[updatedAt] = System.currentTimeMillis()
                }
                aid
            }

            WeeklyAvailabilityTable.deleteWhere { WeeklyAvailabilityTable.availabilityId eq availabilityId }
            availability.weeklyConfig.forEach { config ->
                config.slots.forEach { slot ->
                    WeeklyAvailabilityTable.insert {
                        it[id] = java.util.UUID.randomUUID().toString()
                        it[WeeklyAvailabilityTable.availabilityId] = availabilityId
                        it[dayOfWeek] = config.dayOfWeek
                        it[startTime] = slot.startTime
                        it[endTime] = slot.endTime
                        it[isClosed] = config.isClosed
                    }
                }
            }

            BlockedDatesTable.deleteWhere { storeId eq availability.storeId }
            availability.blockedDates.forEach { date ->
                BlockedDatesTable.insert {
                    it[id] = java.util.UUID.randomUUID().toString()
                    it[storeId] = availability.storeId
                    it[BlockedDatesTable.date] = date
                }
            }
        }
    }

    override suspend fun getAppointments(serviceId: String?, storeId: String?, date: LocalDate): List<Appointment> = dbQuery {
        if (serviceId.isNullOrBlank() && storeId.isNullOrBlank()) return@dbQuery emptyList()

        try {
            val javaDate = java.time.LocalDate.of(date.year, date.month.number, date.day)
            val startOfDay = javaDate.atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
            val margin = 12 * 60 * 60 * 1000L

            val queryStart = startOfDay - margin
            val queryEnd = startOfDay + (24 * 60 * 60 * 1000) + margin

            var conditions = (AppointmentsTable.startTime greaterEq queryStart) and (AppointmentsTable.startTime lessEq queryEnd)
            conditions = conditions and (AppointmentsTable.deletedAt.isNull())

            if (!serviceId.isNullOrBlank()) {
                conditions = conditions and (AppointmentsTable.serviceId eq serviceId)
            }

            if (!storeId.isNullOrBlank()) {
                conditions = conditions and (AppointmentsTable.storeId eq storeId)
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
                                authorId = noteRow[AppointmentNotesTable.authorId],
                                authorName = noteRow[AppointmentNotesTable.authorName],
                                isPrivate = noteRow[AppointmentNotesTable.isPrivate],
                                createdAt = noteRow[AppointmentNotesTable.createdAt]
                            )
                        }

                    Appointment(
                        id = apptId,
                        storeId = row[AppointmentsTable.storeId],
                        serviceId = row[AppointmentsTable.serviceId],
                        customerId = row[AppointmentsTable.customerId],
                        customerName = row[AppointmentsTable.customerName],
                        customerPhone = row[AppointmentsTable.customerPhone],
                        startTime = Instant.fromEpochMilliseconds(row[AppointmentsTable.startTime]),
                        endTime = Instant.fromEpochMilliseconds(row[AppointmentsTable.endTime]),
                        status = try {
                            BookingStatus.valueOf(row[AppointmentsTable.status])
                        } catch (e: Exception) {
                            BookingStatus.PENDING
                        },
                        notes = notes,
                        createdAt = row[AppointmentsTable.createdAt],
                        updatedAt = row[AppointmentsTable.updatedAt],
                        deletedAt = row[AppointmentsTable.deletedAt]
                    )
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getAppointmentsByPhone(phone: String): List<Appointment> = dbQuery {
        AppointmentsTable.selectAll()
            .where { (AppointmentsTable.customerPhone eq phone) and (AppointmentsTable.deletedAt.isNull()) }
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
                            authorId = noteRow[AppointmentNotesTable.authorId],
                            authorName = noteRow[AppointmentNotesTable.authorName],
                            isPrivate = noteRow[AppointmentNotesTable.isPrivate],
                            createdAt = noteRow[AppointmentNotesTable.createdAt]
                        )
                    }

                Appointment(
                    id = apptId,
                    storeId = row[AppointmentsTable.storeId],
                    serviceId = row[AppointmentsTable.serviceId],
                    customerId = row[AppointmentsTable.customerId],
                    customerName = row[AppointmentsTable.customerName],
                    customerPhone = row[AppointmentsTable.customerPhone],
                    startTime = Instant.fromEpochMilliseconds(row[AppointmentsTable.startTime]),
                    endTime = Instant.fromEpochMilliseconds(row[AppointmentsTable.endTime]),
                    status = try {
                        BookingStatus.valueOf(row[AppointmentsTable.status])
                    } catch (e: Exception) {
                        BookingStatus.PENDING
                    },
                    notes = notes,
                    createdAt = row[AppointmentsTable.createdAt],
                    updatedAt = row[AppointmentsTable.updatedAt],
                    deletedAt = row[AppointmentsTable.deletedAt]
                )
            }
    }

    override suspend fun createAppointment(appointment: Appointment) {
        dbQuery {
            try {
                val aid = appointment.id.ifBlank { java.util.UUID.randomUUID().toString() }

                AppointmentsTable.insert {
                    it[id] = aid
                    it[storeId] = appointment.storeId
                    it[serviceId] = appointment.serviceId
                    it[customerId] = appointment.customerId
                    it[customerName] = appointment.customerName
                    it[customerPhone] = appointment.customerPhone
                    it[startTime] = appointment.startTime.toEpochMilliseconds()
                    it[endTime] = appointment.endTime.toEpochMilliseconds()
                    it[status] = appointment.status.name
                }

                appointment.notes.forEach { note ->
                    AppointmentNotesTable.insert {
                        it[id] = note.id.ifBlank { java.util.UUID.randomUUID().toString() }
                        it[appointmentId] = aid
                        it[content] = note.content
                        it[authorId] = note.authorId
                        it[authorName] = note.authorName
                        it[isPrivate] = note.isPrivate
                    }
                }
            } catch (e: Exception) {
                throw e
            }
        }
    }

    override suspend fun updateAppointment(appointment: Appointment, token: String) {
        dbQuery {
            // Validação de Posse
            val isOwner = StoresTable.selectAll()
                .where { (StoresTable.id eq appointment.storeId) and (StoresTable.ownerId eq token) }
                .count() > 0
            if (!isOwner) throw Exception("Acesso negado.")

            AppointmentsTable.update({ AppointmentsTable.id eq appointment.id }) {
                it[status] = appointment.status.name
                it[updatedAt] = System.currentTimeMillis()
            }

            val existingNoteIds = AppointmentNotesTable.selectAll()
                .where { AppointmentNotesTable.appointmentId eq appointment.id }
                .map { it[AppointmentNotesTable.id] }.toSet()

            appointment.notes.filter { it.id !in existingNoteIds }.forEach { note ->
                AppointmentNotesTable.insert {
                    it[id] = note.id.ifBlank { java.util.UUID.randomUUID().toString() }
                    it[appointmentId] = appointment.id
                    it[content] = note.content
                    it[authorId] = note.authorId
                    it[authorName] = note.authorName
                    it[isPrivate] = note.isPrivate
                }
            }
        }
    }
}
