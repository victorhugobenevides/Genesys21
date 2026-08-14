package com.itbenevides.genesys21.ui.components.molecules.card

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.ui.theme.GenesysMotion
import com.itbenevides.genesys21.ui.theme.GenesysTheme

/**
 * GenesysCard: Componente de container principal.
 * Usa tokens semânticos para background, borda e espaçamento.
 */
@Composable
fun GenesysCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = GenesysTheme.colors.surface,
    elevation: Dp = 1.dp,
    shape: Shape? = null,
    onClick: (() -> Unit)? = null,
    border: androidx.compose.foundation.BorderStroke? = null,
    usePadding: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val finalShape = shape ?: RoundedCornerShape(GenesysTheme.config.cornerRadius.dp)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.98f else 1f,
        animationSpec = GenesysMotion.interactiveSpring,
        label = "CardScale"
    )

    val finalBorder =
        border ?: androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = GenesysTheme.colors.outline.copy(alpha = 0.1f),
        )

    Surface(
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        shape = finalShape,
        color = backgroundColor,
        tonalElevation = if (isPressed) elevation * 2 else elevation,
        onClick = onClick ?: {},
        enabled = onClick != null,
        border = finalBorder,
        interactionSource = interactionSource
    ) {
        Column(
            modifier = if (usePadding) Modifier.padding(GenesysTheme.spacing.m) else Modifier,
            content = content
        )
    }
}
