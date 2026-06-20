package com.itbenevides.genesys21.ui.components.layout

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.itbenevides.genesys21.ui.theme.GenesysDimens

/**
 * Listagem horizontal padronizada.
 */
@Composable
fun <T> GenesysLazyRow(
    items: List<T>,
    modifier: Modifier = Modifier,
    spacing: GenesysSpacing = GenesysSpacing.Medium,
    content: @Composable (T) -> Unit,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(spacing.value),
        contentPadding = PaddingValues(vertical = GenesysDimens.SpacingSmall),
    ) {
        items(items) { item ->
            content(item)
        }
    }
}
