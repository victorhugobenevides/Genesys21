package com.itbenevides.genesys21.ui.components.layout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.itbenevides.genesys21.ui.theme.GenesysDimens

@Composable
fun <T> GenesysLazyColumnIndexed(
    items: List<T>,
    modifier: Modifier = Modifier,
    maxWidth: Dp? = null,
    spacing: GenesysSpacing = GenesysSpacing.Medium,
    content: @Composable (Int, T) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        val columnModifier = if (maxWidth != null) modifier.widthIn(max = maxWidth) else modifier.fillMaxWidth()
        
        LazyColumn(
            modifier = columnModifier.fillMaxHeight(),
            contentPadding = PaddingValues(horizontal = GenesysDimens.SpacingLarge, vertical = GenesysDimens.SpacingMedium),
            verticalArrangement = Arrangement.spacedBy(spacing.value)
        ) {
            itemsIndexed(items) { index, item ->
                content(index, item)
            }
        }
    }
}
