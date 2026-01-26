package com.itbenevides.genesys21.ui.components.layout

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Espaçador padronizado utilizando a escala de espaçamento do Design System.
 */
@Composable
fun GenesysSpacer(size: GenesysSpacing = GenesysSpacing.Medium) {
    Spacer(modifier = Modifier.size(size.value))
}
