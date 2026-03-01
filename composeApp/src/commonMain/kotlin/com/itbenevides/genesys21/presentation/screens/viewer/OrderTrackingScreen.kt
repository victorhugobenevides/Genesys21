package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.Order
import com.itbenevides.genesys21.domain.model.OrderStatus
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.components.appbar.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.button.GenesysTextButton
import com.itbenevides.genesys21.ui.components.card.GenesysCard
import com.itbenevides.genesys21.ui.components.layout.*
import com.itbenevides.genesys21.ui.components.text.*
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.util.Analytics
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

    LaunchedEffect(orderId) {
        viewModel.trackOrder(orderId)
        Analytics.trackPageView("${GenesysStrings.TrackOrderTitle} - $orderId")
        Analytics.logEvent("view_order_status", mapOf("order_id" to orderId))
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.onDispose() }
    }

    val onEvent: (OrderTrackingEvent) -> Unit = { event ->
        when (event) {
            is OrderTrackingEvent.OnTrackOrder -> {
                viewModel.trackOrder(event.orderId)
            }
            is OrderTrackingEvent.OnCopyOrderIdClicked -> {
                order?.id?.let { id ->
                    Analytics.logEvent("copy_order_id", mapOf("order_id" to id))
                    clipboardManager.setText(AnnotatedString(id))
                }
            }
            is OrderTrackingEvent.OnBackClicked -> onBack()
        }
    }

    OrderTrackingContent(order, isLoading, onEvent)
}

@Composable
private fun OrderTrackingContent(
    order: Order?,
    isLoading: Boolean,
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
        if (order == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (isLoading) CircularProgressIndicator()
                else GenesysText(text = GenesysStrings.OrderNotFound)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OrderHeaderInfo(order, onEvent)
                GenesysSpacer(GenesysSpacing.Large)
                OrderStatusStepper(order.status)
                GenesysSpacer(GenesysSpacing.ExtraLarge)
                OrderDetailsCard(order)
            }
        }
    }
}

@Composable
private fun OrderHeaderInfo(order: Order, onEvent: (OrderTrackingEvent) -> Unit) {
    GenesysColumn(horizontalAlignment = GenesysAlignment.Center, usePadding = false) {
        GenesysText(
            text = "${GenesysStrings.OrderPrefix}${order.id.uppercase()}",
            style = GenesysTextStyle.Title,
            fontWeight = GenesysFontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        GenesysTextButton(
            text = "Copiar Código",
            onClick = { onEvent(OrderTrackingEvent.OnCopyOrderIdClicked) }
        )
    }
}

@Composable
private fun OrderStatusStepper(currentStatus: OrderStatus) {
    val steps = listOf(
        OrderStatus.PENDING to "Recebido",
        OrderStatus.PROCESSING to "Preparando",
        OrderStatus.COMPLETED to "Finalizado"
    )

    val currentIndex = steps.indexOfFirst { it.first == currentStatus }.let { if (it == -1) 0 else it }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, (status, label) ->
            val isCompleted = index <= currentIndex
            val isCurrent = index == currentIndex
            
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCompleted) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(GenesysIcons.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
                GenesysText(
                    text = label,
                    style = GenesysTextStyle.Label,
                    fontWeight = if (isCurrent) GenesysFontWeight.Bold else GenesysFontWeight.Normal,
                    color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .height(2.dp)
                        .weight(0.5f)
                        .background(
                            if (index < currentIndex) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }
    }
}

@Composable
private fun OrderDetailsCard(order: Order) {
    GenesysCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            GenesysText(text = GenesysStrings.OrderSummary, style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            
            order.items.forEach { item ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    GenesysText(text = "${item.quantity}x", modifier = Modifier.width(32.dp))
                    GenesysText(text = item.product.name, modifier = Modifier.weight(1f))
                    GenesysText(text = "R$ ${item.product.price * item.quantity}")
                }
            }
            
            Spacer(Modifier.height(16.dp))
            GenesysDivider()
            Spacer(Modifier.height(16.dp))
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                GenesysText(text = "Total", fontWeight = GenesysFontWeight.Bold)
                GenesysText(
                    text = "R$ ${order.total}",
                    style = GenesysTextStyle.Title,
                    fontWeight = GenesysFontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
