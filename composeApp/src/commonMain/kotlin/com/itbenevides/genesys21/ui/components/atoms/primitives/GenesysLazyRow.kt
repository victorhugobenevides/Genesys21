package com.itbenevides.genesys21.ui.components.atoms.primitives

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.itbenevides.genesys21.ui.theme.GenesysDimens
import com.itbenevides.genesys21.ui.theme.GenesysTheme

/**
 * Listagem horizontal padronizada.
 */
@Composable
fun <T> GenesysLazyRow(
    items: List<T>,
    modifier: Modifier = Modifier,
    spacing: Dp = GenesysTheme.spacing.m,
    content: @Composable (T) -> Unit,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(spacing),
        contentPadding = PaddingValues(vertical = GenesysDimens.SpacingSmall),
    ) {
        items(items) { item ->
            content(item)
        }
    }
}
