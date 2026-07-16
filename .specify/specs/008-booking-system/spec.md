# Specification: Genesys Booking System (T043)

## 1. Overview
The **Genesys Booking System** aims to transform the platform from a simple product vitrine into a versatile service marketplace. It allows merchants to offer time-based services (appointments, rentals, consultations) directly through their white-label pages.

## 2. Core Concepts
- **Service**: A special type of "product" that has a duration, price, and availability rules instead of physical stock.
- **Schedule (Agenda)**: The merchant's availability configuration (working hours, breaks, holidays).
- **Booking**: A reserved time slot, integrated into the Cart and Order system.
- **Time Slot**: A specific window of time (e.g., 14:00 - 15:00) available for a service.

## 3. Data Structures

### 3.1. BookingService (Domain Model)
Extends the current `Product` concept or lives alongside it.
```kotlin
data class BookingService(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val durationMinutes: Int,
    val bufferTimeMinutes: Int = 0, // Time between appointments
    val categoryId: Int? = null,
    val imageUrls: List<String> = emptyList()
)
```

### 3.2. MerchantAvailability
Configures when the store is open for bookings.
```kotlin
data class MerchantAvailability(
    val merchantId: String,
    val weeklyConfig: List<DayConfig>, // Mon-Sun working hours
    val blockedDates: List<LocalDate>, // Holidays or custom off-days
)

data class DayConfig(
    val dayOfWeek: DayOfWeek,
    val slots: List<TimeRange>, // e.g., 08:00-12:00, 13:00-18:00
    val isClosed: Boolean = false
)
```

### 3.3. Appointment (The Booking Instance)
```kotlin
data class Appointment(
    val id: String,
    val serviceId: String,
    val customerName: String,
    val customerPhone: String,
    val startTime: Instant,
    val endTime: Instant,
    val status: BookingStatus // PENDING, CONFIRMED, CANCELLED
)
```

## 4. Integration Points

### 4.1. WhiteLabel Editor
- **New Block**: `BookingGrid` / `ServiceList`.
- **Editor UI**: Interface to select which services to display.
- **Calendar Setup**: A new "Calendar" tab in the Editor to configure the `MerchantAvailability`.

### 4.2. Public Viewer & Product Details
- **Booking Selector**: When a "Service" is clicked, instead of "Add to Cart" immediately, show a date/time picker.
- **Slot Logic**: Front-end must fetch available slots from the server based on existing appointments and merchant config.

### 4.3. Cart & Checkout
- **Cart Integration**: Services in the cart must store the `selectedStartTime`.
- **Validation**: Re-verify slot availability during checkout to prevent double-booking.

### 4.4. Admin Pages
- **Agenda View**: A master calendar view for the merchant to see all upcoming appointments.
- **Order Details**: Orders containing services will show the appointment details (Time, Duration).

## 5. UI/UX Requirements
- **Atomic Design**:
    - **Atoms**: `CalendarDay`, `TimeSlotChip`.
    - **Molecules**: `DatePickerGrid`, `TimeSlotSelector`.
    - **Organisms**: `BookingCalendar` (Combines date and time selection).
- **Responsiveness**: Calendar must be fluid on mobile and a full-view grid on desktop.
- **Showcase**: All booking components must be added to the Design System Showcase (`/about`).

## 6. Technical Stack
- **Shared**: Logic for slot calculation (commonMain).
- **Server**: SQLite tables for `Availability` and `Appointments`.
- **Compose**: `kotlinx-datetime` for time management.
