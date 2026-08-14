package com.itbenevides.genesys21.ui.components.molecules.input

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.itbenevides.genesys21.domain.model.OrderStatus
import com.itbenevides.genesys21.ui.components.atoms.indicators.GenesysStatusBadge
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysRow
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.theme.GenesysTheme

@Composable
fun GenesysStatusPicker(
    currentStatus: OrderStatus,
    onStatusSelected: (OrderStatus) -> Unit,
) {
    var showStatusMenu by remember { mutableStateOf(false) }

    Box {
        Surface(
            onClick = { showStatusMenu = true },
            shape = RoundedCornerShape(GenesysTheme.config.cornerRadius),
            color = GenesysTheme.colors.surfaceVariant.copy(alpha = 0.5f),
        ) {
            GenesysRow(fillWidth = false, modifier = Modifier.padding(horizontal = GenesysTheme.spacing.xs, vertical = GenesysTheme.spacing.xxs)) {
                GenesysStatusBadge(currentStatus)
                Icon(
                    imageVector = GenesysIcons.ExpandMore,
                    contentDescription = "Mudar status",
                    tint = GenesysTheme.colors.onSurfaceVariant,
                    modifier = Modifier.padding(start = GenesysTheme.spacing.xxs),
                )
            }
        }

        DropdownMenu(
            expanded = showStatusMenu,
            onDismissRequest = { showStatusMenu = false },
        ) {
            DropdownMenuItem(
                text = { Text("Pendente") },
                onClick = {
                    onStatusSelected(OrderStatus.PENDING)
                    showStatusMenu = false
                },
            )
            DropdownMenuItem(
                text = { Text("Processando") },
                onClick = {
                    onStatusSelected(OrderStatus.PROCESSING)
                    showStatusMenu = false
                },
            )
            DropdownMenuItem(
                text = { Text("Concluído") },
                onClick = {
                    onStatusSelected(OrderStatus.COMPLETED)
                    showStatusMenu = false
                },
            )
            DropdownMenuItem(
                text = { Text("Cancelado", color = Color.Red) },
                onClick = {
                    onStatusSelected(OrderStatus.CANCELLED)
                    showStatusMenu = false
                },
            )
        }
    }
}
