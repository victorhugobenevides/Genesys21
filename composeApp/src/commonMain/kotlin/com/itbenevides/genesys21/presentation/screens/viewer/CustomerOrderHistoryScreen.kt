package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.itbenevides.genesys21.domain.model.Order
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.components.organisms.navigation.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.atoms.indicators.GenesysStatusBadge
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.components.molecules.feedback.GenesysEmptyState
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysAlignment
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysColumn
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysDivider
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysLazyColumn
import com.itbenevides.genesys21.ui.components.templates.pages.GenesysPage
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysRow
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacer
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacing
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysWeightBox
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysFontWeight
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysTextStyle
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.theme.GenesysDimens
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.util.AnalyticsManager
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToLong
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun CustomerOrderHistoryScreen(
    onBack: () -> Unit,
    onOrderClick: (Order) -> Unit,
) {
    val viewModel: PageViewModel = koinViewModel()
    val orders by viewModel.customerOrders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var state by remember { mutableStateOf(OrderHistoryState()) }

    state =
        state.copy(
            orders = orders,
            isLoading = isLoading,
        )

    LaunchedEffect(Unit) {
        viewModel.loadCustomerOrders()
        AnalyticsManager.trackPageView(GenesysStrings.OrderHistoryTitle)
    }

    val onEvent: (OrderHistoryEvent) -> Unit = { event ->
        when (event) {
            is OrderHistoryEvent.OnBackClicked -> onBack()
            is OrderHistoryEvent.OnOrderClicked -> {
                AnalyticsManager.logEvent("view_order_from_history", mapOf("order_id" to event.order.id))
                onOrderClick(event.order)
            }
        }
    }

    OrderHistoryContent(state, onEvent)
}

@Composable
private fun OrderHistoryContent(
    state: OrderHistoryState,
    onEvent: (OrderHistoryEvent) -> Unit,
) {
    GenesysPage(
        topBar = {
            GenesysTopAppBar(
                title = GenesysStrings.OrderHistoryTitle,
                onBack = { onEvent(OrderHistoryEvent.OnBackClicked) },
            )
        },
    ) {
        GenesysColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = GenesysAlignment.Center,
            usePadding = false,
        ) {
            if (state.orders.isEmpty() && !state.isLoading) {
                GenesysEmptyState(
                    icon = GenesysIcons.ShoppingBag,
                    title = GenesysStrings.NoHistoryTitle,
                    description = GenesysStrings.NoHistoryDescription,
                    action = {
                        GenesysLoadingButton(
                            text = GenesysStrings.Back,
                            onClick = { onEvent(OrderHistoryEvent.OnBackClicked) },
                        )
                    },
                )
            } else {
                GenesysLazyColumn(
                    items = state.orders,
                    maxWidth = GenesysDimens.ContentMaxWidth,
                ) { order ->
                    HistoryOrderCard(
                        order = order,
                        onClick = { onEvent(OrderHistoryEvent.OnOrderClicked(order)) },
                    )
                    GenesysSpacer(GenesysSpacing.Medium)
                }
            }
        }
    }
}

@Composable
private fun HistoryOrderCard(
    order: Order,
    onClick: () -> Unit,
) {
    val dateText =
        remember(order.createdAt) {
            val instant = Instant.fromEpochMilliseconds(order.createdAt)
            val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            "${dt.dayOfMonth.toString().padStart(2, '0')}/${dt.monthNumber.toString().padStart(2, '0')}/${dt.year}"
        }

    GenesysCard(
        elevation = GenesysDimens.ElevationMedium,
        onClick = onClick,
    ) {
        GenesysColumn(usePadding = true) {
            GenesysRow(verticalAlignment = Alignment.CenterVertically) {
                GenesysWeightBox(1f) {
                    GenesysColumn(usePadding = false) {
                        GenesysText(
                            text = "${GenesysStrings.OrderPrefix}${order.id.takeLast(6).uppercase()}",
                            fontWeight = GenesysFontWeight.ExtraBold,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        )
                        GenesysText(
                            text = dateText,
                            style = GenesysTextStyle.Label,
                        )
                    }
                }
                GenesysStatusBadge(order.status)
            }

            GenesysSpacer(GenesysSpacing.Medium)
            GenesysDivider()
            GenesysSpacer(GenesysSpacing.Medium)

            GenesysRow(verticalAlignment = Alignment.Bottom) {
                GenesysWeightBox(1f) {
                    GenesysText(
                        text = "${order.items.sumOf { it.quantity }} itens",
                        style = GenesysTextStyle.Body,
                    )
                }
                val totalFormatted = (order.total * 100.0).roundToLong() / 100.0
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
