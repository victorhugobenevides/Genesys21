package com.itbenevides.genesys21.ui.components.atoms.primitives

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.ui.theme.GenesysTheme

@Composable
fun GenesysDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color = GenesysTheme.colors.surfaceVariant.copy(alpha = 0.5f),
    usePadding: Boolean = true
) {
    HorizontalDivider(
        modifier = if (usePadding) modifier.padding(vertical = GenesysTheme.spacing.m) else modifier,
        thickness = thickness,
        color = color
    )
}
