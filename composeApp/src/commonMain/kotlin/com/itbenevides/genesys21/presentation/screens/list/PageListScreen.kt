package com.itbenevides.genesys21.presentation.screens.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.itbenevides.genesys21.ui.components.layout.GenesysAlignment
import com.itbenevides.genesys21.ui.components.layout.GenesysColumn
import com.itbenevides.genesys21.ui.components.layout.GenesysDivider
import com.itbenevides.genesys21.ui.components.layout.GenesysPage
import com.itbenevides.genesys21.ui.components.layout.GenesysRow
import com.itbenevides.genesys21.ui.components.layout.GenesysSpacer
import com.itbenevides.genesys21.ui.components.layout.GenesysSpacing
import com.itbenevides.genesys21.ui.components.layout.GenesysWeightBox
import com.itbenevides.genesys21.ui.components.layout.GenesysWeightSpacer
import com.itbenevides.genesys21.ui.components.navigation.GenesysTabData
import com.itbenevides.genesys21.ui.components.navigation.GenesysTabRow
import com.itbenevides.genesys21.ui.components.text.GenesysFontWeight
import com.itbenevides.genesys21.ui.components.text.GenesysText
import com.itbenevides.genesys21.ui.components.text.GenesysTextStyle
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.GenesysDimens
import com.itbenevides.genesys21.ui.theme.GenesysStrings

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
                val newPage = if (event.useTemplate) Page.defaultTemplate(id, state.newPageTitle.trim())
                else Page(id, state.newPageTitle.trim())
                
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
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWideScreen = maxWidth > 1000.dp
            
            GenesysColumn(
                modifier = Modifier.fillMaxWidth(),
                maxWidth = if (isWideScreen) 1200.dp else GenesysDimens.ContentMaxWidth,
                usePadding = false,
                horizontalAlignment = GenesysAlignment.Center
            ) {
                if (state.selectedTab == 0) {
                    PagesTabUI(state, onEvent, onViewPage, onEditPage)
                } else {
                    OrdersTabUI(state, onEvent, isWideScreen) 
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

    if (state.showCreateDialog) CreatePageDialog(state, onEvent)
    if (state.showGlobalSettings && state.pages.isNotEmpty()) GlobalSettingsDialog(state, onEvent)
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
        GenesysText(text = GenesysStrings.ManageVitrines, style = GenesysTextStyle.Headline, fontWeight = GenesysFontWeight.Bold)
        GenesysSpacer(GenesysSpacing.Small)
        GenesysText(text = GenesysStrings.ManageVitrinesSubtitle, style = GenesysTextStyle.Body)
        
        GenesysSpacer(GenesysSpacing.Large)

        if (state.pages.isEmpty() && !state.isLoading) {
            GenesysEmptyState(
                icon = GenesysIcons.WebAssetOff,
                title = GenesysStrings.NoPagesFound,
                description = GenesysStrings.NoPagesDescription
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
    onEvent: (PageListEvent) -> Unit,
    isWideScreen: Boolean
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

    GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = false, useScroll = true) {
        // Dashboard Responsivo
        GenesysRow(modifier = Modifier.fillMaxWidth(), usePadding = true) {
            GenesysWeightBox(if (isWideScreen) 0.5f else 1f) {
                GenesysRow {
                    GenesysWeightBox(1f) {
                        GenesysStatsCard(label = GenesysStrings.Revenue, value = "${GenesysStrings.PricePrefix}$totalRevenue", color = Color(0xFF34C759))
                    }
                    GenesysSpacer(GenesysSpacing.Medium)
                    GenesysWeightBox(1f) {
                        GenesysStatsCard(label = GenesysStrings.Pending, value = totalPending.toString(), color = Color(0xFFFF9500))
                    }
                }
            }
            if (isWideScreen) GenesysWeightSpacer(0.5f)
        }

        // Busca e Filtros
        GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = true) {
            GenesysTextField(
                value = state.searchQuery,
                onValueChange = { onEvent(PageListEvent.OnSearchQueryChanged(it)) },
                label = GenesysStrings.SearchOrdersLabel,
                icon = GenesysIcons.Search
            )

            GenesysSpacer(GenesysSpacing.Medium)

            GenesysRow(modifier = Modifier.fillMaxWidth(), useHorizontalScroll = true) {
                GenesysFilterChip(
                    selected = state.selectedStatusFilter == null,
                    onClick = { onEvent(PageListEvent.OnStatusFilterSelected(null)) },
                    label = GenesysStrings.All
                )

                OrderStatus.entries.forEach { status ->
                    val label = when(status) {
                        OrderStatus.PENDING -> GenesysStrings.StatusPending
                        OrderStatus.PROCESSING -> GenesysStrings.StatusProcessing
                        OrderStatus.COMPLETED -> GenesysStrings.StatusCompleted
                        OrderStatus.CANCELLED -> GenesysStrings.StatusCancelled
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
                description = GenesysStrings.NoOrdersDescription
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
            
            GenesysColumn(modifier = Modifier.weight(1f), usePadding = false) {
                GenesysText(page.title, style = GenesysTextStyle.Body, fontWeight = GenesysFontWeight.Bold)
                GenesysText("${GenesysStrings.SessionIdPrefix}${page.id}", style = GenesysTextStyle.Label)
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
    GenesysCard(modifier = Modifier.fillMaxWidth(), elevation = GenesysDimens.ElevationLow) {
        GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = true) {
            // Cabeçalho iOS Style
            GenesysRow(verticalAlignment = Alignment.CenterVertically) {
                GenesysWeightBox(1f) {
                    GenesysColumn(usePadding = false) {
                        GenesysText(
                            text = "${GenesysStrings.OrderPrefix}${order.id.takeLast(6).uppercase()}", 
                            style = GenesysTextStyle.Label,
                            fontWeight = GenesysFontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (!order.customerName.isNullOrBlank()) {
                            GenesysText(text = order.customerName ?: "", style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold)
                        }
                    }
                }
                GenesysStatusPicker(currentStatus = order.status, onStatusSelected = onStatusUpdate)
            }
            
            GenesysSpacer(GenesysSpacing.Medium)
            GenesysDivider()
            GenesysSpacer(GenesysSpacing.Medium)

            // Tabela de Itens
            order.items.forEach { item ->
                GenesysRow {
                    GenesysText(text = "${item.quantity}x ${item.product.name}", weightValue = 1f)
                    GenesysText(text = "${GenesysStrings.PricePrefix}${item.product.price * item.quantity}", fontWeight = GenesysFontWeight.Bold)
                }
                GenesysSpacer(GenesysSpacing.Small)
            }
            
            GenesysSpacer(GenesysSpacing.Medium)
            GenesysDivider()
            GenesysSpacer(GenesysSpacing.Medium)
            
            // Rodapé com Total e Ação
            GenesysRow(verticalAlignment = Alignment.Bottom) {
                GenesysWeightBox(1f) {
                    GenesysColumn(usePadding = false) {
                        GenesysText(text = GenesysStrings.OrderTotal, style = GenesysTextStyle.Label)
                        GenesysText(
                            text = "${GenesysStrings.PricePrefix}${order.total}", 
                            style = GenesysTextStyle.Headline, 
                            fontWeight = GenesysFontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (!order.whatsappContact.isNullOrBlank()) {
                    GenesysLoadingButton(
                        text = "WhatsApp", // Compacto para o card
                        onClick = { /* Lógica WhatsApp */ },
                        icon = GenesysIcons.Chat
                    )
                }
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
            GenesysColumn(usePadding = false) {
                GenesysLoadingButton(
                    text = GenesysStrings.CreateProfessionalVitrine, 
                    onClick = { onEvent(PageListEvent.OnConfirmCreatePage(useTemplate = true)) }, 
                    enabled = state.newPageTitle.isNotBlank(),
                    isLoading = state.isLoading,
                    fillWidth = true
                ) 
                GenesysSpacer(GenesysSpacing.Small)
                GenesysTextButton(
                    text = GenesysStrings.CreateEmptyVitrine, 
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
            placeholder = GenesysStrings.PageTitlePlaceholder
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
                text = GenesysStrings.Save, 
                onClick = { onEvent(PageListEvent.OnConfirmGlobalSettings(domain, whatsapp)) },
                isLoading = state.isLoading
            ) 
        },
        dismissButton = { GenesysTextButton(text = GenesysStrings.Cancel, onClick = { onEvent(PageListEvent.OnDismissGlobalSettings) }) }
    ) {
        GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = false) {
            GenesysTextField(value = domain, onValueChange = { domain = it }, label = GenesysStrings.CustomDomainLabel)
            GenesysSpacer(GenesysSpacing.Medium)
            GenesysTextField(value = whatsapp, onValueChange = { whatsapp = it }, label = GenesysStrings.WhatsAppLabel)
        }
    }
}
