package com.itbenevides.genesys21.presentation.screens.list

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.Order
import com.itbenevides.genesys21.domain.model.OrderStatus
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.getWebBaseUrl
import com.itbenevides.genesys21.navigation.Route
import com.itbenevides.genesys21.navigation.Router
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.components.appbar.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.button.GenesysIconButton
import com.itbenevides.genesys21.ui.components.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.button.GenesysTextButton
import com.itbenevides.genesys21.ui.components.card.GenesysCard
import com.itbenevides.genesys21.ui.components.card.GenesysStatsCard
import com.itbenevides.genesys21.ui.components.feedback.GenesysDialog
import com.itbenevides.genesys21.ui.components.feedback.GenesysEmptyState
import com.itbenevides.genesys21.ui.components.image.GenesysAvatar
import com.itbenevides.genesys21.ui.components.input.GenesysFilterChip
import com.itbenevides.genesys21.ui.components.input.GenesysStatusPicker
import com.itbenevides.genesys21.ui.components.input.GenesysTextField
import com.itbenevides.genesys21.ui.components.layout.*
import com.itbenevides.genesys21.ui.components.navigation.GenesysTabData
import com.itbenevides.genesys21.ui.components.navigation.GenesysTabRow
import com.itbenevides.genesys21.ui.components.text.GenesysFontWeight
import com.itbenevides.genesys21.ui.components.text.GenesysText
import com.itbenevides.genesys21.ui.components.text.GenesysTextStyle
import com.itbenevides.genesys21.ui.components.text.GenesysTextAlign
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.GenesysDimens
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import org.koin.compose.koinInject

@Composable
fun PageListScreen(
    viewModel: PageViewModel,
    onAddPage: () -> Unit,
    onEditPage: (Page) -> Unit,
    onViewPage: (Page) -> Unit,
    onLogout: () -> Unit
) {
    val pages by viewModel.pages.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val isGlobalLoading by viewModel.isLoading.collectAsState()
    
    var state by remember { mutableStateOf(PageListState()) }

    state = state.copy(
        pages = pages,
        orders = orders,
        isLoading = isGlobalLoading,
        pendingOrdersCount = orders.count { it.status == OrderStatus.PENDING }
    )

    LaunchedEffect(Unit) { 
        viewModel.loadPages() 
        viewModel.loadOrders()
    }

    fun onEvent(event: PageListEvent) {
        when (event) {
            is PageListEvent.OnTabSelected -> state = state.copy(selectedTab = event.index)
            is PageListEvent.OnSearchQueryChanged -> state = state.copy(searchQuery = event.query)
            is PageListEvent.OnStatusFilterSelected -> state = state.copy(selectedStatusFilter = event.status)
            is PageListEvent.OnCreatePageClicked -> state = state.copy(showCreateDialog = true)
            is PageListEvent.OnDismissCreateDialog -> state = state.copy(showCreateDialog = false, newPageTitle = "")
            is PageListEvent.OnNewPageTitleChanged -> state = state.copy(newPageTitle = event.title)
            is PageListEvent.OnConfirmCreatePage -> {
                val id = (1..8).map { "abcdefghijklmnopqrstuvwxyz0123456789".random() }.joinToString("")
                
                // Escolha entre template ou em branco
                val newPage = if (event.useTemplate) {
                    Page.defaultTemplate(id, state.newPageTitle.trim())
                } else {
                    Page(id, state.newPageTitle.trim())
                }
                
                viewModel.savePage(newPage, false) {
                    state = state.copy(showCreateDialog = false, newPageTitle = "")
                    onEditPage(newPage)
                }
            }
            is PageListEvent.OnGlobalSettingsClicked -> state = state.copy(showGlobalSettings = true)
            is PageListEvent.OnDismissGlobalSettings -> state = state.copy(showGlobalSettings = false)
            is PageListEvent.OnConfirmGlobalSettings -> {
                state.pages.firstOrNull()?.let { firstPage ->
                    val updatedPage = firstPage.copy(
                        customDomain = event.domain.ifBlank { null },
                        whatsapp = event.whatsapp.ifBlank { null }
                    )
                    viewModel.savePage(updatedPage, true) {
                        state = state.copy(showGlobalSettings = false)
                        viewModel.loadPages()
                    }
                }
            }
            is PageListEvent.OnDeletePageClicked -> viewModel.deletePage(event.pageId) { viewModel.loadPages() }
            is PageListEvent.OnUpdateOrderStatus -> viewModel.updateOrderStatus(event.orderId, event.newStatus)
            is PageListEvent.OnLogoutClicked -> onLogout()
        }
    }

    PageListContent(state, ::onEvent, onViewPage, onEditPage)
}

