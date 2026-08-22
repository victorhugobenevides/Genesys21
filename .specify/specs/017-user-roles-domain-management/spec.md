# Spec 017: User Roles & Global Domain Management

## 1. Overview
This specification defines a multi-level user management system and a centralized custom domain registry. It allows for secure administrative scaling and gives Super Admins the power to map external domains directly to specific user vitrines.

## 2. User Roles Hierarchy

| Role | Access Level | Description |
| :--- | :--- | :--- |
| **CUSTOMER** | Low | Default role. Access to public pages, order history, and personal profile. |
| **MERCHANT** | Medium | Can manage their own Store, Vitrines, Products, Services, and Orders. |
| **ADMIN** | High | System-level intermediate role. Can manage multiple merchants but not global settings. |
| **SUPERADMIN** | Full | Total control. Access to the SuperAdmin panel, global permissions, and domain mappings. |

## 3. Global Domain Management
Super Admins require a centralized way to handle "White Label" deployments where a domain like `client-store.com` must resolve to a specific `pageId` in Genesys21.

### 3.1. Domain Mapping Registry
A new table will store these global mappings:
- `id` (UUID): Unique mapping identifier.
- `domain` (String, Unique): The full domain name (e.g., `shop.victorben.dev`).
- `targetPageId` (String): The UUID of the Page to render.
- `createdAt` / `updatedAt`: Timestamps for auditing.

## 4. Feature Requirements

### 4.1. Secure Role Escalation
- Only a **SUPERADMIN** can promote a user to **ADMIN** or **SUPERADMIN**.
- An **ADMIN** can promote a **CUSTOMER** to **MERCHANT**.

### 4.2. SuperAdmin Domain Portal
- A new tab/section in the `SuperAdminDashboard` to list, create, and delete domain mappings.
- Validation to ensure a domain isn't mapped twice.

### 4.3. High-Performance Resolver
- The server's domain resolution logic (used for landing on pages via hostname) must check the `DomainMappings` table.
- Cache mappings in memory (Redis or internal map) to avoid DB hits on every request.

## 5. Security & Hardening
- **Route Protection**: All `/api/admin/system/*` routes must be restricted to users with `UserRole.SUPERADMIN`.
- **JWT Verification**: Ensure the `role` and `permissions` claims are verified server-side on every request.
- **Audit Logs**: Every role change or domain mapping update must be logged in `AuditLogsTable`.

## 6. Success Criteria
- [ ] Merchant cannot promote themselves or others to Admin.
- [ ] A custom domain registered by a SuperAdmin correctly renders the target page.
- [ ] Audit logs show the trace of who assigned a domain to a page.
- [ ] User role hierarchy is consistently applied across the Compose UI (hiding/showing tabs).
