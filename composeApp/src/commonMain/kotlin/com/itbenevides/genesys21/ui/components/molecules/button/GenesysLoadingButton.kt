package com.itbenevides.genesys21.ui.components.molecules.button

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.itbenevides.genesys21.ui.theme.GenesysTheme

/**
 * GenesysLoadingButton: Botão principal integrado aos Design Tokens.
 */
@Composable
fun GenesysLoadingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    containerColor: Color = GenesysTheme.colors.brand,
    contentColor: Color = GenesysTheme.colors.onBrand,
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

    Button(
        onClick = onClick,
        modifier = (if (fillWidth) modifier.fillMaxWidth() else modifier)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .semantics {
                if (isLoading) {
                    contentDescription = "$text, Carregando..."
                }
            },
        enabled = enabled && !isLoading,
        shape = shape ?: RoundedCornerShape(GenesysTheme.config.cornerRadius.dp / 2),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = GenesysTheme.colors.surfaceVariant,
            disabledContentColor = GenesysTheme.colors.onSurfaceVariant
        ),
        interactionSource = interactionSource,
        contentPadding = PaddingValues(horizontal = GenesysTheme.spacing.l, vertical = GenesysTheme.spacing.s)
    ) {
        AnimatedContent(
            targetState = isLoading,
            label = "LoadingButtonAnimation",
            transitionSpec = {
                (fadeIn() + scaleIn()).togetherWith(fadeOut() + scaleOut())
            }
        ) { loading ->
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = contentColor,
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    icon?.let {
                        Icon(it, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(GenesysTheme.spacing.xs))
                    }
                    Text(
                        text = text,
                        style = GenesysTheme.typography.action,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
