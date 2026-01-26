package com.itbenevides.genesys21.ui.components.image

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
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
import coil3.compose.AsyncImage
import com.itbenevides.genesys21.ui.theme.GenesysDimens

/**
 * Componente de imagem abstraído.
 * Suporta definição de tamanho e forma para manter o isolamento da Presentation.
 */
@Composable
fun GenesysImage(
    url: String,
    size: Dp = GenesysDimens.IconLogo,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    shape: Shape = RoundedCornerShape(GenesysDimens.CornerRadiusSmall),
    placeholderColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
) {
    val imageModifier = modifier
        .size(size)
        .clip(shape)

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
                modifier = Modifier.size(GenesysDimens.IconHuge)
            )
        }
    }
}
