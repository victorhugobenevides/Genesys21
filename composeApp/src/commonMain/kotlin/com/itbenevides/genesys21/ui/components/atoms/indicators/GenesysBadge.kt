package com.itbenevides.genesys21.ui.components.atoms.indicators

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.itbenevides.genesys21.ui.theme.GenesysTheme

@Composable
fun GenesysBadge(
    label: String,
    color: Color,
    showDot: Boolean = true,
    modifier: Modifier = Modifier,
    textColor: Color? = null,
) {
    val finalTextColor = textColor ?: color
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(GenesysTheme.spacing.xxs),
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = GenesysTheme.spacing.xs, vertical = GenesysTheme.spacing.xxs),
        ) {
            if (showDot) {
                Box(Modifier.size(GenesysTheme.spacing.xxs).background(finalTextColor, CircleShape))
                Spacer(Modifier.width(GenesysTheme.spacing.xxs))
            }
            Text(
                label.uppercase(),
                color = finalTextColor,
                style =
                    GenesysTheme.typography.label.copy(
                        fontWeight = FontWeight.Bold,
                    ),
            )
        }
    }
}
