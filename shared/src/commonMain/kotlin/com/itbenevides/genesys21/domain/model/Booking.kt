package com.itbenevides.genesys21.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object SafeInstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: Instant,
    ) = encoder.encodeString(value.toString())

    override fun deserialize(decoder: Decoder): Instant = Instant.parse(decoder.decodeString())
}

@Serializable
data class BookingService(
    /** UUID */
    val id: String,
    /** Store.id */
    val storeId: String,
    val name: String,
    val description: String? = null,
    val price: Double,
    val durationMinutes: Int,
    val bufferTimeMinutes: Int = 0,
    /** Refactor to String (UUID) */
    val categoryId: String? = null,
    val imageUrls: List<String> = emptyList(),
    val isEnabled: Boolean = true,
    val isOnline: Boolean = false,
    val isHomeService: Boolean = false,
    val maxParticipants: Int = 1,
    /** Hidden from public list */
    val meetingLink: String? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val deletedAt: Long? = null,
)

@Serializable
data class MerchantAvailability(
    /** Rename from merchantId */
    val storeId: String,
    val weeklyConfig: List<DayConfig> = emptyList(),
    val blockedDates: List<LocalDate> = emptyList(),
    val updatedAt: Long = 0,
)

@Serializable
data class DayConfig(
    /** 1 (Mon) to 7 (Sun) */
    val dayOfWeek: Int,
    val slots: List<TimeSlotRange> = emptyList(),
    val isClosed: Boolean = false,
)

@Serializable
data class TimeSlotRange(
    /** HH:mm */
    val startTime: String,
    /** HH:mm */
    val endTime: String,
)

@Serializable
data class BookingNote(
    /** UUID */
    val id: String,
    val content: String,
    /** Link to UserProfile.id */
    val authorId: String? = null,
    val authorName: String,
    val isPrivate: Boolean = false,
    val createdAt: Long = 0,
)

@Serializable
data class Appointment(
    /** UUID */
    val id: String,
    /** Rename from merchantId */
    val storeId: String,
    /** UUID */
    val serviceId: String,
    /** Rename from userId */
    val customerId: String? = null,
    val customerName: String,
    val customerPhone: String,
    @Serializable(with = SafeInstantSerializer::class)
    val startTime: Instant,
    @Serializable(with = SafeInstantSerializer::class)
    val endTime: Instant,
    val status: BookingStatus = BookingStatus.PENDING,
    /** Only visible to the customer and merchant */
    val meetingLink: String? = null,
    /** For home services */
    val address: Address? = null,
    /** Calculated round trip cost */
    val travelFee: Double = 0.0,
    val notes: List<BookingNote> = emptyList(),
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val deletedAt: Long? = null,
)

@Serializable
enum class BookingStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    COMPLETED,
}
