package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysIconButton
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysTextButton
import com.itbenevides.genesys21.ui.components.atoms.indicators.GenesysLoadingIndicator
import com.itbenevides.genesys21.ui.components.atoms.indicators.GenesysStatusBadge
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysAlignment
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysColumn
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysDivider
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysRow
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacer
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysWeightBox
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText
import com.itbenevides.genesys21.ui.theme.*
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.components.molecules.feedback.GenesysEmptyState
import com.itbenevides.genesys21.ui.components.molecules.layout.GenesysSectionHeader
import com.itbenevides.genesys21.ui.components.organisms.navigation.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.organisms.status.GenesysTrackingTimeline
import com.itbenevides.genesys21.ui.components.templates.pages.GenesysPage
import com.itbenevides.genesys21.ui.theme.AppTheme
import com.itbenevides.genesys21.ui.theme.GenesysDimens
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.ui.util.shimmerBrush
import com.itbenevides.genesys21.util.AnalyticsManager
import com.itbenevides.genesys21.util.CalendarUtils
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    orderId: String,
    status: String? = null,
    onBack: () -> Unit,
) {
    val viewModel: PageViewModel = koinViewModel()
    val order by viewModel.trackedOrder.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isWaitingForSignal by viewModel.isWaitingForPaymentSignal.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val uriHandler = LocalUriHandler.current

    // 1. State Management
    var state by remember { mutableStateOf(OrderTrackingState()) }

    state =
        state.copy(
            order = order,
            isLoading = isLoading || isWaitingForSignal,
        )

    LaunchedEffect(orderId) {
        viewModel.trackOrder(orderId)
        AnalyticsManager.trackPageView("${GenesysStrings.TrackOrderTitle} - $orderId")
        AnalyticsManager.logEvent("view_order_status", mapOf("order_id" to orderId))

        if (status == "success") {
            viewModel.clearCart()
        }
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

    val themeToUse = state.order?.theme ?: com.itbenevides.genesys21.domain.model.PageThemeConfig.ELEGANCE

    // 3. Render
    AppTheme(themeConfig = themeToUse) {
        OrderTrackingContent(
            state = state,
            onEvent = onEvent,
            onContactStore = { phone ->
                val message = "Olá, estou acompanhando meu pedido #${state.order?.id} e gostaria de falar com a loja."
                uriHandler.openUri("https://wa.me/$phone?text=${message.replace(" ", "%20")}")
            },
            onOpenCalendar = { link -> uriHandler.openUri(link) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderTrackingContent(
    state: OrderTrackingState,
    onEvent: (OrderTrackingEvent) -> Unit,
    onContactStore: (String) -> Unit,
    onOpenCalendar: (String) -> Unit,
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
                    OrderTrackingShimmer()
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
                            val currentOrder = state.order

                            // DESTAQUE: Card de Status Principal
                            GenesysCard(elevation = GenesysDimens.ElevationMedium) {
                                GenesysColumn(usePadding = true, horizontalAlignment = GenesysAlignment.Center) {
                                    GenesysText(text = GenesysStrings.OrderStatusLabel, style = GenesysTextStyle.Label)
                                    GenesysSpacer(GenesysTheme.spacing.m)
                                    GenesysStatusBadge(currentOrder.status)

                                    GenesysSpacer(GenesysTheme.spacing.l)

                                    GenesysRow(horizontalArrangement = Arrangement.Center) {
                                        GenesysText(
                                            text = "${GenesysStrings.OrderPrefix}${currentOrder.id.uppercase()}",
                                            style = GenesysTextStyle.Title,
                                            fontWeight = GenesysFontWeight.ExtraBold,
                                        )
                                        GenesysSpacer(GenesysTheme.spacing.s)
                                        GenesysIconButton(
                                            icon = GenesysIcons.Copy,
                                            onClick = { onEvent(OrderTrackingEvent.OnCopyOrderIdClicked) },
                                        )
                                    }

                                    // BOTAO FALAR COM A LOJA (WhatsApp do Lojista)
                                    currentOrder.whatsappContact?.let { whatsapp ->
                                        if (whatsapp.isNotBlank()) {
                                            GenesysSpacer(GenesysTheme.spacing.m)
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

                            GenesysSpacer(GenesysTheme.spacing.l)

                            // EVOLUÇÃO UX: Linha do tempo de acompanhamento
                            val isWaitingForSignal = currentOrder.status == com.itbenevides.genesys21.domain.model.OrderStatus.AWAITING_PAYMENT

                                if (isWaitingForSignal) {
                                    GenesysCard {
                                        GenesysColumn(usePadding = true, horizontalAlignment = GenesysAlignment.Center) {
                                            GenesysLoadingIndicator(modifier = Modifier.size(32.dp))
                                            GenesysSpacer(GenesysTheme.spacing.m)
                                        GenesysText(
                                            text = "Aguardando confirmação de pagamento...",
                                            style = GenesysTextStyle.Body,
                                            fontWeight = GenesysFontWeight.Bold
                                        )
                                        GenesysText(
                                            text = "Isso pode levar alguns segundos após a conclusão na Stripe.",
                                            style = GenesysTextStyle.Label
                                        )
                                    }
                                }
                                GenesysSpacer(GenesysTheme.spacing.l)
                            }

                            GenesysTrackingTimeline(currentStatus = currentOrder.status)

                            GenesysSpacer(GenesysTheme.spacing.l)

                            // Resumo do Pedido com alinhamento Premium
                            GenesysCard {
                                GenesysColumn(usePadding = true) {
                                    GenesysSectionHeader(title = GenesysStrings.OrderSummary)
                                    GenesysSpacer(GenesysTheme.spacing.m)

                                    currentOrder.items.forEach { item ->
                                        GenesysRow {
                                            GenesysWeightBox(1f) {
                                                GenesysColumn(usePadding = false) {
                                                    GenesysText(text = "${item.quantity}x ${item.name}")

                                                    item.appointment?.let { appt ->
                                                        GenesysSpacer(GenesysTheme.spacing.s)
                                                        GenesysTextButton(
                                                            text = "Adicionar ao Google Calendar",
                                                            icon = GenesysIcons.Schedule,
                                                            onClick = {
                                                                val link = CalendarUtils.generateGoogleCalendarLink(
                                                                    title = "Agendamento: ${item.name} (Genesys21)",
                                                                    description = "Seu atendimento foi confirmado!\nPedido: #${currentOrder.id}",
                                                                    startTime = appt.startTime,
                                                                    endTime = appt.endTime
                                                                )
                                                                onOpenCalendar(link)
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                            // ARREDONDAMENTO: Subtotal por item
                                            val subtotal = (item.price * item.quantity * 100.0).roundToLong() / 100.0
                                            GenesysText(
                                                text = "${GenesysStrings.PricePrefix}$subtotal",
                                                fontWeight = GenesysFontWeight.Bold,
                                            )
                                        }
                                        GenesysSpacer(GenesysTheme.spacing.s)
                                    }

                                    GenesysSpacer(GenesysTheme.spacing.m)
                                    GenesysDivider()
                                    GenesysSpacer(GenesysTheme.spacing.m)

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
                                            color = GenesysTheme.colors.brand,
                                        )
                                    }
                                }
                            }
                        }

                        GenesysSpacer(GenesysTheme.spacing.huge)
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderTrackingShimmer() {
    GenesysColumn(
        maxWidth = GenesysDimens.ContentMaxWidth,
        usePadding = true,
        verticalArrangement = Arrangement.spacedBy(GenesysTheme.spacing.l)
    ) {
        // Card de Status Shimmer
        GenesysCard {
            GenesysColumn(usePadding = true, horizontalAlignment = GenesysAlignment.Center) {
                Box(modifier = Modifier.size(100.dp, 20.dp).clip(RoundedCornerShape(8.dp)).background(shimmerBrush()))
                GenesysSpacer(GenesysTheme.spacing.m)
                Box(modifier = Modifier.size(150.dp, 32.dp).clip(RoundedCornerShape(16.dp)).background(shimmerBrush()))
                GenesysSpacer(GenesysTheme.spacing.l)
                Box(modifier = Modifier.size(200.dp, 24.dp).clip(RoundedCornerShape(8.dp)).background(shimmerBrush()))
                GenesysSpacer(GenesysTheme.spacing.m)
                Box(modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(12.dp)).background(shimmerBrush()))
            }
        }

        // Timeline Shimmer
        GenesysCard {
            GenesysColumn(usePadding = true) {
                repeat(4) {
                    GenesysRow(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(24.dp).clip(androidx.compose.foundation.shape.CircleShape).background(shimmerBrush()))
                        GenesysSpacer(GenesysTheme.spacing.m)
                        Box(modifier = Modifier.size(120.dp, 16.dp).clip(RoundedCornerShape(4.dp)).background(shimmerBrush()))
                    }
                    if (it < 3) {
                        Box(modifier = Modifier.padding(start = 11.dp).size(2.dp, 20.dp).background(shimmerBrush()))
                    }
                }
            }
        }

        // Summary Shimmer
        GenesysCard {
            GenesysColumn(usePadding = true) {
                Box(modifier = Modifier.size(150.dp, 20.dp).clip(RoundedCornerShape(8.dp)).background(shimmerBrush()))
                GenesysSpacer(GenesysTheme.spacing.l)
                repeat(3) {
                    GenesysRow {
                        Box(modifier = Modifier.size(100.dp, 16.dp).clip(RoundedCornerShape(4.dp)).background(shimmerBrush()))
                        Spacer(modifier = Modifier.weight(1f))
                        Box(modifier = Modifier.size(60.dp, 16.dp).clip(RoundedCornerShape(4.dp)).background(shimmerBrush()))
                    }
                    GenesysSpacer(GenesysTheme.spacing.m)
                }
                GenesysDivider()
                GenesysSpacer(GenesysTheme.spacing.m)
                GenesysRow {
                    Box(modifier = Modifier.size(80.dp, 24.dp).clip(RoundedCornerShape(8.dp)).background(shimmerBrush()))
                    Spacer(modifier = Modifier.weight(1f))
                    Box(modifier = Modifier.size(100.dp, 24.dp).clip(RoundedCornerShape(8.dp)).background(shimmerBrush()))
                }
            }
        }
    }
}
