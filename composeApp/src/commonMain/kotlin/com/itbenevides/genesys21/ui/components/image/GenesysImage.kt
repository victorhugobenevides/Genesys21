package com.itbenevides.genesys21.ui.components.image

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.itbenevides.genesys21.ui.theme.GenesysDimens

/**
 * Componente de imagem abstraído e flexível.
 * Suporta definição de tamanho e opção circular (redonda).
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
    placeholderColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
) {
    // Define a forma final: Circular ou Arredondada (baseada no DS)
    val finalShape = when {
        isCircular -> CircleShape
        shape != null -> shape
        else -> RoundedCornerShape(GenesysDimens.CornerRadiusSmall)
    }

    val imageModifier = modifier
        .size(size)
        .clip(finalShape)

    if (url.isNotEmpty()) {
        AsyncImage(
            model = url,
            contentDescription = contentDescription,
            modifier = imageModifier,
            contentScale = contentScale
        )
    } else {
        Box(
            modifier = imageModifier.background(placeholderColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                modifier = Modifier.size(size * 0.5f) // Ícone proporcional ao tamanho
            )
        }
    }
}
