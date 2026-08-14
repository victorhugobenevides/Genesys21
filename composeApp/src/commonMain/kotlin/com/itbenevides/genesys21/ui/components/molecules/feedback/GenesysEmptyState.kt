package com.itbenevides.genesys21.ui.components.molecules.feedback

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.itbenevides.genesys21.ui.theme.GenesysTheme

@Composable
fun GenesysEmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(GenesysTheme.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            icon,
            null,
            modifier = Modifier.size(GenesysTheme.spacing.huge),
            tint = GenesysTheme.colors.brand.copy(alpha = 0.2f),
        )
        Spacer(Modifier.height(GenesysTheme.spacing.m))
        Text(
            title,
            style = GenesysTheme.typography.headline,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            description,
            style = GenesysTheme.typography.body,
            color = GenesysTheme.colors.onSurfaceVariant.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = GenesysTheme.spacing.xs),
        )
        action?.let {
            Spacer(Modifier.height(GenesysTheme.spacing.l))
            it()
        }
    }
}
