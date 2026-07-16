package com.itbenevides.genesys21.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Duration

object SafeInstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Instant) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): Instant = Instant.parse(decoder.decodeString())
}

@Serializable
data class BookingService(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val durationMinutes: Int,
    val bufferTimeMinutes: Int = 0,
    val categoryId: Int? = null,
    val imageUrls: List<String> = emptyList(),
    val isEnabled: Boolean = true,
)

@Serializable
data class MerchantAvailability(
    val merchantId: String,
    val weeklyConfig: List<DayConfig> = emptyList(),
    val blockedDates: List<LocalDate> = emptyList(),
)

@Serializable
data class DayConfig(
    // 1 (Mon) to 7 (Sun)
    val dayOfWeek: Int,
    val slots: List<TimeSlotRange> = emptyList(),
    val isClosed: Boolean = false,
)

@Serializable
data class TimeSlotRange(
    // HH:mm
    val startTime: String,
    // HH:mm
    val endTime: String,
)

@Serializable
data class BookingNote(
    val id: String = "",
    val content: String,
    val createdAt: Long,
    val authorName: String,
    val isPrivate: Boolean = false, // Se true, apenas o mercador vê
)

@Serializable
data class Appointment(
    val id: String,
    val serviceId: String,
    val merchantId: String,
    val customerName: String,
    val customerPhone: String,
    @Serializable(with = SafeInstantSerializer::class)
    val startTime: Instant,
    @Serializable(with = SafeInstantSerializer::class)
    val endTime: Instant,
    val status: BookingStatus = BookingStatus.PENDING,
    val notes: List<BookingNote> = emptyList(),
)

@Serializable
enum class BookingStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    COMPLETED,
}
