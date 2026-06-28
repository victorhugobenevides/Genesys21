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
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.ui.theme.GenesysMotion

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
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = GenesysMotion.colorSpring,
        label = "chipColor",
    )

    val labelColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = GenesysMotion.colorSpring,
        label = "labelColor",
    )

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            )
            if (badgeCount > 0) {
                Surface(
                    color =
                        if (selected) {
                            MaterialTheme.colorScheme.onPrimary.copy(
                                alpha = 0.2f,
                            )
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        },
                    shape = CircleShape,
                    modifier = Modifier.padding(start = 8.dp),
                ) {
                    Text(
                        text = badgeCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
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
                borderColor = if (selected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                selectedBorderColor = Color.Transparent,
            ),
    )
}