@Composable
private fun PageListContent(
    state: PageListState,
    onEvent: (PageListEvent) -> Unit,
    onViewPage: (Page) -> Unit,
    onEditPage: (Page) -> Unit
) {
    val router: Router = koinInject()

    GenesysPage(
        topBar = {
            GenesysColumn(usePadding = false) {
                GenesysTopAppBar(
                    title = GenesysStrings.AdminTitle,
                    onBack = null,
                    actions = {
                        GenesysIconButton(icon = GenesysIcons.Settings, onClick = { onEvent(PageListEvent.OnGlobalSettingsClicked) })
                        GenesysIconButton(icon = GenesysIcons.Add, onClick = { onEvent(PageListEvent.OnCreatePageClicked) })
                    }
                )
                
                GenesysTabRow(
                    selectedTabIndex = state.selectedTab,
                    tabs = listOf(
                        GenesysTabData(GenesysStrings.VitrineTab, GenesysIcons.Web),
                        GenesysTabData(GenesysStrings.OrdersTab, GenesysIcons.List, badgeCount = state.pendingOrdersCount)
                    ),
                    onTabSelected = { onEvent(PageListEvent.OnTabSelected(it)) }
                )
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            GenesysColumn(
                modifier = Modifier.fillMaxWidth(),
                maxWidth = GenesysDimens.ContentMaxWidth,
                usePadding = false,
                horizontalAlignment = GenesysAlignment.Center
            ) {
                if (state.selectedTab == 0) {
                    PagesTabUI(state, onEvent, onViewPage, onEditPage)
                } else {
                    OrdersTabUI(state, onEvent) 
                }
                
                GenesysSpacer(GenesysSpacing.Large)
                GenesysRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    GenesysTextButton(
                        text = GenesysStrings.Logout,
                        onClick = { onEvent(PageListEvent.OnLogoutClicked) }
                    )
                }
                GenesysSpacer(GenesysSpacing.Huge)
            }
        }
    }

    if (state.showCreateDialog) {
        CreatePageDialog(state, onEvent)
    }

    if (state.showGlobalSettings && state.pages.isNotEmpty()) {
        GlobalSettingsDialog(state, onEvent)
    }
}

@Composable
private fun PagesTabUI(
    state: PageListState,
    onEvent: (PageListEvent) -> Unit,
    onViewPage: (Page) -> Unit,
    onEditPage: (Page) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = true) {
        GenesysText(text = "Gerenciar Vitrines", style = GenesysTextStyle.Headline, fontWeight = GenesysFontWeight.Bold)
        GenesysSpacer(GenesysSpacing.Small)
        GenesysText(text = "Crie e gerencie seus canais de venda ativos.", style = GenesysTextStyle.Body)
        
        GenesysSpacer(GenesysSpacing.Large)

        if (state.pages.isEmpty() && !state.isLoading) {
            GenesysEmptyState(
                icon = GenesysIcons.WebAssetOff,
                title = GenesysStrings.NoPagesFound,
                description = "Toque no + para criar sua primeira vitrine."
            )
        } else {
            state.pages.forEach { page ->
                PageItemRow(
                    page = page,
                    onView = { onViewPage(page) },
                    onEdit = { onEditPage(page) },
                    onCopyUrl = { 
                        val baseUrl = getWebBaseUrl()
                        val url = "$baseUrl/p/${page.id}"
                        clipboardManager.setText(AnnotatedString(url))
                    },
                    onDelete = { onEvent(PageListEvent.OnDeletePageClicked(page.id)) }
                )
                GenesysSpacer(GenesysSpacing.Small)
            }
        }
    }
}

