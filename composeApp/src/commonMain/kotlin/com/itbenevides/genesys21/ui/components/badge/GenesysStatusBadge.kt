package com.itbenevides.genesys21.ui.components.badge

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.itbenevides.genesys21.domain.model.OrderStatus

@Composable
fun GenesysStatusBadge(status: OrderStatus) {
    val color =
        when (status) {
            OrderStatus.PENDING -> Color(0xFFFBC02D)
            OrderStatus.PROCESSING -> Color(0xFF1976D2)
            OrderStatus.COMPLETED -> Color(0xFF388E3C)
            OrderStatus.CANCELLED -> Color(0xFFD32F2F)
        }
    val label =
        when (status) {
            OrderStatus.PENDING -> "PENDENTE"
            OrderStatus.PROCESSING -> "EM ANDAMENTO"
            OrderStatus.COMPLETED -> "CONCLUÍDO"
            OrderStatus.CANCELLED -> "CANCELADO"
        }
    GenesysBadge(label = label, color = color)
}
