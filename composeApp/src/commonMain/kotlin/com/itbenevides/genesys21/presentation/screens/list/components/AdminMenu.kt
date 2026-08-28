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
    val badgeCount: Int = 0
) {
    companion object {
        // DASHBOARD
        val MainDashboard = AdminMenuItem(0, "Painel Principal", GenesysIcons.Analytics, AdminMenuCategory.DASHBOARD)
        val B2BInsights = AdminMenuItem(9, "Insights Rede", GenesysIcons.BusinessCenter, AdminMenuCategory.DASHBOARD, UserRole.SUPERADMIN)

        // OPERATIONS
        val Vitrines = AdminMenuItem(1, "Vitrines", GenesysIcons.Web, AdminMenuCategory.OPERATIONS)
        val Orders = AdminMenuItem(2, "Pedidos", GenesysIcons.List, AdminMenuCategory.OPERATIONS)
        val Agenda = AdminMenuItem(3, "Agenda", GenesysIcons.Schedule, AdminMenuCategory.OPERATIONS)
        val Services = AdminMenuItem(4, "Serviços", GenesysIcons.Inventory, AdminMenuCategory.OPERATIONS)

        // FINANCIAL
        val Receipts = AdminMenuItem(5, "Notas Fiscais", GenesysIcons.ReceiptLong, AdminMenuCategory.FINANCIAL)
        val Payments = AdminMenuItem(6, "Pagamentos", GenesysIcons.Payments, AdminMenuCategory.FINANCIAL)

        // SYSTEM
        val StoreSettings = AdminMenuItem(10, "Minha Loja", GenesysIcons.Settings, AdminMenuCategory.SYSTEM)
        val Profile = AdminMenuItem(8, "Meu Perfil", GenesysIcons.Person, AdminMenuCategory.SYSTEM)
        val GlobalControl = AdminMenuItem(7, "Controle Global", GenesysIcons.AdminPanelSettings, AdminMenuCategory.SYSTEM, UserRole.SUPERADMIN)

        fun getVisibleItems(role: UserRole, pendingOrders: Int = 0): List<AdminMenuItem> {
            val all = listOf(
                MainDashboard, B2BInsights,
                Vitrines, Orders.copy(badgeCount = pendingOrders), Agenda, Services,
                Receipts, Payments,
                StoreSettings, Profile, GlobalControl
            )

            // Verificação de hierarquia simplificada (ordinais)
            return all.filter {
                when (it.requiredRole) {
                    UserRole.SUPERADMIN -> role == UserRole.SUPERADMIN
                    UserRole.ADMIN -> role == UserRole.ADMIN || role == UserRole.SUPERADMIN
                    else -> true // Merchant/Customer (embora customer nem devesse estar aqui)
                }
            }
        }
    }
}