@Composable
private fun OrdersTabUI(
    state: PageListState,
    onEvent: (PageListEvent) -> Unit
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

    val totalRevenue = remember(state.orders) { state.orders.filter { it.status == OrderStatus.COMPLETED }.sumOf { it.total } }
    val totalPending = remember(state.orders) { state.orders.count { it.status == OrderStatus.PENDING } }

    // Habilitado scroll nativo do Design System
    GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = false, useScroll = true) {
        // Estatísticas: Dashboard Compacto iOS
        GenesysRow(modifier = Modifier.fillMaxWidth(), usePadding = true) {
            GenesysWeightBox(1f) {
                GenesysStatsCard(label = "Receita", value = "R$ $totalRevenue", color = Color(0xFF34C759))
            }
            GenesysSpacer(GenesysSpacing.Medium)
            GenesysWeightBox(1f) {
                GenesysStatsCard(label = "Pendentes", value = totalPending.toString(), color = Color(0xFFFF9500))
            }
        }

        // Busca e Filtros
        GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = true) {
            GenesysTextField(
                value = state.searchQuery,
                onValueChange = { onEvent(PageListEvent.OnSearchQueryChanged(it)) },
                label = "Buscar pedido ou cliente",
                icon = GenesysIcons.Search
            )

            GenesysSpacer(GenesysSpacing.Medium)

            GenesysRow(modifier = Modifier.fillMaxWidth(), useHorizontalScroll = true) {
                GenesysFilterChip(
                    selected = state.selectedStatusFilter == null,
                    onClick = { onEvent(PageListEvent.OnStatusFilterSelected(null)) },
                    label = "Todos"
                )

                OrderStatus.entries.forEach { status ->
                    val label = when(status) {
                        OrderStatus.PENDING -> "Pendentes"
                        OrderStatus.PROCESSING -> "Em curso"
                        OrderStatus.COMPLETED -> "Concluídos"
                        OrderStatus.CANCELLED -> "Cancelados"
                    }
                    GenesysSpacer(GenesysSpacing.Small)
                    GenesysFilterChip(
                        selected = state.selectedStatusFilter == status,
                        onClick = { onEvent(PageListEvent.OnStatusFilterSelected(status)) },
                        label = label
                    )
                }
            }
        }

        // Lista de Pedidos
        if (filteredOrders.isEmpty() && !state.isLoading) {
            GenesysEmptyState(
                icon = GenesysIcons.SearchOff,
                title = GenesysStrings.NoOrdersFound,
                description = "Nenhum pedido encontrado."
            )
        } else {
            GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = true) {
                filteredOrders.forEach { order ->
                    OrderCardUI(
                        order = order, 
                        onStatusUpdate = { newStatus -> onEvent(PageListEvent.OnUpdateOrderStatus(order.id, newStatus)) }
                    )
                    GenesysSpacer(GenesysSpacing.Medium)
                }
            }
        }
    }
}

@Composable
private fun PageItemRow(
    page: Page,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onCopyUrl: () -> Unit,
    onDelete: () -> Unit
) {
    GenesysCard(modifier = Modifier.fillMaxWidth()) {
        GenesysRow(modifier = Modifier.fillMaxWidth(), usePadding = true) {
            GenesysAvatar(icon = GenesysIcons.Web)
            GenesysSpacer(GenesysSpacing.Medium)
            
            // Usando Column para empilhar Título e ID
            GenesysColumn(modifier = Modifier.weight(1f), usePadding = false) {
                GenesysText(page.title, style = GenesysTextStyle.Body, fontWeight = GenesysFontWeight.Bold)
                GenesysText("ID: ${page.id}", style = GenesysTextStyle.Label)
            }
            
            GenesysRow(fillWidth = false) {
                GenesysIconButton(icon = GenesysIcons.Visibility, onClick = onView)
                GenesysIconButton(icon = GenesysIcons.Edit, onClick = onEdit)
                GenesysIconButton(icon = GenesysIcons.Copy, onClick = onCopyUrl)
                GenesysIconButton(icon = GenesysIcons.Delete, onClick = onDelete, tint = Color(0xFFFF3B30))
            }
        }
    }
}

