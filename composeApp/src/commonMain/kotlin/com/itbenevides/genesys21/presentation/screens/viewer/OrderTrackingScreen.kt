package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.components.appbar.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.badge.GenesysStatusBadge
import com.itbenevides.genesys21.ui.components.button.GenesysIconButton
import com.itbenevides.genesys21.ui.components.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.card.GenesysCard
import com.itbenevides.genesys21.ui.components.feedback.GenesysEmptyState
import com.itbenevides.genesys21.ui.components.feedback.GenesysTrackingTimeline
import com.itbenevides.genesys21.ui.components.layout.GenesysAlignment
import com.itbenevides.genesys21.ui.components.layout.GenesysColumn
import com.itbenevides.genesys21.ui.components.layout.GenesysDivider
import com.itbenevides.genesys21.ui.components.layout.GenesysPage
import com.itbenevides.genesys21.ui.components.layout.GenesysRow
import com.itbenevides.genesys21.ui.components.layout.GenesysSectionHeader
import com.itbenevides.genesys21.ui.components.layout.GenesysSpacer
import com.itbenevides.genesys21.ui.components.layout.GenesysSpacing
import com.itbenevides.genesys21.ui.components.layout.GenesysWeightBox
import com.itbenevides.genesys21.ui.components.text.GenesysFontWeight
import com.itbenevides.genesys21.ui.components.text.GenesysText
import com.itbenevides.genesys21.ui.components.text.GenesysTextStyle
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.AppTheme
import com.itbenevides.genesys21.ui.theme.GenesysDimens
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToLong

@Composable
fun OrderTrackingScreen(
    orderId: String,
    onBack: () -> Unit
) {
    val viewModel: PageViewModel = koinViewModel()
    val order by viewModel.trackedOrder.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    // 1. State Management
    var state by remember { mutableStateOf(OrderTrackingState()) }
    
    state = state.copy(
        order = order,
        isLoading = isLoading
    )

    LaunchedEffect(orderId) {
        viewModel.trackOrder(orderId)
    }

    // 2. Event Handler
    val onEvent: (OrderTrackingEvent) -> Unit = { event ->
        when (event) {
            is OrderTrackingEvent.OnTrackOrder -> viewModel.trackOrder(event.orderId)
            is OrderTrackingEvent.OnCopyOrderIdClicked -> {
                state.order?.id?.let { clipboardManager.setText(AnnotatedString(it)) }
            }
            is OrderTrackingEvent.OnBackClicked -> onBack()
        }
    }

    val themeToUse = state.order?.theme ?: com.itbenevides.genesys21.domain.model.PageThemeConfig.ROYAL

    // 3. Render
    AppTheme(themeConfig = themeToUse) {
        OrderTrackingContent(state, onEvent)
    }
}

@Composable
private fun OrderTrackingContent(
    state: OrderTrackingState,
    onEvent: (OrderTrackingEvent) -> Unit
) {
    GenesysPage(
        topBar = {
            GenesysTopAppBar(
                title = GenesysStrings.TrackOrderTitle,
                onBack = { onEvent(OrderTrackingEvent.OnBackClicked) }
            )
        }
    ) {
        // Container Root centralizado (WasmJs)
        GenesysColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = GenesysAlignment.Center,
            usePadding = false
        ) {
            // Container responsivo com largura controlada pelo DS
            GenesysWeightBox(1f) {
                GenesysColumn(
                    maxWidth = GenesysDimens.ContentMaxWidth, 
                    useScroll = true
                ) {
                    if (state.order == null && !state.isLoading) {
                        GenesysEmptyState(
                            icon = GenesysIcons.SearchOff,
                            title = GenesysStrings.OrderNotFound,
                            description = GenesysStrings.NoOrdersDescription,
                            action = { 
                                GenesysLoadingButton(
                                    text = GenesysStrings.Back, 
                                    onClick = { onEvent(OrderTrackingEvent.OnBackClicked) }
                                ) 
                            }
                        )
                    } else if (state.order != null) {
                        val currentOrder = state.order!!
                        
                        // DESTAQUE: Card de Status Principal
                        GenesysCard(elevation = GenesysDimens.ElevationMedium) {
                             GenesysColumn(usePadding = true, horizontalAlignment = GenesysAlignment.Center) {
                                GenesysText(text = GenesysStrings.OrderStatusLabel, style = GenesysTextStyle.Label)
                                GenesysSpacer(GenesysSpacing.Medium)
                                GenesysStatusBadge(currentOrder.status)
                                
                                GenesysSpacer(GenesysSpacing.Large)
                                
                                GenesysRow(horizontalArrangement = Arrangement.Center) {
                                    GenesysText(
                                        text = "${GenesysStrings.OrderPrefix}${currentOrder.id.uppercase()}", 
                                        style = GenesysTextStyle.Title, 
                                        fontWeight = GenesysFontWeight.ExtraBold
                                    )
                                    GenesysSpacer(GenesysSpacing.Small)
                                    GenesysIconButton(
                                        icon = GenesysIcons.Copy, 
                                        onClick = { onEvent(OrderTrackingEvent.OnCopyOrderIdClicked) }
                                    )
                                }
                            }
                        }

                        GenesysSpacer(GenesysSpacing.Large)
                        
                        // EVOLUÇÃO UX: Linha do tempo de acompanhamento
                        GenesysTrackingTimeline(currentStatus = currentOrder.status)
                        
                        GenesysSpacer(GenesysSpacing.Large)

                        // Resumo do Pedido com alinhamento Premium
                        GenesysCard {
                            GenesysColumn(usePadding = true) {
                                GenesysSectionHeader(title = GenesysStrings.OrderSummary)
                                GenesysSpacer(GenesysSpacing.Medium)
                                
                                currentOrder.items.forEach { item ->
                                    GenesysRow {
                                        GenesysWeightBox(1f) {
                                            GenesysText(text = "${item.quantity}x ${item.product.name}")
                                        }
                                        // ARREDONDAMENTO: Subtotal por item
                                        val subtotal = (item.product.price * item.quantity * 100.0).roundToLong() / 100.0
                                        GenesysText(
                                            text = "${GenesysStrings.PricePrefix}$subtotal", 
                                            fontWeight = GenesysFontWeight.Bold
                                        )
                                    }
                                    GenesysSpacer(GenesysSpacing.Small)
                                }
                                
                                GenesysSpacer(GenesysSpacing.Medium)
                                GenesysDivider()
                                GenesysSpacer(GenesysSpacing.Medium)
                                
                                GenesysRow {
                                    GenesysWeightBox(1f) {
                                        GenesysText(text = GenesysStrings.Total, style = GenesysTextStyle.Title)
                                    }
                                    // ARREDONDAMENTO: Total geral do pedido
                                    val totalFormatted = (currentOrder.total * 100.0).roundToLong() / 100.0
                                    GenesysText(
                                        text = "${GenesysStrings.PricePrefix}$totalFormatted", 
                                        style = GenesysTextStyle.Title, 
                                        fontWeight = GenesysFontWeight.ExtraBold,
                                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                    
                    GenesysSpacer(GenesysSpacing.Huge)
                }
            }
        }
    }
}
