# Spec 009: User Authentication, RBAC, and SuperAdmin Dashboard

## 1. Overview
Implement a comprehensive authentication system using Firebase, coupled with Role-Based Access Control (RBAC). A SuperAdmin dashboard will allow the primary owner to grant merchant privileges to other users. Authentication will be required for both creating pages and completing consumer-facing actions (booking and purchasing).

## 2. User Roles
- **SuperAdmin**: (victorkoto@gmail.com) Can manage all users, grant/revoke Admin/Merchant permissions, and view system-wide stats.
- **Merchant (Admin)**: Can create and manage their own pages, products, services, and view their specific orders/bookings.
- **Customer (Consumer)**: Can view public pages, but must be logged in to finalize a checkout or book a slot.

## 3. Core Requirements

### 3.1 Authentication Gateways
- **For Merchants**: Login via Google or Email/Password to access the Editor and Dashboard.
- **For Customers**: Mandatory login (or "quick sign-up" via Google) during the checkout/booking flow.
- **State Management**: Persist user session across the KMP app using `AuthRepository`.

### 3.2 SuperAdmin Dashboard (Exclusive to victorkoto@gmail.com)
- **User Management UI**: List all registered users.
- **Permission Toggle**: A simple switch to enable "Merchant Mode" for a user (allowing them to create pages).
- **Domain Approval**: Ability to review custom domains requested by merchants.

### 3.3 Auth-Protected Actions
- **Finalize Purchase**: The "Finalizar Pedido" button in the Cart should trigger a login sheet if the user is anonymous.
- **Book Service**: Selecting a time slot in the Booking Engine should require authentication to link the `Appointment` to a user account.
- **Page Editor**: Entire `/editor` route remains protected.

## 4. Technical Implementation

### 4.1 Database Changes (Server)
- **Users Table**: Add `role` (SUPERADMIN, MERCHANT, CUSTOMER) and `isApproved` (Boolean).
- **Audit Logs**: Track who granted permissions to whom.

### 4.2 UseCases (Shared)
- `GetUserProfileUseCase`: Fetch current user data and role.
- `UpdateUserRoleUseCase`: (SuperAdmin only) Change a user's role.
- `GetSystemUsersUseCase`: (SuperAdmin only) Fetch all users for the dashboard.

### 4.3 UI/UX (ComposeApp)
- **SuperAdmin Tab**: A new tab in the Merchant List screen (visible only to SuperAdmin).
- **Auth Interceptor**: A wrapper component `AuthGuard` that shows the Login screen if the user is not authenticated.

## 5. Success Criteria
- [ ] User `victorkoto@gmail.com` logs in and sees an "Admin Panel" tab.
- [ ] SuperAdmin can change a "Customer" user to a "Merchant".
- [ ] A new user can create a page ONLY after SuperAdmin approval.
- [ ] A customer is prompted to log in when clicking "Confirm Booking".
