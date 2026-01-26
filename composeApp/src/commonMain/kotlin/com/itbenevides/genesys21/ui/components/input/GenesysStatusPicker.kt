package com.itbenevides.genesys21.ui.components.input

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.itbenevides.genesys21.domain.model.OrderStatus
import com.itbenevides.genesys21.ui.components.badge.GenesysStatusBadge
import com.itbenevides.genesys21.ui.components.layout.GenesysRow
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons

@Composable
fun GenesysStatusPicker(
    currentStatus: OrderStatus,
    onStatusSelected: (OrderStatus) -> Unit
) {
    var showStatusMenu by remember { mutableStateOf(false) }

    Box {
        Surface(
            onClick = { showStatusMenu = true },
            shape = MaterialTheme.shapes.small,
            color = Color.Transparent
        ) {
            GenesysRow {
                GenesysStatusBadge(currentStatus)
                Icon(GenesysIcons.ExpandMore, contentDescription = "Mudar status", tint = Color.Gray)
            }
        }
        
        DropdownMenu(
            expanded = showStatusMenu,
            onDismissRequest = { showStatusMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Pendente") },
                onClick = {
                    onStatusSelected(OrderStatus.PENDING)
                    showStatusMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text("Processando") },
                onClick = {
                    onStatusSelected(OrderStatus.PROCESSING)
                    showStatusMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text("Concluído") },
                onClick = {
                    onStatusSelected(OrderStatus.COMPLETED)
                    showStatusMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text("Cancelado", color = Color.Red) },
                onClick = {
                    onStatusSelected(OrderStatus.CANCELLED)
                    showStatusMenu = false
                }
            )
        }
    }
}
