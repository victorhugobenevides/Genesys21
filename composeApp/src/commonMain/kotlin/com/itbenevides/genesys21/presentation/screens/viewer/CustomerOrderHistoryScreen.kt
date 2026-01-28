package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.itbenevides.genesys21.domain.model.Order
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.components.appbar.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.badge.GenesysStatusBadge
import com.itbenevides.genesys21.ui.components.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.card.GenesysCard
import com.itbenevides.genesys21.ui.components.feedback.GenesysEmptyState
import com.itbenevides.genesys21.ui.components.layout.*
import com.itbenevides.genesys21.ui.components.text.GenesysText
import com.itbenevides.genesys21.ui.components.text.GenesysTextStyle
import com.itbenevides.genesys21.ui.components.text.GenesysFontWeight
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.GenesysDimens
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.util.AnalyticsManager
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CustomerOrderHistoryScreen(
    onBack: () -> Unit,
    onOrderClick: (Order) -> Unit
) {
    val viewModel: PageViewModel = koinViewModel()
    val orders by viewModel.customerOrders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // 1. State Management
    var state by remember { mutableStateOf(OrderHistoryState()) }
    
    state = state.copy(
        orders = orders,
        isLoading = isLoading
    )

    LaunchedEffect(Unit) {
        viewModel.loadCustomerOrders()
        AnalyticsManager.trackPageView(GenesysStrings.OrderHistoryTitle)
    }

    // 2. Event Handler
    val onEvent: (OrderHistoryEvent) -> Unit = { event ->
        when (event) {
            is OrderHistoryEvent.OnBackClicked -> onBack()
            is OrderHistoryEvent.OnOrderClicked -> {
                AnalyticsManager.logEvent("view_order_from_history", mapOf("order_id" to event.order.id))
                onOrderClick(event.order)
            }
        }
    }

    // 3. Render
    OrderHistoryContent(state, onEvent)
}

@Composable
private fun OrderHistoryContent(
    state: OrderHistoryState,
    onEvent: (OrderHistoryEvent) -> Unit
) {
    GenesysPage(
        topBar = {
            GenesysTopAppBar(
                title = GenesysStrings.OrderHistoryTitle,
                onBack = { onEvent(OrderHistoryEvent.OnBackClicked) }
            )
        }
    ) {
        GenesysColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = GenesysAlignment.Center,
            usePadding = false
        ) {
            if (state.orders.isEmpty() && !state.isLoading) {
                 GenesysEmptyState(
                    icon = GenesysIcons.ShoppingBag,
                    title = GenesysStrings.NoOrdersFound,
                    description = "Você ainda não possui pedidos registrados.",
                    action = {
                        GenesysLoadingButton(
                            text = GenesysStrings.Back, 
                            onClick = { onEvent(OrderHistoryEvent.OnBackClicked) }
                        )
                    }
                )
            } else {
                GenesysLazyColumn(
                    items = state.orders,
                    maxWidth = GenesysDimens.ContentMaxWidth
                ) { order ->
                    HistoryOrderCard(
                        order = order, 
                        onClick = { onEvent(OrderHistoryEvent.OnOrderClicked(order)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryOrderCard(order: Order, onClick: () -> Unit) {
    val date = remember(order.createdAt) {
        val instant = Instant.fromEpochMilliseconds(order.createdAt)
        val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        "${dt.dayOfMonth.toString().padStart(2, '0')}/${dt.monthNumber.toString().padStart(2, '0')}/${dt.year} às ${dt.hour}:${dt.minute.toString().padStart(2, '0')}"
    }

    GenesysCard(
        elevation = GenesysDimens.ElevationMedium,
        onClick = onClick
    ) {
        GenesysRow {
            GenesysWeightBox(1f) {
                GenesysColumn(usePadding = false) {
                    GenesysText(
                        text = "Pedido #${order.id.takeLast(6).uppercase()}", 
                        fontWeight = GenesysFontWeight.Bold
                    )
                    GenesysText(
                        text = date, 
                        style = GenesysTextStyle.Label
                    )
                }
            }
            GenesysStatusBadge(order.status)
        }
        
        GenesysSpacer(GenesysSpacing.Small)
        GenesysDivider()
        GenesysSpacer(GenesysSpacing.Small)
        
        GenesysRow {
            GenesysWeightBox(1f) {
                GenesysText(
                    text = "${order.items.sumOf { it.quantity }} itens", 
                    style = GenesysTextStyle.Body
                )
            }
            GenesysText(
                text = "Total: R$ ${order.total}", 
                style = GenesysTextStyle.Title,
                fontWeight = GenesysFontWeight.Bold
            )
        }
    }
}
