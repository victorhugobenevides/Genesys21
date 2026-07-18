# Implementation Plan: Database Redesign & Security

## Phase 1: Shared Model Refactoring
- [ ] Define `Store` and `Profile` models in `shared`.
- [ ] Update all `id` fields in existing models to `String` (containing UUIDs).
- [ ] Add `createdAt`, `updatedAt`, and `deletedAt` fields to base entities.
- [ ] Refactor `Order` and `Appointment` to use `storeId` instead of `ownerId`.

## Phase 2: Server-Side Table Definition (Exposed)
- [ ] Create `BaseTable` class to centralize `created_at`, `updated_at`, and `deleted_at`.
- [ ] Re-implement `UsersTable` and create `StoresTable`.
- [ ] Re-implement `PagesTable` and `PageComponentsTable` using UUIDs.
- [ ] Re-implement `ProductsTable`, `CategoriesTable`, and `MediaTable`.
- [ ] Re-implement `OrdersTable` and `BookingTables` with strict foreign keys.
- [ ] Create `AuditLogsTable` for tracking actions.

## Phase 3: Fresh Initialization & Data Migration
- [ ] Implement `DatabaseFactory.dropAndRebuild()` for testing and initial reset.
- [ ] Create a `Seeder` utility to populate the new schema with basic templates and a test SuperAdmin.
- [ ] Update `DatabaseMigrator` to handle the transition (even if it's a hard reset).

## Phase 4: Repository Layer Alignment
- [ ] Update `SqliteUserRepository` to handle the `User` + `Profile` split.
- [ ] Update `SqlitePageRepository` to filter by `storeId`.
- [ ] Update `SqliteOrderRepository` and `SqliteBookingRepository` to include audit log creation on state changes.
- [ ] Implement global "Soft Delete" filter in repository select queries.

## Phase 5: Testing & Security Verification
- [ ] Add integration tests for UUID generation and collision safety.
- [ ] Verify that a user cannot access data from a `storeId` they don't own.
- [ ] Verify "Soft Delete" prevents data from appearing in lists but remains in the DB.
- [ ] Run full screenshot and unit test suite to ensure zero regression.
