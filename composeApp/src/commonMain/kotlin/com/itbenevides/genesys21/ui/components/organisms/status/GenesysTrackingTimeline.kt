package com.itbenevides.genesys21.ui.components.organisms.status

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.OrderStatus
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysColumn
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacer
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacing
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysFontWeight
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysTextStyle
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons

@Composable
fun GenesysTrackingTimeline(currentStatus: OrderStatus) {
    val steps =
        listOf(
            OrderStatus.PENDING to "Pedido Recebido",
            OrderStatus.PROCESSING to "Em Preparação",
            OrderStatus.COMPLETED to "Pedido Concluído",
        )

    GenesysColumn(usePadding = false) {
        steps.forEachIndexed { index, (status, label) ->
            val isCompleted =
                when (currentStatus) {
                    OrderStatus.CANCELLED -> false
                    OrderStatus.PENDING -> index == 0
                    OrderStatus.PROCESSING -> index <= 1
                    OrderStatus.COMPLETED -> true
                }

            val isActive =
                when (currentStatus) {
                    OrderStatus.PENDING -> index == 0
                    OrderStatus.PROCESSING -> index == 1
                    OrderStatus.COMPLETED -> index == 2
                    else -> false
                }

            val color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier =
                            Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isCompleted) color else color.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isCompleted && !isActive) {
                            Icon(GenesysIcons.Check, null, modifier = Modifier.size(16.dp), tint = Color.White)
                        } else if (isActive) {
                            Box(Modifier.size(10.dp).background(Color.White, CircleShape))
                        }
                    }
                    if (index < steps.size - 1) {
                        Box(
                            Modifier
                                .width(2.dp)
                                .height(32.dp)
                                .background(if (isCompleted && currentStatus != status) color else color.copy(alpha = 0.3f)),
                        )
                    }
                }

                GenesysSpacer(GenesysSpacing.Medium)

                GenesysText(
                    text = label,
                    style = GenesysTextStyle.Body,
                    fontWeight = if (isActive) GenesysFontWeight.ExtraBold else null,
                    color = if (isCompleted) Color.Unspecified else Color.Gray,
                )
            }
        }
    }
}
