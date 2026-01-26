package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString

@Composable
fun OrderTrackingScreen(
    orderId: String,
    onBack: () -> Unit
) {
    val viewModel: PageViewModel = koinViewModel()
    val order by viewModel.trackedOrder.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(orderId) {
        viewModel.trackOrder(orderId)
    }

    val themeToUse = order?.theme ?: com.itbenevides.genesys21.domain.model.PageThemeConfig.ROYAL

    AppTheme(themeConfig = themeToUse) {
        GenesysPage(
            topBar = {
                GenesysTopAppBar(
                    title = GenesysStrings.TrackOrderTitle,
                    onBack = onBack
                )
            }
        ) {
            // Root que centraliza o conteúdo em telas largas (WasmJs)
            GenesysColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = GenesysAlignment.Center,
                usePadding = false
            ) {
                // Conteúdo limitado pela largura máxima do DS
                GenesysColumn(
                    maxWidth = GenesysDimens.ContentMaxWidth, 
                    useScroll = true
                ) {
                    if (order == null && !isLoading) {
                        GenesysEmptyState(
                            icon = GenesysIcons.SearchOff,
                            title = GenesysStrings.OrderNotFound,
                            description = "Não conseguimos localizar seu pedido.",
                            action = { GenesysLoadingButton(text = GenesysStrings.Back, onClick = onBack) }
                        )
                    } else if (order != null) {
                        val currentOrder = order!!
                        
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
                                    GenesysIconButton(icon = GenesysIcons.Copy, onClick = { clipboardManager.setText(AnnotatedString(currentOrder.id)) })
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
}
