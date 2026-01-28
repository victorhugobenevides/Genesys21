package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.itbenevides.genesys21.domain.model.OrderStatus
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.components.appbar.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.button.GenesysIconButton
import com.itbenevides.genesys21.ui.components.card.GenesysCard
import com.itbenevides.genesys21.ui.components.feedback.GenesysEmptyState
import com.itbenevides.genesys21.ui.components.badge.GenesysStatusBadge
import com.itbenevides.genesys21.ui.components.feedback.GenesysTrackingTimeline
import com.itbenevides.genesys21.ui.components.layout.*
import com.itbenevides.genesys21.ui.components.text.GenesysText
import com.itbenevides.genesys21.ui.components.text.GenesysTextStyle
import com.itbenevides.genesys21.ui.components.text.GenesysFontWeight
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.AppTheme
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.ui.theme.GenesysDimens
import org.koin.compose.viewmodel.koinViewModel

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
        GenesysColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = GenesysAlignment.Center,
            usePadding = false
        ) {
            GenesysColumn(
                maxWidth = GenesysDimens.ContentMaxWidth, 
                useScroll = true
            ) {
                if (state.order == null && !state.isLoading) {
                    GenesysEmptyState(
                        icon = GenesysIcons.SearchOff,
                        title = GenesysStrings.OrderNotFound,
                        description = "Não conseguimos localizar seu pedido.",
                        action = { 
                            GenesysLoadingButton(
                                text = GenesysStrings.Back, 
                                onClick = { onEvent(OrderTrackingEvent.OnBackClicked) }
                            ) 
                        }
                    )
                } else if (state.order != null) {
                    val currentOrder = state.order
                    
                    GenesysCard(elevation = GenesysDimens.ElevationMedium) {
                         GenesysColumn(usePadding = true, horizontalAlignment = GenesysAlignment.Center) {
                            GenesysText(text = GenesysStrings.OrderStatusLabel, style = GenesysTextStyle.Label)
                            GenesysSpacer(GenesysSpacing.Medium)
                            GenesysStatusBadge(currentOrder.status)
                            
                            GenesysSpacer(GenesysSpacing.Large)
                            
                            GenesysRow {
                                GenesysText(
                                    text = "#${currentOrder.id.uppercase()}", 
                                    style = GenesysTextStyle.Title, 
                                    fontWeight = GenesysFontWeight.ExtraBold,
                                    weightValue = 1f
                                )
                                GenesysIconButton(
                                    icon = GenesysIcons.Copy, 
                                    onClick = { onEvent(OrderTrackingEvent.OnCopyOrderIdClicked) }
                                )
                            }
                        }
                    }

                    GenesysSpacer(GenesysSpacing.Large)
                    GenesysTrackingTimeline(currentStatus = currentOrder.status)
                    GenesysSpacer(GenesysSpacing.Large)

                    GenesysCard {
                        GenesysColumn(usePadding = true) {
                            GenesysSectionHeader(title = GenesysStrings.OrderSummary)
                            GenesysSpacer(GenesysSpacing.Medium)
                            
                            currentOrder.items.forEach { item ->
                                GenesysRow {
                                    GenesysText(
                                        text = "${item.quantity}x ${item.product.name}", 
                                        weightValue = 1f
                                    )
                                    GenesysText(
                                        text = "R$ ${item.product.price * item.quantity}", 
                                        fontWeight = GenesysFontWeight.Bold
                                    )
                                }
                            }
                            
                            GenesysSpacer(GenesysSpacing.Medium)
                            GenesysDivider()
                            GenesysSpacer(GenesysSpacing.Medium)
                            
                            GenesysRow {
                                GenesysText(text = GenesysStrings.Total, style = GenesysTextStyle.Title, weightValue = 1f)
                                GenesysText(text = "R$ ${currentOrder.total}", style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
