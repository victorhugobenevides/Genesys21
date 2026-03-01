package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.Order
import com.itbenevides.genesys21.domain.model.OrderStatus
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.components.appbar.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.card.GenesysCard
import com.itbenevides.genesys21.ui.components.feedback.GenesysEmptyState
import com.itbenevides.genesys21.ui.components.layout.GenesysColumn
import com.itbenevides.genesys21.ui.components.layout.GenesysDivider
import com.itbenevides.genesys21.ui.components.layout.GenesysPage
import com.itbenevides.genesys21.ui.components.layout.GenesysSpacer
import com.itbenevides.genesys21.ui.components.layout.GenesysSpacing
import com.itbenevides.genesys21.ui.components.text.GenesysFontWeight
import com.itbenevides.genesys21.ui.components.text.GenesysText
import com.itbenevides.genesys21.ui.components.text.GenesysTextStyle
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.util.Analytics
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

    LaunchedEffect(Unit) {
        viewModel.loadCustomerOrders()
        Analytics.trackPageView(GenesysStrings.OrderHistoryTitle)
    }

    val onEvent: (OrderHistoryEvent) -> Unit = { event ->
        when (event) {
            is OrderHistoryEvent.OnOrderClicked -> {
                Analytics.logEvent("view_order_from_history", mapOf("order_id" to event.order.id))
                onOrderClick(event.order)
            }
            is OrderHistoryEvent.OnBackClicked -> onBack()
        }
    }

    OrderHistoryContent(orders, isLoading, onEvent)
}

@Composable
private fun OrderHistoryContent(
    orders: List<Order>,
    isLoading: Boolean,
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
        if (orders.isEmpty() && !isLoading) {
            GenesysEmptyState(
                icon = GenesysIcons.List,
                title = GenesysStrings.NoHistoryTitle,
                description = GenesysStrings.NoHistoryDescription
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orders) { order ->
                    OrderHistoryItem(order = order, onClick = { onEvent(OrderHistoryEvent.OnOrderClicked(order)) })
                }
            }
        }
    }
}

@Composable
private fun OrderHistoryItem(order: Order, onClick: () -> Unit) {
    GenesysCard(onClick = onClick) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    GenesysText(
                        text = "${GenesysStrings.OrderPrefix}${order.id.takeLast(6).uppercase()}",
                        style = GenesysTextStyle.Label,
                        fontWeight = GenesysFontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    val dateText = remember(order.createdAt) {
                        try {
                            val instant = Instant.fromEpochMilliseconds(order.createdAt)
                            val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                            "${dt.dayOfMonth}/${dt.monthNumber}/${dt.year}"
                        } catch (e: Exception) { "" }
                    }
                    GenesysText(text = dateText, style = GenesysTextStyle.Label, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                StatusBadge(status = order.status)
            }
            
            Spacer(Modifier.height(12.dp))
            GenesysDivider()
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val itemsText = if (order.items.size == 1) "1 item" else "${order.items.size} itens"
                GenesysText(text = itemsText, style = GenesysTextStyle.Body)
                
                GenesysText(
                    text = "${GenesysStrings.PricePrefix}${order.total}",
                    style = GenesysTextStyle.Title,
                    fontWeight = GenesysFontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(status: OrderStatus) {
    val color = when (status) {
        OrderStatus.COMPLETED -> Color(0xFF34C759)
        OrderStatus.PENDING, OrderStatus.PAYMENT_PENDING -> Color(0xFFFF9500)
        OrderStatus.PROCESSING -> Color(0xFF007AFF)
        OrderStatus.CANCELLED, OrderStatus.FAILED -> Color(0xFFFF3B30)
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = status.name.replace("_", " "),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
    }
}
