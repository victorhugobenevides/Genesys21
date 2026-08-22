# Tasks: User Roles & Global Domain Management

## Phase 1: Database & Model Foundation
- [ ] **T001** Update `UserRole` enum to include `ADMIN` and ensure hierarchy logic is clear.
- [ ] **T002** Create `DomainMappingsTable` in the server and add migration.
- [ ] **T003** Create `DomainMapping` domain model in `shared` and ensure serialization.

## Phase 2: Server-Side Logic
- [ ] **T004** Implement `SqliteDomainRepository` to manage the new mappings.
- [ ] **T005** Create `/api/admin/system/domains` routes (Protected by SuperAdmin check).
- [ ] **T006** Update `Application.kt` (or domain resolver) to prioritize global mappings when resolving a hostname.
- [ ] **T007** Add `AuditLogger` entries for all domain mapping operations.

## Phase 3: SuperAdmin UI (Compose)
- [ ] **T008** Add a "Domínios" tab to the `SuperAdminDashboard`.
- [ ] **T009** Implement the list and "Add Mapping" dialog for custom domains.
- [ ] **T010** Add role promotion logic in `UserAdminCard` to support the new `ADMIN` role.

## Phase 4: Security & Validation
- [ ] **T011** Verify route protection: Ensure a Merchant gets 403 when trying to access system domains.
- [ ] **T012** Unit test the role hierarchy logic in `shared`.
- [ ] **T013** Run Paparazzi snapshots for the updated SuperAdmin dashboard.

## Phase 5: Verification & Handover
- [ ] **T014** End-to-End test: Map a dummy local domain to a page and verify redirect/render.
- [ ] **T015** Final audit log check to ensure all actions are traceable.
