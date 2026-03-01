package com.itbenevides.genesys21.ui.components.card

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.ui.theme.CardPadding
import com.itbenevides.genesys21.ui.theme.GenesysDimens
import com.itbenevides.genesys21.ui.theme.GenesysPadding

/**
 * GenesysCard - Container padronizado seguindo Material 3.
 * 
 * Features:
 * - Tonal Elevation adaptativo (mais claro em temas escuros)
 * - Padding semântico (Small/Medium/Large)
 * - Estados de clique com ripple
 * - Consistência visual garantida
 * 
 * @param modifier Modifier adicional
 * @param backgroundColor Cor de fundo
 * @param elevation Tonal elevation (adaptativo por padrão)
 * @param shape Shape do card
 * @param size Tamanho do card (afeta padding) - Small, Medium, Large
 * @param contentPadding Padding customizado (opcional, sobrescreve size)
 * @param onClick Callback de clique (opcional)
 */
@Composable
fun GenesysCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    elevation: Dp = tonalElevationAdaptive(),
    shape: Shape = RoundedCornerShape(16.dp),
    size: CardSize = CardSize.Medium,
    contentPadding: Dp? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val actualPadding = contentPadding ?: when (size) {
        CardSize.Small -> CardPadding.Small
        CardSize.Medium -> CardPadding.Medium
        CardSize.Large -> CardPadding.Large
    }

    if (onClick != null) {
        Surface(
            modifier = modifier,
            shape = shape,
            color = backgroundColor,
            tonalElevation = elevation,
            onClick = onClick
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(actualPadding),
                content = content
            )
        }
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = backgroundColor,
            tonalElevation = elevation
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(actualPadding),
                content = content
            )
        }
    }
}

/**
 * Tamanhos semânticos de card.
 */
enum class CardSize {
    /** Card pequeno: stats, badges, indicadores. */
    Small,
    /** Card padrão: lista de produtos, informações. */
    Medium,
    /** Card grande: destaques, hero cards. */
    Large
}

/**
 * Retorna tonal elevation adaptativo baseado no tema.
 * Cards em temas escuros precisam de mais elevação para serem visíveis.
 */
@Composable
private fun tonalElevationAdaptive(): Dp {
    val isDarkTheme = !MaterialTheme.colorScheme.isLight
    return if (isDarkTheme) 4.dp else 1.dp
}

// Helper para detectar tema escuro
private val ColorScheme.isLight: Boolean
    get() = this.background.luminance() > 0.5f

private fun Color.luminance(): Float {
    val r = red
    val g = green
    val b = blue
    return 0.2126f * r + 0.7152f * g + 0.0722f * b
}
