# Tasks: Spec 020 - Data Resilience & Disaster Recovery

## Phase 1: Local Hardening
- [ ] **T001** Implement `PRAGMA integrity_check` on `DatabaseFactory.init`.
- [ ] **T002** Upgrade `BackupService` to use `ZIP` compression for backups.
- [ ] **T003** Increase local backup rotation to 30 days (Tier 1).

## Phase 2: Off-site Integration
- [ ] **T004** Create `CloudStorageService` (interface + implementation for GCS/S3).
- [ ] **T005** Automate daily upload of DB zip and `/uploads` folder to cloud storage.
- [ ] **T006** Implement encrypted backup (AES-256) before upload.

## Phase 3: Work-in-Progress Stability
- [ ] **T007** Add `drafts` table to SQLite to support multi-device work-in-progress.
- [ ] **T008** Sync `PageDraftRepository` with the server-side drafts table for logged-in users.

## Phase 4: Pipeline Safeguards
- [ ] **T009** Create a CI task to run Flyway migrations against a temporary DB containing production-like schema.
- [ ] **T010** Add a "Pre-deploy Backup" step that triggers a Tier 1 snapshot before SSH deployment.

## Phase 5: Monitoring & Recovery
- [ ] **T011** Add a "Backup History" view to the `SuperAdminDashboard`.
- [ ] **T012** Document the disaster recovery procedure (Manual Restore steps).
