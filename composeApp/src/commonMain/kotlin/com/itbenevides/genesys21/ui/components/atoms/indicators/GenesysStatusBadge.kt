package com.itbenevides.genesys21.ui.components.atoms.indicators

import androidx.compose.runtime.Composable
import com.itbenevides.genesys21.domain.model.OrderStatus
import com.itbenevides.genesys21.ui.theme.GenesysTheme

@Composable
fun GenesysStatusBadge(status: OrderStatus) {
    val color =
        when (status) {
            OrderStatus.PENDING -> GenesysTheme.colors.brandContainer
            OrderStatus.AWAITING_PAYMENT -> GenesysTheme.colors.accent
            OrderStatus.PROCESSING -> GenesysTheme.colors.brand
            OrderStatus.COMPLETED -> GenesysTheme.colors.success
            OrderStatus.CANCELLED -> GenesysTheme.colors.error
        }
    val label =
        when (status) {
            OrderStatus.PENDING -> "PENDENTE"
            OrderStatus.AWAITING_PAYMENT -> "AGUARD. PAGAMENTO"
            OrderStatus.PROCESSING -> "EM ANDAMENTO"
            OrderStatus.COMPLETED -> "CONCLUÍDO"
            OrderStatus.CANCELLED -> "CANCELADO"
        }
    GenesysBadge(label = label, color = color)
}
