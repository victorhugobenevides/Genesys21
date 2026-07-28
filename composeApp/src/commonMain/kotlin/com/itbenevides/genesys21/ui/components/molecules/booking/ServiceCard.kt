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
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacing
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.theme.GenesysMotion
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.ui.util.staggeredEntry
import kotlin.math.roundToLong

@Composable
fun ServiceCard(
    service: BookingService,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
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
        val isGridMode = maxWidth < 300.dp

        GenesysCard(
            onClick = onClick,
            elevation = if (isHovered) 4.dp else 0.dp,
            backgroundColor = MaterialTheme.colorScheme.surface,
            border =
                androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color =
                        if (isHovered) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
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
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        ServiceImage(service, backendUrl)
                        ServiceBadge(service, Alignment.BottomEnd)
                    }

                    Spacer(Modifier.height(12.dp))

                    ServiceInfo(service, isGridMode)
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
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        ServiceImage(service, backendUrl)
                        ServiceBadge(service, Alignment.BottomEnd)
                    }

                    Spacer(Modifier.width(16.dp))

                    ServiceInfo(service, isGridMode, Modifier.weight(1f))
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
        modifier = Modifier.align(alignment).padding(8.dp),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.8f),
        tonalElevation = 2.dp,
    ) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(GenesysIcons.Schedule, null, modifier = Modifier.size(12.dp), tint = Color.Black)
            Spacer(Modifier.width(4.dp))
            Text(
                text = "${service.durationMinutes} min",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.Black
            )
        }
    }
}

@Composable
private fun ServiceInfo(
    service: BookingService,
    isGridMode: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = service.name,
            style = if (isGridMode) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface,
        )

        val desc = service.description
        if (desc != null && !isGridMode) {
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        val priceFormatted = (service.price * 100.0).roundToLong() / 100.0
        Text(
            text = "${GenesysStrings.PricePrefix}$priceFormatted",
            style = if (isGridMode) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
        )

        if (!isGridMode) {
            GenesysSpacer(GenesysSpacing.Small)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shadowElevation = 4.dp,
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
