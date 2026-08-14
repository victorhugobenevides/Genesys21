package com.itbenevides.genesys21.ui.components.molecules.button

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.ui.theme.GenesysMotion

/**
 * GenesysLoadingButton: Follows M3 principles for interaction and motion.
 * Added scale animation on press and refined loading transition.
 */
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = GenesysMotion.interactiveSpring,
        label = "ButtonScale"
    )

    val loadingText = "Carregando..."

    Button(
        onClick = onClick,
        modifier = (if (fillWidth) modifier.fillMaxWidth() else modifier)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .semantics {
                if (isLoading) {
                    contentDescription = "$text, $loadingText"
                }
            },
        enabled = enabled && !isLoading,
        shape = shape ?: MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        interactionSource = interactionSource
    ) {
        AnimatedContent(
            targetState = isLoading,
            label = "LoadingButtonAnimation",
            transitionSpec = {
                (fadeIn() + expandIn(expandFrom = Alignment.Center)).togetherWith(fadeOut() + shrinkOut(shrinkTowards = Alignment.Center))
            }
        ) { loading ->
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
