# Spec 020: Data Resilience & Disaster Recovery

## 1. Overview
This specification defines the strategy to ensure total data durability and recoverability for Genesys21. It aims to protect user creations, edits, and transactional data against software bugs, infrastructure failures, human errors, and malicious attacks.

## 2. Core Pillars

### 2.1. Persistence Guarding
- **Database**: SQLite in WAL (Write-Ahead Logging) mode to handle concurrent access safely.
- **Volumes**: Strategic host-level persistence with daily integrity checks.

### 2.2. Multi-tier Backup Strategy
1.  **Tier 1 (Local/Hourly)**: Periodic snapshots of the SQLite file on the host machine.
2.  **Tier 2 (Off-site/Daily)**: Encrypted upload of the database and `/uploads` folder to a separate cloud provider (e.g., Google Cloud Storage or S3).
3.  **Tier 3 (Git/Schema)**: Flyway migrations for guaranteed schema versioning and rollback capability.

### 2.3. Draft & Work-in-Progress Protection
- **Cloud Drafts**: Move from purely local `localStorage` drafts to an "Auto-save to Cloud" model for authenticated users.
- **Transactional Safety**: Use client-side UUIDs (idempotency) to ensure retried requests don't cause partial or duplicate data states.

## 3. Threat Mitigation

| Threat | Mitigation |
| :--- | :--- |
| **Buggy Deploy** | Automatic Flyway validation + Tier 1 Backup before migration. |
| **VM Failure** | Tier 2 (Off-site) Backup allows full restore on a fresh machine. |
| **Data Corruption** | WAL mode + `PRAGMA integrity_check` integrated into the startup process. |
| **Ransomware** | Read-only backups with 30-day versioning policy (immutability). |

## 4. Technical Requirements

### 4.1. Server Changes
- **Off-site Sync**: A new service to compress and upload data to a cloud bucket.
- **Startup Integrity**: The server MUST verify DB integrity on boot. If corrupted, it MUST refuse to start and notify the SuperAdmin.

### 4.2. Infrastructure Changes
- **CircleCI Guardrails**: Add a "Dry-run Migration" step to the pipeline that clones the production DB structure and runs Flyway.
- **Resource Monitoring**: Monitor disk space and notify if backups fail or space is low.

## 5. Success Criteria
- [ ] Database survives a forced VM deletion (Recovery from Off-site backup).
- [ ] A buggy migration is detected and blocked in the CI pipeline.
- [ ] User drafts are available across different devices (Cloud-sync).
- [ ] Backup logs are visible in the SuperAdmin dashboard.
