# Implementation Plan: Genesys Booking System

This feature will be implemented in 4 phases to ensure stability and total integration with the existing Design System.

## Phase 1: Core Domain & Data (Backend + Shared)
- [ ] Define `BookingService` and `Appointment` models in `shared`.
- [ ] Create `MerchantAvailability` configuration structure.
- [ ] Implement Server migrations for `services`, `availability`, and `appointments`.
- [ ] Develop `BookingRepository` to fetch available slots based on a specific date.

## Phase 2: Design System Extensions (Atoms & Molecules)
- [ ] **Atoms**:
    - `GenesysCalendarDay`: A clickable day tile with "today" and "selected" states.
    - `GenesysTimeChip`: Displays a time slot (e.g., "14:30").
- [ ] **Molecules**:
    - `GenesysDatePicker`: A grid-based date selector.
    - `GenesysTimePicker`: A selection area for available slots.
- [ ] **Organisms**:
    - `GenesysBookingEngine`: The main component that coordinates date -> slot availability -> selection.
- [ ] Update `/about` showcase with these new components.

## Phase 3: Editor & Admin Integration
- [ ] Add `PageComponent.ServiceList` to the WhiteLabel system.
- [ ] Create `ServiceComponentEditor` with price/duration settings.
- [ ] Implement the **Merchant Agenda Screen** in the Admin dashboard.
- [ ] Add the "Availability Settings" dialog to the Page Editor.

## Phase 4: Consumer Flow & Checkout
- [ ] Update `ProductDetailsScreen` to detect if a product is a "Service".
- [ ] Integrate `GenesysBookingEngine` into the details view.
- [ ] Modify `CartItem` to include `appointmentTime`.
- [ ] Implement checkout validation to lock slots upon payment/submission.
- [ ] Add "Add to Google Calendar" button to the `OrderTrackingScreen` for services.
