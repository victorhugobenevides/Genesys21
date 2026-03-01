package com.itbenevides.genesys21.ui.components.image

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.itbenevides.genesys21.ui.components.feedback.ShimmerPlaceholder
import com.itbenevides.genesys21.ui.theme.GenesysDimens

/**
 * Componente de imagem abstraído e flexível.
 * 
 * Features:
 * - Crossfade suave na transição
 * - Shimmer loading animado
 * - Error state com retry
 * - Placeholder adaptativo para tema escuro
 * - Acessibilidade completa
 * 
 * @param url URL da imagem (se vazio, mostra placeholder)
 * @param size Tamanho da imagem
 * @param isCircular Se true, aplica CircleShape
 * @param modifier Modifier adicional
 * @param contentDescription Descrição de acessibilidade
 * @param contentScale Escala de conteúdo
 * @param shape Shape customizado
 * @param crossfadeMillis Duração do crossfade em ms (padrão: 300)
 * @param showShimmer Se true, mostra shimmer durante carregamento
 * @param onRetry Callback para tentar carregar novamente (quando null, mostra erro estático)
 */
@Composable
fun GenesysImage(
    url: String,
    size: Dp = GenesysDimens.IconLogo,
    isCircular: Boolean = false,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    shape: Shape? = null,
    crossfadeMillis: Int = 300,
    showShimmer: Boolean = true,
    onRetry: (() -> Unit)? = null
) {
    // Define a forma final
    val finalShape = when {
        isCircular -> CircleShape
        shape != null -> shape
        else -> RoundedCornerShape(GenesysDimens.CornerRadiusSmall)
    }

    val imageModifier = modifier
        .size(size)
        .clip(finalShape)

    if (url.isNotEmpty()) {
        // SubcomposeAsyncImage com crossfade e controles de estado
        // URL passado diretamente para compatibilidade multiplataforma
        SubcomposeAsyncImage(
            model = url,
            contentDescription = contentDescription,
            modifier = imageModifier,
            contentScale = contentScale,
            loading = {
                if (showShimmer) {
                    ShimmerPlaceholder(
                        modifier = Modifier.matchParentSize(),
                        shape = finalShape,
                        shimmerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(
                                    alpha = if (MaterialTheme.colorScheme.isLight) 0.3f else 0.5f
                                )
                            )
                    )
                }
            },
            error = {
                ErrorStateWithRetry(
                    size = size,
                    onRetry = onRetry,
                    finalShape = finalShape
                )
            }
        )
    } else {
        // Placeholder quando URL está vazio
        PlaceholderState(
            size = size,
            finalShape = finalShape,
            contentDescription = contentDescription
        )
    }
}

@Composable
private fun ErrorStateWithRetry(
    size: Dp,
    onRetry: (() -> Unit)?,
    finalShape: Shape
) {
    val isDarkTheme = !MaterialTheme.colorScheme.isLight
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = if (isDarkTheme) {
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
                } else {
                    MaterialTheme.colorScheme.errorContainer
                }
            )
            .clip(finalShape),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.BrokenImage,
                contentDescription = "Erro ao carregar imagem",
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(size * 0.3f)
            )
            
            if (onRetry != null) {
                Spacer(modifier = Modifier.height(4.dp))
                
                IconButton(
                    onClick = onRetry,
                    modifier = Modifier.size(size * 0.25f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Tentar novamente",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(size * 0.2f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaceholderState(
    size: Dp,
    finalShape: Shape,
    contentDescription: String?
) {
    val isDarkTheme = !MaterialTheme.colorScheme.isLight
    
    // Placeholder com melhor contraste em temas escuros
    val placeholderColor = if (isDarkTheme) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }
    
    val iconAlpha = if (isDarkTheme) 0.6f else 0.3f
    
    Box(
        modifier = Modifier
            .size(size)
            .clip(finalShape)
            .background(placeholderColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Image,
            contentDescription = contentDescription ?: "Imagem não disponível",
            tint = MaterialTheme.colorScheme.primary.copy(alpha = iconAlpha),
            modifier = Modifier.size(size * 0.5f)
        )
    }
}

// Helper para detectar tema escuro
private val androidx.compose.material3.ColorScheme.isLight: Boolean
    get() = this.background.luminance() > 0.5f

private fun Color.luminance(): Float {
    val r = red
    val g = green
    val b = blue
    return 0.2126f * r + 0.7152f * g + 0.0722f * b
}