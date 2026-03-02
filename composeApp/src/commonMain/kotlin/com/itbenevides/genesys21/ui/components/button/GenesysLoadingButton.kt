package com.itbenevides.genesys21.ui.components.button

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.ui.components.text.*
import com.itbenevides.genesys21.ui.theme.GenesysDimens

/**
 * GenesysLoadingButton - Botão de ação primária seguindo Material 3.
 * Implementa estados de carregamento, ícones e hierarquia tipográfica padronizada.
 * 
 * @param text Texto do botão
 * @param onClick Callback de clique
 * @param icon Ícone opcional (requer iconContentDescription para acessibilidade)
 * @param iconContentDescription Descrição do ícone para leitores de tela
 */
@Composable
fun GenesysLoadingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    iconContentDescription: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    shape: Shape = RoundedCornerShape(GenesysDimens.CornerRadiusLarge),
    fillWidth: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .height(GenesysDimens.ButtonHeight) // Altura ergonômica padronizada
            .then(if (fillWidth) modifier.fillMaxWidth() else modifier),
        enabled = enabled && !isLoading,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        ),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        AnimatedContent(targetState = isLoading, label = "LoadingButtonAnimation") { loading ->
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 3.dp,
                    color = contentColor
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    icon?.let {
                        Icon(
                            imageVector = it, 
                            contentDescription = iconContentDescription, 
                            modifier = Modifier.size(GenesysDimens.IconMedium)
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    
                    GenesysText(
                        text = text,
                        style = GenesysTextStyle.Label,
                        fontWeight = GenesysFontWeight.Bold,
                        color = contentColor,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
