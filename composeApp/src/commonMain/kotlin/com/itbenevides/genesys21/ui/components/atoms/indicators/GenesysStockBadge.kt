package com.itbenevides.genesys21.ui.components.atoms.indicators

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.itbenevides.genesys21.ui.theme.GenesysTheme

@Composable
fun GenesysStockBadge(
    stock: Int,
    modifier: Modifier = Modifier,
) {
    val (label, color) =
        when {
            stock <= 0 -> "Esgotado" to GenesysTheme.colors.error
            stock < 5 -> "Restam apenas $stock!" to GenesysTheme.colors.error
            else -> "Estoque: $stock" to GenesysTheme.colors.brand
        }
    GenesysBadge(label = label, color = color, showDot = true, modifier = modifier)
}
