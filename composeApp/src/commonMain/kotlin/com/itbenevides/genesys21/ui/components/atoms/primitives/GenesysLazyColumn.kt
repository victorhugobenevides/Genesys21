package com.itbenevides.genesys21.ui.components.atoms.primitives

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.itbenevides.genesys21.ui.theme.GenesysDimens

@Composable
fun <T> GenesysLazyColumn(
    items: List<T>,
    modifier: Modifier = Modifier,
    maxWidth: Dp? = null,
    usePadding: Boolean = true,
    spacing: GenesysSpacing = GenesysSpacing.Medium,
    content: @Composable (T) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        val columnModifier =
            modifier
                .fillMaxHeight()
                .then(if (maxWidth != null) Modifier.widthIn(max = maxWidth) else Modifier.fillMaxWidth())
                .then(if (usePadding) Modifier.padding(horizontal = GenesysDimens.SpacingLarge) else Modifier)

        LazyColumn(
            modifier = columnModifier,
            contentPadding = PaddingValues(vertical = GenesysDimens.SpacingMedium),
            verticalArrangement = Arrangement.spacedBy(spacing.value),
        ) {
            items(items) { item ->
                content(item)
            }
        }
    }
}
