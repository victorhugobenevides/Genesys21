package com.itbenevides.genesys21.ui.components.atoms.primitives

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.itbenevides.genesys21.ui.theme.GenesysTheme

/**
 * Espaçador padronizado utilizando a escala de espaçamento do Design System.
 */
@Composable
fun GenesysSpacer(size: Dp = GenesysTheme.spacing.m) {
    Spacer(modifier = Modifier.size(size))
}
