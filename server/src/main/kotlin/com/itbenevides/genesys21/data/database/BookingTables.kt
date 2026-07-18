package com.itbenevides.genesys21.data.database

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object BookingServicesTable : Table("booking_services") {
    val id = varchar("id", 50)
    val ownerId = varchar("owner_id", 100).index("idx_booking_services_owner_id")
    val name = varchar("name", 255)
    val price = double("price")
    val description = text("description")
    val durationMinutes = integer("duration_minutes")
    val bufferTimeMinutes = integer("buffer_time_minutes").default(0)
    val categoryId = reference("category_id", CategoriesTable, onDelete = ReferenceOption.SET_NULL).nullable()
    val isEnabled = bool("is_enabled").default(true)

    override val primaryKey = PrimaryKey(id)
}

object BookingServiceImagesTable : Table("booking_service_images") {
    val id = integer("id").autoIncrement()
    val serviceId = varchar("service_id", 50).references(BookingServicesTable.id, onDelete = ReferenceOption.CASCADE)
    val imageUrl = text("image_url")
    val order = integer("image_order")

    override val primaryKey = PrimaryKey(id)
}

object MerchantAvailabilityTable : Table("merchant_availability") {
    val id = integer("id").autoIncrement()
    val merchantId = varchar("merchant_id", 100).uniqueIndex("idx_merchant_availability_merchant_id")

    override val primaryKey = PrimaryKey(id)
}

object WeeklyAvailabilityTable : Table("weekly_availability") {
    val id = integer("id").autoIncrement()
    val availabilityId = integer("availability_id").references(MerchantAvailabilityTable.id, onDelete = ReferenceOption.CASCADE)
    val dayOfWeek = integer("day_of_week") // 1-7
    val startTime = varchar("start_time", 5) // HH:mm
    val endTime = varchar("end_time", 5) // HH:mm
    val isClosed = bool("is_closed").default(false)

    override val primaryKey = PrimaryKey(id)
}

object BlockedDatesTable : Table("blocked_dates") {
    val id = integer("id").autoIncrement()
    val merchantId = varchar("merchant_id", 100).references(MerchantAvailabilityTable.merchantId, onDelete = ReferenceOption.CASCADE)
    val date = date("blocked_date")

    override val primaryKey = PrimaryKey(id)
}

object AppointmentsTable : Table("appointments") {
    val id = varchar("id", 50)
    val userId = varchar("user_id", 100).nullable().index("idx_appointments_user_id")
    val serviceId = varchar("service_id", 50)
    val merchantId = varchar("merchant_id", 100).index("idx_appointments_merchant_id")
    val customerName = varchar("customer_name", 255)
    val customerPhone = varchar("customer_phone", 50)
    val startTime = long("start_time_ms")
    val endTime = long("end_time_ms")
    val status = varchar("status", 20).default("PENDING")
    val orderId = varchar("order_id", 50).references(OrdersTable.id).nullable()

    override val primaryKey = PrimaryKey(id)
}

object AppointmentNotesTable : Table("appointment_notes") {
    val id = varchar("id", 50)
    val appointmentId = varchar("appointment_id", 50).references(AppointmentsTable.id, onDelete = ReferenceOption.CASCADE)
    val content = text("content")
    val createdAt = long("created_at")
    val authorName = varchar("author_name", 255)
    val isPrivate = bool("is_private").default(false)

    override val primaryKey = PrimaryKey(id)
}
