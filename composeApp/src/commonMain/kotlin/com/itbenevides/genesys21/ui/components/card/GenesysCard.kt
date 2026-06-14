package com.itbenevides.genesys21.ui.components.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GenesysCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    elevation: Dp = 1.dp,
    shape: Shape = RoundedCornerShape(24.dp), // Aumentado para um look mais moderno
    onClick: (() -> Unit)? = null,
    border: androidx.compose.foundation.BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val finalBorder = border ?: androidx.compose.foundation.BorderStroke(
        width = 1.dp, 
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
    )

    if (onClick != null) {
        Surface(
            modifier = modifier,
            shape = shape,
            color = backgroundColor,
            tonalElevation = elevation,
            onClick = onClick,
            border = finalBorder
        ) {
            Column(modifier = Modifier.padding(16.dp), content = content)
        }
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = backgroundColor,
            tonalElevation = elevation,
            border = finalBorder
        ) {
            Column(modifier = Modifier.padding(16.dp), content = content)
        }
    }
}
