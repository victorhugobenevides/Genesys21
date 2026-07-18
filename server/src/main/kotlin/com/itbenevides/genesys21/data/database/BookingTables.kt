package com.itbenevides.genesys21.data.database

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.date

object BookingServicesTable : BaseTable("booking_services") {
    val id = varchar("id", 50) // UUID
    val storeId = varchar("store_id", 50).references(StoresTable.id, onDelete = ReferenceOption.CASCADE)
    val name = varchar("name", 255)
    val price = double("price")
    val description = text("description").nullable()
    val durationMinutes = integer("duration_minutes")
    val bufferTimeMinutes = integer("buffer_time_minutes").default(0)
    val categoryId = varchar("category_id", 50).references(CategoriesTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val isEnabled = bool("is_enabled").default(true)

    override val primaryKey = PrimaryKey(id)
}

object BookingServiceImagesTable : BaseTable("booking_service_images") {
    val id = varchar("id", 50) // UUID
    val serviceId = varchar("service_id", 50).references(BookingServicesTable.id, onDelete = ReferenceOption.CASCADE)
    val imageUrl = text("image_url")
    val order = integer("image_order")

    override val primaryKey = PrimaryKey(id)
}

object MerchantAvailabilityTable : BaseTable("merchant_availability") {
    val id = varchar("id", 50) // UUID
    val storeId = varchar("store_id", 50).references(StoresTable.id, onDelete = ReferenceOption.CASCADE).uniqueIndex()

    override val primaryKey = PrimaryKey(id)
}

object WeeklyAvailabilityTable : BaseTable("weekly_availability") {
    val id = varchar("id", 50) // UUID
    val availabilityId = varchar("availability_id", 50).references(MerchantAvailabilityTable.id, onDelete = ReferenceOption.CASCADE)
    val dayOfWeek = integer("day_of_week") // 1-7
    val startTime = varchar("start_time", 5) // HH:mm
    val endTime = varchar("end_time", 5) // HH:mm
    val isClosed = bool("is_closed").default(false)

    override val primaryKey = PrimaryKey(id)
}

object BlockedDatesTable : BaseTable("blocked_dates") {
    val id = varchar("id", 50) // UUID
    val storeId = varchar("store_id", 50).references(StoresTable.id, onDelete = ReferenceOption.CASCADE)
    val date = date("blocked_date")

    override val primaryKey = PrimaryKey(id)
}

object AppointmentsTable : BaseTable("appointments") {
    val id = varchar("id", 50) // UUID
    val storeId = varchar("store_id", 50).references(StoresTable.id, onDelete = ReferenceOption.CASCADE)
    val serviceId = varchar("service_id", 50).references(BookingServicesTable.id, onDelete = ReferenceOption.RESTRICT)
    val customerId = varchar("customer_id", 100).references(UsersTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val customerName = varchar("customer_name", 255)
    val customerPhone = varchar("customer_phone", 50)
    val startTime = long("start_time_ms")
    val endTime = long("end_time_ms")
    val status = varchar("status", 20).default("PENDING")
    val orderId = varchar("order_id", 50).references(OrdersTable.id, onDelete = ReferenceOption.SET_NULL).nullable()

    override val primaryKey = PrimaryKey(id)
}

object AppointmentNotesTable : BaseTable("appointment_notes") {
    val id = varchar("id", 50) // UUID
    val appointmentId = varchar("appointment_id", 50).references(AppointmentsTable.id, onDelete = ReferenceOption.CASCADE)
    val content = text("content")
    val authorId = varchar("author_id", 100).references(UsersTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val authorName = varchar("author_name", 255)
    val isPrivate = bool("is_private").default(false)

    override val primaryKey = PrimaryKey(id)
}
