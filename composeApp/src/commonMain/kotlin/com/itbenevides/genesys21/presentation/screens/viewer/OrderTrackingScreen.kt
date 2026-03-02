package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.animation.*
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.Order
import com.itbenevides.genesys21.domain.model.OrderStatus
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.components.appbar.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.button.GenesysTextButton
import com.itbenevides.genesys21.ui.components.card.GenesysCard
import com.itbenevides.genesys21.ui.components.feedback.GenesysEmptyState
import com.itbenevides.genesys21.ui.components.layout.*
import com.itbenevides.genesys21.ui.components.text.*
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.util.Analytics
import kotlinx.coroutines.launch
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
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(orderId) {
        viewModel.trackOrder(orderId)
        Analytics.trackPageView("${GenesysStrings.TrackOrderTitle} - $orderId")
    }

    val onEvent: (OrderTrackingEvent) -> Unit = { event ->
        when (event) {
            is OrderTrackingEvent.OnCopyOrderIdClicked -> {
                order?.id?.let { id ->
                    clipboardManager.setText(AnnotatedString(id))
                    scope.launch { snackbarHostState.showSnackbar("Código copiado!") }
                }
            }
            is OrderTrackingEvent.OnBackClicked -> onBack()
            is OrderTrackingEvent.OnContactSeller -> {
                order?.whatsappContact?.let { whatsapp ->
                    val msg = "Olá, gostaria de informações sobre o pedido #${order?.id}"
                    uriHandler.openUri("https://wa.me/$whatsapp?text=${msg.replace(" ", "%20")}")
                }
            }
            else -> {}
        }
    }

    GenesysPage(
        topBar = {
            GenesysTopAppBar(
                title = GenesysStrings.TrackOrderTitle,
                onBack = { onEvent(OrderTrackingEvent.OnBackClicked) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        if (order == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (isLoading) CircularProgressIndicator()
                else GenesysEmptyState(
                    icon = GenesysIcons.SearchOff,
                    title = GenesysStrings.OrderNotFound,
                    description = "Verifique o código ou tente novamente mais tarde."
                )
            }
        } else {
            OrderTrackingLayout(order!!, onEvent)
        }
    }
}

@Composable
private fun OrderTrackingLayout(order: Order, onEvent: (OrderTrackingEvent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OrderHeaderInfo(order, onEvent)
        GenesysSpacer(GenesysSpacing.Large)
        
        // UX IMPROVEMENT: Stepper mais visual e animado
        OrderStatusStepper(order.status)
        
        GenesysSpacer(GenesysSpacing.ExtraLarge)
        OrderDetailsCard(order)
        
        GenesysSpacer(GenesysSpacing.Large)
        
        // UX IMPROVEMENT: Botão de contato direto
        if (!order.whatsappContact.isNullOrBlank()) {
            GenesysLoadingButton(
                text = "Falar com o Vendedor",
                icon = GenesysIcons.Chat,
                onClick = { onEvent(OrderTrackingEvent.OnContactSeller) },
                fillWidth = true,
                containerColor = Color(0xFF25D366) // Cor do WhatsApp
            )
        }
    }
}

@Composable
private fun OrderStatusStepper(currentStatus: OrderStatus) {
    val steps = listOf(
        Triple(OrderStatus.PENDING, "Recebido", GenesysIcons.Description),
        Triple(OrderStatus.PROCESSING, "Preparando", GenesysIcons.Inventory),
        Triple(OrderStatus.COMPLETED, "Finalizado", GenesysIcons.Check)
    )

    val currentIndex = steps.indexOfFirst { it.first == currentStatus }.let { if (it == -1) 0 else it }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, (status, label, icon) ->
            val isCompleted = index <= currentIndex
            val isCurrent = index == currentIndex
            
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCompleted) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon, 
                        contentDescription = null, 
                        tint = if (isCompleted) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
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
                        .height(3.dp)
                        .weight(0.4f)
                        .background(
                            if (index < currentIndex) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }
    }
}

// ... OrderHeaderInfo e OrderDetailsCard permanecem similares mas com ajustes de espaçamento
@Composable
private fun OrderHeaderInfo(order: Order, onEvent: (OrderTrackingEvent) -> Unit) {
    GenesysColumn(horizontalAlignment = GenesysAlignment.Center, usePadding = false) {
        GenesysText(
            text = "Status do Pedido",
            style = GenesysTextStyle.Body,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        GenesysText(
            text = "${GenesysStrings.OrderPrefix}${order.id.uppercase()}",
            style = GenesysTextStyle.Headline,
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
private fun OrderDetailsCard(order: Order) {
    GenesysCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            GenesysText(text = GenesysStrings.OrderSummary, style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            
            order.items.forEach { item ->
                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    GenesysText(text = "${item.quantity}x", fontWeight = GenesysFontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(36.dp))
                    GenesysText(text = item.product.name, modifier = Modifier.weight(1f), maxLines = 1)
                    GenesysText(text = "R$ ${item.product.price * item.quantity}", fontWeight = GenesysFontWeight.Bold)
                }
            }
            
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(16.dp))
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                GenesysText(text = "Total Pago", style = GenesysTextStyle.Body)
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
