package com.itbenevides.genesys21.ui.components.atoms.indicators

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun GenesysStockBadge(
    stock: Int,
    modifier: Modifier = Modifier,
) {
    val (label, color) =
        when {
            stock <= 0 -> "Esgotado" to MaterialTheme.colorScheme.error
            stock < 5 -> "Restam apenas $stock!" to MaterialTheme.colorScheme.error
            else -> "Estoque: $stock" to MaterialTheme.colorScheme.secondary
        }
    GenesysBadge(label = label, color = color, showDot = true, modifier = modifier)
}
