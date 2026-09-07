package com.itbenevides.genesys21.presentation.screens.list.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.ChatMessage
import com.itbenevides.genesys21.domain.model.OrderStatus
import com.itbenevides.genesys21.domain.model.Order
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.presentation.screens.list.PageListEvent
import com.itbenevides.genesys21.presentation.screens.list.PageListState
import com.itbenevides.genesys21.presentation.screens.list.components.OrderCardUI
import com.itbenevides.genesys21.presentation.screens.list.components.OrderDetailContent
import com.itbenevides.genesys21.presentation.screens.list.components.OrdersHeaderUI
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysBox
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysColumn
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacer
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText
import com.itbenevides.genesys21.ui.components.molecules.feedback.GenesysEmptyState
import com.itbenevides.genesys21.ui.theme.GenesysTheme
import com.itbenevides.genesys21.ui.theme.GenesysTextStyle

/**
 * Tab de Gestão de Pedidos.
 */
@Composable
fun OrdersTab(
    state: PageListState,
    viewModel: PageViewModel,
    isExpanded: Boolean,
    selectedOrderIdForDetail: String?,
    onSelectOrderForDetail: (String?) -> Unit,
    onEvent: (PageListEvent) -> Unit,
    onContactCustomer: (String, String, String) -> Unit,
    chatMessages: List<ChatMessage>
) {
    val filteredOrders = remember(state.orders, state.searchQuery, state.selectedStatusFilter) {
        state.orders.filter { order ->
            val matchesSearch = state.searchQuery.isBlank() ||
                    order.id.contains(state.searchQuery, ignoreCase = true) ||
                    (order.customerName?.contains(state.searchQuery, ignoreCase = true) == true)
            val matchesStatus = state.selectedStatusFilter == null || order.status == state.selectedStatusFilter
            matchesSearch && matchesStatus
        }
    }

    if (isExpanded) {
        // Layout Master-Detail para Desktop/Tablet
        Row(modifier = Modifier.fillMaxSize()) {
            // Master: Lista de Pedidos
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                contentPadding = PaddingValues(bottom = 64.dp)
            ) {
                item {
                    OrdersHeaderUI(state, onEvent)
                    GenesysSpacer(GenesysTheme.spacing.m)
                }

                if (filteredOrders.isEmpty() && !state.isLoading) {
                    item {
                        GenesysEmptyState(
                            icon = GenesysIcons.SearchOff,
                            title = "Nenhum pedido",
                            description = "Ajuste os filtros."
                        )
                    }
                } else {
                    items(items = filteredOrders, key = { it.id }) { order ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            OrderCardUI(
                                order = order,
                                isSelected = order.id == selectedOrderIdForDetail,
                                onStatusUpdate = { onEvent(PageListEvent.OnUpdateOrderStatus(order.id, it)) },
                                onContact = { onContactCustomer(order.customerPhone ?: "", order.id, order.customerName ?: "Cliente") },
                                onClick = { onSelectOrderForDetail(order.id) }
                            )
                        }
                        GenesysSpacer(GenesysTheme.spacing.s)
                    }
                }
            }

            // Detail: Conteúdo do Pedido
            val selectedOrder = remember(selectedOrderIdForDetail, filteredOrders) {
                filteredOrders.find { it.id == selectedOrderIdForDetail }
            }

            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                if (selectedOrder != null) {
                    OrderDetailContent(
                        order = selectedOrder,
                        chatMessages = chatMessages,
                        onStatusUpdate = { onEvent(PageListEvent.OnUpdateOrderStatus(selectedOrder.id, it)) },
                        onContact = { onContactCustomer(selectedOrder.customerPhone ?: "", selectedOrder.id, selectedOrder.customerName ?: "Cliente") },
                        onSendMessage = { viewModel.sendChatMessage(selectedOrder.id, "Lojista", it, isFromMerchant = true) }
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        GenesysText("Selecione um pedido para ver os detalhes", color = GenesysTheme.colors.outline)
                    }
                }
                GenesysSpacer(GenesysTheme.spacing.huge)
            }
        }
    } else {
        // Layout Single-Column para Mobile
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 64.dp),
        ) {
            item {
                OrdersHeaderUI(state, onEvent)
                GenesysSpacer(GenesysTheme.spacing.m)
            }

            if (filteredOrders.isEmpty() && !state.isLoading) {
                item {
                    GenesysEmptyState(
                        icon = GenesysIcons.SearchOff,
                        title = "Nenhum pedido",
                        description = "Tente ajustar seus filtros.",
                    )
                }
            } else {
                items(items = filteredOrders, key = { it.id }) { order ->
                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        OrderCardUI(
                            order = order,
                            onStatusUpdate = { onEvent(PageListEvent.OnUpdateOrderStatus(order.id, it)) },
                            onContact = { onContactCustomer(order.customerPhone ?: "", order.id, order.customerName ?: "Cliente") },
                        )
                    }
                    GenesysSpacer(GenesysTheme.spacing.m)
                }
            }

            item {
                GenesysSpacer(GenesysTheme.spacing.huge)
            }
        }
    }
}
