package com.itbenevides.genesys21.ui.components.image

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImage
import com.itbenevides.genesys21.ui.theme.GenesysDimens
import com.itbenevides.genesys21.ui.util.shimmerBrush

/**
 * Componente de imagem abstraído e flexível.
 * Suporta definição de tamanho e opção circular (redonda) e efeito shimmer.
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
    placeholderColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    showShimmer: Boolean = true,
) {
    var isLoading by remember { mutableStateOf(true) }

    // Define a forma final: Circular ou Arredondada (baseada no DS)
    val finalShape =
        when {
            isCircular -> CircleShape
            shape != null -> shape
            else -> RoundedCornerShape(GenesysDimens.CornerRadiusSmall)
        }

    val imageModifier =
        modifier
            .size(size)
            .clip(finalShape)

    Box(modifier = imageModifier) {
        if (url.isNotEmpty()) {
            AsyncImage(
                model = url,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
                onLoading = { isLoading = true },
                onSuccess = { isLoading = false },
                onError = { isLoading = false },
            )

            if (isLoading && showShimmer) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(shimmerBrush()),
                )
            }
        } else {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(placeholderColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    modifier = Modifier.size(size * 0.5f),
                )
            }
        }
    }
}
