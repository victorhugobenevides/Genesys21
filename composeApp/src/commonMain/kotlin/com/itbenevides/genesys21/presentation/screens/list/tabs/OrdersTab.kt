package com.itbenevides.genesys21.presentation.screens.list.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 64.dp),
        ) {
            item {
                GenesysColumn(modifier = Modifier.widthIn(max = 1200.dp), usePadding = false) {
                    OrdersHeaderUI(state, onEvent)
                }
            }

            val filteredOrders =
                state.orders.filter { order ->
                    val matchesSearch =
                        state.searchQuery.isBlank() ||
                            order.id.contains(state.searchQuery, ignoreCase = true) ||
                            (order.customerName?.contains(state.searchQuery, ignoreCase = true) == true)
                    val matchesStatus = state.selectedStatusFilter == null || order.status == state.selectedStatusFilter
                    matchesSearch && matchesStatus
                }

            if (filteredOrders.isEmpty() && !state.isLoading) {
                item {
                    GenesysEmptyState(
                        icon = GenesysIcons.SearchOff,
                        title = "Nenhum pedido encontrado",
                        description = "Tente ajustar seus filtros de busca.",
                    )
                }
            } else {
                if (isExpanded) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth().heightIn(min = 600.dp)) {
                            Column(modifier = Modifier.weight(1f).padding(16.dp)) {
                                filteredOrders.forEach { order ->
                                    OrderCardUI(
                                        order = order,
                                        isSelected = order.id == selectedOrderIdForDetail,
                                        onStatusUpdate = { newStatus: OrderStatus ->
                                            onEvent(PageListEvent.OnUpdateOrderStatus(order.id, newStatus))
                                        },
                                        onContact = {
                                            onContactCustomer(order.customerPhone ?: "", order.id, order.customerName ?: "Cliente")
                                        },
                                        onClick = {
                                            onSelectOrderForDetail(order.id)
                                        }
                                    )
                                    GenesysSpacer(GenesysTheme.spacing.s)
                                }
                            }

                            Column(modifier = Modifier.weight(1.5f).padding(16.dp)) {
                                val selectedOrder = filteredOrders.find { it.id == selectedOrderIdForDetail }
                                if (selectedOrder != null) {
                                    OrderDetailContent(
                                        order = selectedOrder,
                                        chatMessages = chatMessages,
                                        onStatusUpdate = { newStatus: OrderStatus ->
                                            onEvent(PageListEvent.OnUpdateOrderStatus(selectedOrder.id, newStatus))
                                        },
                                        onContact = {
                                            onContactCustomer(selectedOrder.customerPhone ?: "", selectedOrder.id, selectedOrder.customerName ?: "Cliente")
                                        },
                                        onSendMessage = { content: String ->
                                            viewModel.sendChatMessage(selectedOrder.id, "Lojista", content, isFromMerchant = true)
                                        }
                                    )
                                } else {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        GenesysText("Selecione um pedido para ver os detalhes", color = GenesysTheme.colors.outline)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    items(items = filteredOrders, key = { it.id }) { order: Order ->
                        GenesysBox(modifier = Modifier.widthIn(max = 1200.dp).padding(horizontal = 16.dp)) {
                            OrderCardUI(
                                order = order,
                                onStatusUpdate = { newStatus: OrderStatus ->
                                    onEvent(PageListEvent.OnUpdateOrderStatus(order.id, newStatus))
                                },
                                onContact = {
                                    onContactCustomer(order.customerPhone ?: "", order.id, order.customerName ?: "Cliente")
                                },
                            )
                        }
                        GenesysSpacer(GenesysTheme.spacing.m)
                    }
                }
            }
        }
    }
}
