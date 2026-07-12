package com.itbenevides.genesys21.ui.components.molecules.button

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun GenesysLoadingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    shape: Shape? = null,
    fillWidth: Boolean = false,
) {
    Button(
        onClick = onClick,
        modifier = if (fillWidth) modifier.fillMaxWidth() else modifier,
        enabled = enabled && !isLoading,
        shape = shape ?: MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
    ) {
        AnimatedContent(targetState = isLoading, label = "LoadingButtonAnimation") { loading ->
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    icon?.let {
                        Icon(it, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(text, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
