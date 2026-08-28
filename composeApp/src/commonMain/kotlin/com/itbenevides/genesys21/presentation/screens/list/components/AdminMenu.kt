package com.itbenevides.genesys21.presentation.screens.list.components

import androidx.compose.ui.graphics.vector.ImageVector
import com.itbenevides.genesys21.domain.model.UserRole
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons

enum class AdminMenuCategory(val label: String) {
    DASHBOARD("DASHBOARD"),
    OPERATIONS("OPERAÇÕES"),
    FINANCIAL("FINANCEIRO"),
    SYSTEM("SISTEMA")
}

data class AdminMenuItem(
    val id: Int,
    val label: String,
    val icon: ImageVector,
    val category: AdminMenuCategory,
    val requiredRole: UserRole = UserRole.MERCHANT,
    val requiredPermission: com.itbenevides.genesys21.domain.model.UserPermission? = null,
    val badgeCount: Int = 0
) {
    companion object {
        // DASHBOARD
        val MainDashboard = AdminMenuItem(0, "Painel Principal", GenesysIcons.Analytics, AdminMenuCategory.DASHBOARD)
        val B2BInsights = AdminMenuItem(9, "Insights Rede", GenesysIcons.BusinessCenter, AdminMenuCategory.DASHBOARD, UserRole.SUPERADMIN)

        // OPERATIONS
        val Vitrines = AdminMenuItem(1, "Vitrines", GenesysIcons.Web, AdminMenuCategory.OPERATIONS, requiredPermission = com.itbenevides.genesys21.domain.model.UserPermission.MANAGE_VITRINES)
        val Orders = AdminMenuItem(2, "Pedidos", GenesysIcons.List, AdminMenuCategory.OPERATIONS, requiredPermission = com.itbenevides.genesys21.domain.model.UserPermission.MANAGE_ORDERS)
        val Agenda = AdminMenuItem(3, "Agenda", GenesysIcons.Schedule, AdminMenuCategory.OPERATIONS, requiredPermission = com.itbenevides.genesys21.domain.model.UserPermission.MANAGE_AGENDA)
        val Services = AdminMenuItem(4, "Serviços", GenesysIcons.Inventory, AdminMenuCategory.OPERATIONS, requiredPermission = com.itbenevides.genesys21.domain.model.UserPermission.MANAGE_SERVICES)

        // FINANCIAL
        val Receipts = AdminMenuItem(5, "Notas Fiscais", GenesysIcons.ReceiptLong, AdminMenuCategory.FINANCIAL, requiredPermission = com.itbenevides.genesys21.domain.model.UserPermission.MANAGE_RECEIPTS)
        val Payments = AdminMenuItem(6, "Pagamentos", GenesysIcons.Payments, AdminMenuCategory.FINANCIAL, requiredPermission = com.itbenevides.genesys21.domain.model.UserPermission.MANAGE_STORE)

        // SYSTEM
        val StoreSettings = AdminMenuItem(10, "Minha Loja", GenesysIcons.Settings, AdminMenuCategory.SYSTEM, requiredPermission = com.itbenevides.genesys21.domain.model.UserPermission.MANAGE_STORE)
        val Profile = AdminMenuItem(8, "Meu Perfil", GenesysIcons.Person, AdminMenuCategory.SYSTEM)
        val GlobalUsers = AdminMenuItem(11, "Usuários Global", GenesysIcons.AdminPanelSettings, AdminMenuCategory.SYSTEM, UserRole.SUPERADMIN)
        val GlobalDomains = AdminMenuItem(12, "Domínios Global", GenesysIcons.Language, AdminMenuCategory.SYSTEM, UserRole.SUPERADMIN)
        val AuditLogs = AdminMenuItem(13, "Logs de Auditoria", GenesysIcons.List, AdminMenuCategory.SYSTEM, UserRole.SUPERADMIN)

        fun getVisibleItems(user: com.itbenevides.genesys21.domain.model.UserProfile?, pendingOrders: Int = 0): List<AdminMenuItem> {
            val role = user?.role ?: UserRole.CUSTOMER
            val permissions = user?.permissions ?: emptySet()

            val all = listOf(
                MainDashboard, B2BInsights,
                Vitrines, Orders.copy(badgeCount = pendingOrders), Agenda, Services,
                Receipts, Payments,
                StoreSettings, Profile, GlobalUsers, GlobalDomains, AuditLogs
            )

            return all.filter { item ->
                // 1. Check Role Hierarchy
                val roleMatch = when (item.requiredRole) {
                    UserRole.SUPERADMIN -> role == UserRole.SUPERADMIN
                    UserRole.ADMIN -> role == UserRole.ADMIN || role == UserRole.SUPERADMIN
                    else -> role != UserRole.CUSTOMER
                }

                // 2. Check Permission if defined
                val permissionMatch = item.requiredPermission == null || permissions.contains(item.requiredPermission)

                // SuperAdmin passes all permission checks
                roleMatch && (permissionMatch || role == UserRole.SUPERADMIN)
            }
        }
    }
}
