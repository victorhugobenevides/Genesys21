# Tasks: Database Redesign & Security

- [x] T001 Migrate to Store-centric data model (everything linked to `storeId`)
- [x] T002 Implement `StoresTable` to centralize merchant branding and settings
- [x] T003 Use UUIDs (Strings) for all primary and foreign keys
- [x] T004 Add `AuditLogsTable` and basic logging for permissions
- [x] T005 Implement Soft Deletes (`deleted_at`) for Pages and critical entities
- [x] T006 Ensure Ktor server handles multi-tenancy validation in repositories
- [x] T007 Refactor `shared` models to match the new normalized schema
