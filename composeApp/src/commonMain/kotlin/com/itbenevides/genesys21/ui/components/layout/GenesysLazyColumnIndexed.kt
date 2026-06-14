package com.itbenevides.genesys21.ui.components.layout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.ui.theme.GenesysDimens

@Composable
fun <T> GenesysLazyColumnIndexed(
    items: List<T>,
    modifier: Modifier = Modifier,
    maxWidth: Dp? = null,
    spacing: GenesysSpacing = GenesysSpacing.Medium,
    usePadding: Boolean = true,
    key: ((Int, T) -> Any)? = null,
    itemModifier: ((Int, T) -> Modifier)? = null,
    content: @Composable (Int, T) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        val columnModifier = if (maxWidth != null) modifier.widthIn(max = maxWidth) else modifier.fillMaxWidth()

        // Responsividade: Reduz padding em telas estreitas (mobile)
        val horizontalPadding = if (this.maxWidth < 600.dp) GenesysDimens.SpacingMedium else GenesysDimens.SpacingLarge

        LazyColumn(
            modifier = columnModifier.fillMaxHeight(),
            contentPadding =
                if (usePadding) {
                    PaddingValues(horizontal = horizontalPadding, vertical = GenesysDimens.SpacingMedium)
                } else {
                    PaddingValues(0.dp)
                },
            verticalArrangement = Arrangement.spacedBy(if (usePadding) spacing.value else 0.dp),
        ) {
            itemsIndexed(
                items = items,
                key = key,
            ) { index, item ->
                val baseModifier = itemModifier?.invoke(index, item) ?: Modifier
                Box(modifier = baseModifier) {
                    content(index, item)
                }
            }
        }
    }
}