@Composable
private fun OrderCardUI(order: Order, onStatusUpdate: (OrderStatus) -> Unit) {
    // ESTRUTURA VERTICAL PURA: Garante estabilidade no WasmJs e remove sobreposição
    GenesysCard(modifier = Modifier.fillMaxWidth(), elevation = GenesysDimens.ElevationLow) {
        GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = true) {
            // Seção 1: Identificação (Vertical)
            GenesysText(
                text = "PEDIDO #${order.id.takeLast(6).uppercase()}", 
                style = GenesysTextStyle.Label,
                fontWeight = GenesysFontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            if (!order.customerName.isNullOrBlank()) {
                GenesysText(
                    text = order.customerName ?: "", 
                    style = GenesysTextStyle.Title, 
                    fontWeight = GenesysFontWeight.Bold
                )
            }
            
            GenesysSpacer(GenesysSpacing.Small)
            
            // Seção 2: Status
            GenesysStatusPicker(
                currentStatus = order.status,
                onStatusSelected = onStatusUpdate
            )
            
            GenesysSpacer(GenesysSpacing.Medium)
            GenesysDivider()
            GenesysSpacer(GenesysSpacing.Medium)

            // Seção 3: Itens (Um embaixo do outro)
            order.items.forEach { item ->
                GenesysText(
                    text = "${item.quantity}x ${item.product.name}", 
                    style = GenesysTextStyle.Body
                )
                GenesysText(
                    text = "R$ ${item.product.price * item.quantity}",
                    style = GenesysTextStyle.Label,
                    fontWeight = GenesysFontWeight.Bold
                )
                GenesysSpacer(GenesysSpacing.Small)
            }
            
            GenesysDivider()
            GenesysSpacer(GenesysSpacing.Medium)
            
            // Seção 4: Total
            GenesysText(text = "Total do Pedido", style = GenesysTextStyle.Label)
            GenesysText(
                text = "R$ ${order.total}", 
                style = GenesysTextStyle.Headline, 
                fontWeight = GenesysFontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            
            if (!order.whatsappContact.isNullOrBlank()) {
                GenesysSpacer(GenesysSpacing.Large)
                GenesysLoadingButton(
                    text = "Conversar no WhatsApp",
                    onClick = { /* Lógica WhatsApp */ },
                    fillWidth = true,
                    icon = GenesysIcons.Chat
                )
            }
        }
    }
}

@Composable
private fun CreatePageDialog(state: PageListState, onEvent: (PageListEvent) -> Unit) {
    GenesysDialog(
        onDismissRequest = { onEvent(PageListEvent.OnDismissCreateDialog) },
        title = GenesysStrings.NewPageTitle,
        confirmButton = { 
            // Layout vertical para os botões de criação
            GenesysColumn(usePadding = false) {
                GenesysLoadingButton(
                    text = "Criar Vitrine Profissional", 
                    onClick = { onEvent(PageListEvent.OnConfirmCreatePage(useTemplate = true)) }, 
                    enabled = state.newPageTitle.isNotBlank(),
                    isLoading = state.isLoading,
                    fillWidth = true
                ) 
                GenesysSpacer(GenesysSpacing.Small)
                GenesysTextButton(
                    text = "Criar em Branco", 
                    onClick = { onEvent(PageListEvent.OnConfirmCreatePage(useTemplate = false)) },
                    enabled = state.newPageTitle.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        dismissButton = { GenesysTextButton(text = GenesysStrings.Cancel, onClick = { onEvent(PageListEvent.OnDismissCreateDialog) }) }
    ) {
        GenesysTextField(
            value = state.newPageTitle, 
            onValueChange = { onEvent(PageListEvent.OnNewPageTitleChanged(it)) }, 
            label = GenesysStrings.PageTitleLabel,
            placeholder = "Ex: Minha Loja Premium"
        )
    }
}

@Composable
private fun GlobalSettingsDialog(state: PageListState, onEvent: (PageListEvent) -> Unit) {
    val firstPage = state.pages.firstOrNull()
    var domain by remember { mutableStateOf(firstPage?.customDomain ?: "") }
    var whatsapp by remember { mutableStateOf(firstPage?.whatsapp ?: "") }

    GenesysDialog(
        onDismissRequest = { onEvent(PageListEvent.OnDismissGlobalSettings) },
        title = GenesysStrings.GlobalSettings,
        confirmButton = { 
            GenesysLoadingButton(
                text = "Salvar", 
                onClick = { onEvent(PageListEvent.OnConfirmGlobalSettings(domain, whatsapp)) },
                isLoading = state.isLoading
            ) 
        },
        dismissButton = { GenesysTextButton(text = GenesysStrings.Cancel, onClick = { onEvent(PageListEvent.OnDismissGlobalSettings) }) }
    ) {
        GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = false) {
            GenesysTextField(value = domain, onValueChange = { domain = it }, label = "Domínio Customizado")
            GenesysSpacer(GenesysSpacing.Medium)
            GenesysTextField(value = whatsapp, onValueChange = { whatsapp = it }, label = "WhatsApp")
        }
    }
}
