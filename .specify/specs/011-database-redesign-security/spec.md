# Spec 011: Scalable Database Redesign & Security Hardening

## 1. Overview
The current database structure in Genesys21 grew organically as features were added. While functional, it has some normalization issues, inconsistent ID formats, and limited auditability. This spec proposes a full redesign of the database schema to ensure long-term scalability, strict data integrity, and enhanced security for a multi-tenant (SaaS-like) environment.

## 2. Core Principles
- **Multi-tenancy by Design**: Every record must clearly belong to a `Merchant` or `Store`.
- **Normalization (3NF)**: Eliminate data redundancy while maintaining performance via strategic indexing.
- **Security First**: Use UUIDs to prevent ID enumeration attacks; implement soft deletes for critical data; robust foreign key constraints.
- **Auditability**: Track who did what and when across the entire system.
- **KMP Stability**: Ensure models in `shared` reflect the clean structure and facilitate type-safe serialization.

## 3. Proposed Schema Architecture

### 3.1. Foundation Layer (Auth & Identity)
- **Users**: Core authentication data (Firebase UID, Email, Global Role).
- **Stores (Merchants)**: High-level entity representing a business. A `User` can own one or more `Stores`. This centralizes `whatsapp`, `theme`, and `custom_domain`.
- **Profiles**: Extended user data (Name, Avatar, Phone) linked to a `User`.

### 3.2. Content Layer (CMS)
- **Pages**: Linked to `StoreId`. Contains metadata for the vitrine.
- **Components**: Normalized blocks within a page. Store unstructured content as JSON or specialized tables.
- **Media**: Centralized registry for all uploaded images/files with metadata (size, format, owner).

### 3.3. Commerce Layer
- **Products**: Linked to `StoreId` and `CategoryId`.
- **Categories**: Multi-level support (Parent/Child relationship).
- **Orders**: Linked to `StoreId` and optionally to a registered `UserId` (Customer).
- **OrderItems**: Snapshot of product price and metadata at the time of purchase.

### 4.4. Booking Layer
- **Services**: Specialized commerce entities.
- **Availability**: Weekly configuration and exceptions (holidays).
- **Appointments**: Linkage between `Service`, `Customer`, and `Store`.

## 4. Security & Integrity
- **UUIDs**: All primary keys transition from `Int` or `String` to `UUID`.
- **Soft Deletes**: `deleted_at` timestamp on `Pages`, `Products`, `Services`, and `Orders`.
- **Strict Constraints**: All references must have defined `onDelete` behaviors (Protecting parent records).
- **Audit Columns**: `created_at`, `updated_at`, `created_by`, `updated_by` on every table.
- **Encryption**: Sensitive customer data (if any) should be considered for at-rest encryption.

## 5. Technical Implementation Details
- **Migration Strategy**: Since we are willing to "start from zero", we will implement a `FreshDatabaseInit` script that drops everything and builds the new schema.
- **Database Engine**: Continue with SQLite for now (KMP compatibility), but ensure Exposed SQL usage is dialect-agnostic for future PostgreSQL migration.
- **Shared Models**: Refactor `domain.model` in `shared` to exactly match the new normalized entities.

## 6. Success Criteria
- [ ] New database schema implemented and verified via unit tests.
- [ ] All `owner_id` references replaced by `store_id` (Store-centric model).
- [ ] All IDs are UUIDs.
- [ ] Soft deletes functional on critical entities.
- [ ] Global `AuditLog` captures sensitive state changes (Permissions, Order cancellations).
- [ ] No regression in existing features (Login, Page Editing, Ordering, Booking).
