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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.components.organisms.navigation.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.atoms.indicators.GenesysStatusBadge
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysIconButton
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.components.molecules.feedback.GenesysEmptyState
import com.itbenevides.genesys21.ui.components.atoms.indicators.GenesysLoadingOverlay
import com.itbenevides.genesys21.ui.components.organisms.status.GenesysTrackingTimeline
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysAlignment
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysColumn
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysDivider
import com.itbenevides.genesys21.ui.components.templates.pages.GenesysPage
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysRow
import com.itbenevides.genesys21.ui.components.molecules.layout.GenesysSectionHeader
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacer
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacing
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysWeightBox
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysFontWeight
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysTextStyle
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.theme.AppTheme
import com.itbenevides.genesys21.ui.theme.GenesysDimens
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.util.AnalyticsManager
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToLong

@Composable
fun OrderTrackingScreen(
    orderId: String,
    onBack: () -> Unit,
) {
    val viewModel: PageViewModel = koinViewModel()
    val order by viewModel.trackedOrder.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val uriHandler = LocalUriHandler.current

    // 1. State Management
    var state by remember { mutableStateOf(OrderTrackingState()) }

    state =
        state.copy(
            order = order,
            isLoading = isLoading,
        )

    LaunchedEffect(orderId) {
        viewModel.trackOrder(orderId)
        AnalyticsManager.trackPageView("${GenesysStrings.TrackOrderTitle} - $orderId")
        AnalyticsManager.logEvent("view_order_status", mapOf("order_id" to orderId))
    }

    // 2. Event Handler
    val onEvent: (OrderTrackingEvent) -> Unit = { event ->
        when (event) {
            is OrderTrackingEvent.OnTrackOrder -> viewModel.trackOrder(event.orderId)
            is OrderTrackingEvent.OnCopyOrderIdClicked -> {
                state.order?.id?.let {
                    clipboardManager.setText(AnnotatedString(it))
                    AnalyticsManager.logEvent("copy_order_id", mapOf("order_id" to it))
                }
            }
            is OrderTrackingEvent.OnBackClicked -> onBack()
        }
    }

    val themeToUse = state.order?.theme ?: com.itbenevides.genesys21.domain.model.PageThemeConfig.ROYAL

    // 3. Render
    AppTheme(themeConfig = themeToUse) {
        OrderTrackingContent(state, onEvent, onContactStore = { phone ->
            val message = "Olá, estou acompanhando meu pedido #${state.order?.id} e gostaria de falar com a loja."
            uriHandler.openUri("https://wa.me/$phone?text=${message.replace(" ", "%20")}")
        })
    }
}

@Composable
private fun OrderTrackingContent(
    state: OrderTrackingState,
    onEvent: (OrderTrackingEvent) -> Unit,
    onContactStore: (String) -> Unit,
) {
    GenesysPage(
        topBar = {
            GenesysTopAppBar(
                title = GenesysStrings.TrackOrderTitle,
                onBack = { onEvent(OrderTrackingEvent.OnBackClicked) },
            )
        },
    ) {
        // Container Root centralizado (WasmJs)
        GenesysColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = GenesysAlignment.Center,
            usePadding = false,
        ) {
            // Container responsivo com largura controlada pelo DS
            GenesysWeightBox(1f) {
                if (state.isLoading) {
                    GenesysLoadingOverlay()
                } else {
                    GenesysColumn(
                        maxWidth = GenesysDimens.ContentMaxWidth,
                        useScroll = true,
                    ) {
                        if (state.order == null) {
                            GenesysEmptyState(
                                icon = GenesysIcons.SearchOff,
                                title = GenesysStrings.OrderNotFound,
                                description = GenesysStrings.NoOrdersDescription,
                                action = {
                                    GenesysLoadingButton(
                                        text = GenesysStrings.Back,
                                        onClick = { onEvent(OrderTrackingEvent.OnBackClicked) },
                                    )
                                },
                            )
                        } else {
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
                                            fontWeight = GenesysFontWeight.ExtraBold,
                                        )
                                        GenesysSpacer(GenesysSpacing.Small)
                                        GenesysIconButton(
                                            icon = GenesysIcons.Copy,
                                            onClick = { onEvent(OrderTrackingEvent.OnCopyOrderIdClicked) },
                                        )
                                    }

                                    // BOTAO FALAR COM A LOJA (WhatsApp do Lojista)
                                    currentOrder.whatsappContact?.let { whatsapp ->
                                        if (whatsapp.isNotBlank()) {
                                            GenesysSpacer(GenesysSpacing.Medium)
                                            GenesysLoadingButton(
                                                text = "Falar com a Loja",
                                                icon = GenesysIcons.Chat,
                                                onClick = { onContactStore(whatsapp) },
                                                fillWidth = true,
                                            )
                                        }
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
                                                fontWeight = GenesysFontWeight.Bold,
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
                                            color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
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
}
