package com.itbenevides.genesys21.ui.components.atoms.indicators

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.ui.theme.GenesysTheme

@Composable
fun GenesysLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = GenesysTheme.colors.brand,
    strokeWidth: Dp = 3.dp
) {
    CircularProgressIndicator(
        modifier = modifier,
        color = color,
        strokeWidth = strokeWidth
    )
}

/**
 * Overlay de carregamento que bloqueia a tela.
 */
@Composable
fun GenesysLoadingOverlay(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Black.copy(alpha = 0.5f)
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .clickable(enabled = false) { }, // Bloqueia toques no fundo
        contentAlignment = Alignment.Center
    ) {
        GenesysLoadingIndicator()
    }
}
