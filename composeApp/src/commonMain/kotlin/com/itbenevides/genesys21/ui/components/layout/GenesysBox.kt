package com.itbenevides.genesys21.ui.components.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Container base padronizado para o DS Genesys21.
 */
@Composable
fun GenesysBox(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = contentAlignment,
        content = content
    )
}
