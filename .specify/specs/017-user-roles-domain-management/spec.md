# Spec 017: User Roles, Global Domain & Anonymous Flow

## 1. Overview
This specification defines a comprehensive user management system, a centralized domain registry, and a privacy-first anonymous checkout flow. It also introduces a lightweight internal chat for order/appointment management.

## 2. User Roles Hierarchy

| Role | Access Level | Description |
| :--- | :--- | :--- |
| **ANONYMOUS** | Transient | Session-based. Access to public pages, single order tracking via Nick. |
| **CUSTOMER** | Low | Registered user. Persistent order history and profile. |
| **MERCHANT** | Medium | Can manage their own Store, Vitrines, Products, and Orders. |
| **ADMIN** | High | System-level intermediate. Manages multiple merchants. |
| **SUPERADMIN** | Full | Total control. Global permissions and domain mappings. |

## 3. Global Domain Management
Super Admins manage mappings between external hostnames and internal page IDs.
- **Resolver**: The server prioritizes the `DomainMappings` table when an unknown hostname is detected.
- **White Label**: Allows `client-site.com` to render a specific Genesys21 page directly.

## 4. Anonymous Flow & Privacy
- **Nick-only identity**: Anonymous users provide only a "Nick" for identification.
- **Transient Data**: Address data (for delivery or local service) is stored **only** within the `Order` or `Appointment` record. It is never linked to a persistent user profile.
- **Authentication**: Access to order tracking for anonymous users is validated via `sessionId` and `orderId`.

## 5. Communication: Internal Chat Engine
To facilitate trust between Anonymous users and Merchants, an integrated chat system is required.
- **WhatsApp**: Mandatory fallback for delivery/local services (even for anonymous) to ensure logistic reliability.
- **Internal Chat**:
    - Lightweight messaging system tied to `orderId` or `appointmentId`.
    - **Persistence**: Messages for Anonymous users live only as long as the Order/Appointment is active.
    - **Architecture**: Implemented via **HTTP Long Polling** for maximum WasmJS compatibility.

## 6. Data Structures

### 6.1. Domain Mapping
- `domain` (String, Unique)
- `targetPageId` (String)

### 6.2. Lightweight Chat (Messages)
- `id` (UUID)
- `refId` (Order ID or Appointment ID)
- `senderNick` (String)
- `content` (String)
- `isFromMerchant` (Boolean)
- `createdAt` (Long)

## 7. Success Criteria
- [ ] Registered domain resolves correctly to the target page.
- [ ] Anonymous user can complete a checkout using only a Nick.
- [ ] Address data for anonymous users does not persist outside the Order.
- [ ] Internal chat works for both registered and anonymous users via the tracking page.
