package com.itbenevides.genesys21.ui.components.atoms.buttons

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.itbenevides.genesys21.ui.theme.GenesysTheme

@Composable
fun GenesysTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    color: Color = GenesysTheme.colors.brand,
    icon: ImageVector? = null,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !isLoading,
    ) {
        AnimatedContent(targetState = isLoading, label = "TextButtonLoading") { loading ->
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(GenesysTheme.spacing.m),
                    strokeWidth = GenesysTheme.spacing.xxxs,
                    color = color,
                )
            } else {
                androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(GenesysTheme.spacing.m),
                            tint = if (enabled) color else color.copy(alpha = 0.38f),
                        )
                        Spacer(Modifier.width(GenesysTheme.spacing.xs))
                    }
                    Text(
                        text = text,
                        fontWeight = FontWeight.Bold,
                        style = GenesysTheme.typography.action,
                        color = if (enabled) color else color.copy(alpha = 0.38f),
                    )
                }
            }
        }
    }
}
