package com.itbenevides.genesys21.ui.components.molecules.booking

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.itbenevides.genesys21.di.getBaseUrl
import com.itbenevides.genesys21.domain.model.BookingService
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacer
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.theme.GenesysMotion
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.ui.theme.GenesysTheme
import com.itbenevides.genesys21.ui.util.staggeredEntry
import kotlin.math.roundToLong

@Composable
fun ServiceCard(
    service: BookingService,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showPrice: Boolean = true, // NOVO: Controle de visibilidade do preço
    index: Int = 0,
) {
    var isHovered by remember { mutableStateOf(false) }
    val backendUrl = remember { getBaseUrl() }

    val interactionScale by animateFloatAsState(
        targetValue = if (isHovered) 1.03f else 1f,
        animationSpec = GenesysMotion.interactiveSpring,
    )

    BoxWithConstraints(
        modifier =
            modifier
                .staggeredEntry(index)
                .scale(interactionScale)
                .semantics(mergeDescendants = true) {
                    contentDescription = "Serviço: ${service.name}, Preço: ${GenesysStrings.PricePrefix}${service.price}, Duração: ${service.durationMinutes} minutos"
                }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            when (event.type) {
                                PointerEventType.Enter -> isHovered = true
                                PointerEventType.Exit -> isHovered = false
                            }
                        }
                    }
                },
    ) {
        // Modo Grade: Se a largura for pequena (ex: dentro de uma grade de 2+ colunas), usa layout vertical
        val isGridMode = maxWidth < GenesysTheme.spacing.xxl * 6 // ~300dp

        GenesysCard(
            onClick = onClick,
            elevation = if (isHovered) GenesysTheme.spacing.xxs else GenesysTheme.spacing.none,
            backgroundColor = GenesysTheme.colors.surface,
            border =
                androidx.compose.foundation.BorderStroke(
                    width = GenesysTheme.spacing.xxxs,
                    color =
                        if (isHovered) {
                            GenesysTheme.colors.brand.copy(alpha = 0.2f)
                        } else {
                            GenesysTheme.colors.onSurface.copy(alpha = 0.08f)
                        },
                ),
        ) {
            if (isGridMode) {
                // LAYOUT VERTICAL (ESTILO PRODUTO)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(GenesysTheme.config.cornerRadius))
                            .background(GenesysTheme.colors.surfaceVariant.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        ServiceImage(service, backendUrl)
                        ServiceBadge(service, Alignment.BottomEnd)
                    }

                    Spacer(Modifier.height(GenesysTheme.spacing.s))

                    ServiceInfo(service, isGridMode, showPrice)
                }
            } else {
                // LAYOUT HORIZONTAL (ESTILO LISTA)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(GenesysTheme.config.cornerRadius))
                            .background(GenesysTheme.colors.surfaceVariant.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        ServiceImage(service, backendUrl)
                        ServiceBadge(service, Alignment.BottomEnd)
                    }

                    Spacer(Modifier.width(GenesysTheme.spacing.m))

                    ServiceInfo(service, isGridMode, showPrice, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ServiceImage(service: BookingService, backendUrl: String) {
    val imageUrl = remember(service.imageUrls) {
        val first = service.imageUrls.firstOrNull() ?: ""
        if (first.startsWith("/")) "$backendUrl$first" else first
    }

    AsyncImage(
        model = imageUrl,
        contentDescription = service.name,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
    )
}

@Composable
private fun BoxScope.ServiceBadge(service: BookingService, alignment: Alignment) {
    Surface(
        modifier = Modifier.align(alignment).padding(GenesysTheme.spacing.xs),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.8f),
        tonalElevation = GenesysTheme.spacing.xxxs,
    ) {
        Row(modifier = Modifier.padding(horizontal = GenesysTheme.spacing.xs, vertical = GenesysTheme.spacing.xxs), verticalAlignment = Alignment.CenterVertically) {
            Icon(GenesysIcons.Schedule, null, modifier = Modifier.size(GenesysTheme.spacing.s), tint = Color.Black)
            Spacer(Modifier.width(GenesysTheme.spacing.xxs))
            Text(
                text = "${service.durationMinutes} min",
                style = GenesysTheme.typography.label.copy(fontWeight = FontWeight.Bold),
                color = Color.Black
            )
        }
    }
}

@Composable
private fun ServiceInfo(
    service: BookingService,
    isGridMode: Boolean,
    showPrice: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = service.name,
            style = if (isGridMode) GenesysTheme.typography.title else GenesysTheme.typography.headline,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = GenesysTheme.colors.onSurface,
        )

        val desc = service.description
        if (desc != null && !isGridMode) {
            Text(
                text = desc,
                style = GenesysTheme.typography.bodySmall,
                color = GenesysTheme.colors.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        val priceFormatted = (service.price * 100.0).roundToLong() / 100.0
        if (showPrice) {
            Text(
                text = "${GenesysStrings.PricePrefix}$priceFormatted",
                style = if (isGridMode) GenesysTheme.typography.body else GenesysTheme.typography.title,
                fontWeight = FontWeight.Black,
                color = GenesysTheme.colors.brand,
            )
        }

        if (!isGridMode) {
            GenesysSpacer(GenesysTheme.spacing.s)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(GenesysTheme.spacing.s),
                    color = GenesysTheme.colors.brand,
                    contentColor = Color.White,
                    shadowElevation = GenesysTheme.spacing.xxs,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = GenesysIcons.ArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}
