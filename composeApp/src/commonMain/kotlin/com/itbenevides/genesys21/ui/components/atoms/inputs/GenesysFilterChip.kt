package com.itbenevides.genesys21.ui.components.atoms.inputs

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.itbenevides.genesys21.ui.theme.GenesysMotion
import com.itbenevides.genesys21.ui.theme.GenesysTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenesysFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0,
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) GenesysTheme.colors.brand else Color.Transparent,
        animationSpec = GenesysMotion.colorSpring,
        label = "chipColor",
    )

    val labelColor by animateColorAsState(
        targetValue = if (selected) GenesysTheme.colors.onBrand else GenesysTheme.colors.onSurfaceVariant,
        animationSpec = GenesysMotion.colorSpring,
        label = "labelColor",
    )

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = GenesysTheme.typography.label,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            )
            if (badgeCount > 0) {
                Surface(
                    color =
                        if (selected) {
                            GenesysTheme.colors.onBrand.copy(
                                alpha = 0.2f,
                            )
                        } else {
                            GenesysTheme.colors.brandContainer
                        },
                    shape = CircleShape,
                    modifier = Modifier.padding(start = GenesysTheme.spacing.xs),
                ) {
                    Text(
                        text = badgeCount.toString(),
                        style = GenesysTheme.typography.label,
                        modifier = Modifier.padding(horizontal = GenesysTheme.spacing.xxs, vertical = GenesysTheme.spacing.xxxs),
                        color = if (selected) GenesysTheme.colors.onBrand else GenesysTheme.colors.onBrandContainer,
                    )
                }
            }
        },
        shape = CircleShape,
        modifier = modifier.animateContentSize(),
        colors =
            FilterChipDefaults.filterChipColors(
                selectedContainerColor = containerColor,
                selectedLabelColor = labelColor,
                containerColor = containerColor,
                labelColor = labelColor,
            ),
        border =
            FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = selected,
                borderColor = if (selected) Color.Transparent else GenesysTheme.colors.outline.copy(alpha = 0.3f),
                selectedBorderColor = Color.Transparent,
            ),
    )
}
