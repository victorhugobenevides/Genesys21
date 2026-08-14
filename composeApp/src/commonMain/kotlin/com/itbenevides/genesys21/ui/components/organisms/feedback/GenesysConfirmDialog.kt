package com.itbenevides.genesys21.ui.components.organisms.feedback

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.itbenevides.genesys21.ui.theme.GenesysTheme

@Composable
fun GenesysConfirmDialog(
    onDismissRequest: () -> Unit,
    title: String,
    text: String,
    icon: ImageVector? = null,
    confirmButton: @Composable () -> Unit,
    dismissButton: (@Composable () -> Unit)? = null,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = icon?.let { { Icon(it, null, tint = GenesysTheme.colors.brand, modifier = Modifier.size(GenesysTheme.spacing.xxl)) } },
        title = {
            Text(
                title,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                fontWeight = FontWeight.Bold,
                style = GenesysTheme.typography.title,
            )
        },
        text = {
            Text(
                text,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                style = GenesysTheme.typography.body,
            )
        },
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        shape = RoundedCornerShape(GenesysTheme.config.cornerRadius),
        containerColor = GenesysTheme.colors.surface,
    )
}
