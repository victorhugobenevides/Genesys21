# Implementation Plan: Genesys Booking System

This feature will be implemented in 4 phases to ensure stability and total integration with the existing Design System.

## Phase 1: Core Domain & Data (Backend + Shared)
- [x] Define `BookingService` and `Appointment` models in `shared`.
- [x] Create `MerchantAvailability` configuration structure.
- [x] Implement Server migrations for `services`, `availability`, and `appointments`.
- [x] Develop `BookingRepository` to fetch available slots based on a specific date.

## Phase 2: Design System Extensions (Atoms & Molecules)
- [x] **Atoms**:
    - `GenesysCalendarDay`: A clickable day tile with "today" and "selected" states.
    - `GenesysTimeChip`: Displays a time slot (e.g., "14:30").
- [x] **Molecules**:
    - `GenesysDatePicker`: A grid-based date selector.
    - `GenesysTimePicker`: A selection area for available slots.
- [x] **Organisms**:
    - `GenesysBookingEngine`: The main component that coordinates date -> slot availability -> selection.
- [x] Update showcase with these new components.

## Phase 3: Editor & Admin Integration
- [x] Add `PageComponent.ServiceList` to the WhiteLabel system.
- [x] Create `ServiceComponentEditor` with price/duration settings.
- [x] Implement the **Merchant Agenda Screen** in the Admin dashboard.
- [x] Add the "Availability Settings" dialog to the Page Editor.

## Phase 4: Consumer Flow & Checkout
- [x] Update `ProductDetailsScreen` to detect if a product is a "Service".
- [x] Integrate `GenesysBookingEngine` into the details view.
- [x] Modify `CartItem` to include `appointmentTime`.
- [x] Implement checkout validation to lock slots upon payment/submission.
- [x] Add "Add to Google Calendar" button to the `OrderTrackingScreen` for services.
