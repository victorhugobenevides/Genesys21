package com.itbenevides.genesys21.ui.components.molecules.layout

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.itbenevides.genesys21.ui.theme.GenesysTheme

@Composable
fun GenesysSectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = GenesysTheme.typography.title,
                fontWeight = FontWeight.Bold,
            )
            subtitle?.let {
                Text(
                    it,
                    style = GenesysTheme.typography.bodySmall,
                    color = GenesysTheme.colors.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
        }
        trailingContent?.invoke()
    }
}
