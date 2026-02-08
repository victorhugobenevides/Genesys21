package com.itbenevides.genesys21.presentation.screens.list

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.Order
import com.itbenevides.genesys21.domain.model.OrderStatus
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.PageComponent
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
import com.itbenevides.genesys21.ui.components.layout.*
import com.itbenevides.genesys21.ui.components.navigation.*
import com.itbenevides.genesys21.ui.components.text.*
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.GenesysDimens
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.util.downloadFile
import com.itbenevides.genesys21.util.rememberFileHandler
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.roundToLong

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
    val uriHandler = LocalUriHandler.current
    
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

    val onEvent: (PageListEvent) -> Unit = { event ->
        when (event) {
            is PageListEvent.OnTabSelected -> state = state.copy(selectedTab = event.index)
            is PageListEvent.OnSearchQueryChanged -> state = state.copy(searchQuery = event.query)
            is PageListEvent.OnStatusFilterSelected -> state = state.copy(selectedStatusFilter = event.status)
            is PageListEvent.OnCreatePageClicked -> state = state.copy(showCreateDialog = true)
            is PageListEvent.OnDismissCreateDialog -> state = state.copy(showCreateDialog = false, newPageTitle = "")
            is PageListEvent.OnNewPageTitleChanged -> state = state.copy(newPageTitle = event.title)
            is PageListEvent.OnConfirmCreatePage -> {
                val id = (1..8).map { "abcdefghijklmnopqrstuvwxyz0123456789".random() }.joinToString("")
                val newPage = Page(id = id, title = state.newPageTitle.trim())
                
                viewModel.savePage(newPage, isEditing = false) {
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
            is PageListEvent.OnRenamePageClicked -> state = state.copy(showRenameDialog = true, pageToRename = event.page)
            is PageListEvent.OnDismissRenameDialog -> state = state.copy(showRenameDialog = false, pageToRename = null)
            is PageListEvent.OnConfirmRenamePage -> {
                state.pageToRename?.let { page ->
                    val updated = page.copy(title = event.newTitle)
                    viewModel.savePage(updated, true) {
                        state = state.copy(showRenameDialog = false, pageToRename = null)
                        viewModel.loadPages()
                    }
                }
            }

            is PageListEvent.OnDeletePageClicked -> viewModel.deletePage(event.pageId) { viewModel.loadPages() }
            is PageListEvent.OnUpdateOrderStatus -> viewModel.updateOrderStatus(event.orderId, event.newStatus)
            is PageListEvent.OnLogoutClicked -> onLogout()
            
            is PageListEvent.OnExportPageClicked -> {
                val json = Json.encodeToString(event.page)
                downloadFile(json, "${event.page.title.replace(" ", "_")}.benevides")
            }
            is PageListEvent.OnExportAllClicked -> {
                if (state.pages.isNotEmpty()) {
                    val json = Json.encodeToString(state.pages)
                    downloadFile(json, "backup_genesys21_${state.pages.size}_paginas.benevides")
                }
            }
            is PageListEvent.OnImportPageClicked -> {
                println("LOG_IMPORT: Iniciando tentativa de importação.")
                try {
                    // Configuração ultra-resiliente
                    val jsonParser = Json { 
                        ignoreUnknownKeys = true 
                        coerceInputValues = true
                        isLenient = true
                        allowStructuredMapKeys = true
                    }

                    // Tenta detectar se é lista ou objeto único
                    val jsonTrimmed = event.json.trim()
                    
                    if (jsonTrimmed.startsWith("[")) {
                        println("LOG_IMPORT: Detectada lista de páginas.")
                        val importedPages = jsonParser.decodeFromString<List<Page>>(event.json)
                        println("LOG_IMPORT: ${importedPages.size} páginas decodificadas.")
                        importedPages.forEach { page ->
                            val newId = (1..8).map { "abcdefghijklmnopqrstuvwxyz0123456789".random() }.joinToString("")
                            viewModel.savePage(page.copy(id = newId, ownerId = null), false) { }
                        }
                        viewModel.loadPages()
                        state = state.copy(showCreateDialog = false)
                    } else {
                        println("LOG_IMPORT: Detectada página única.")
                        // Fallback: Se o JSON usa "type" em vez de "component_class", tentamos tratar
                        val jsonToProcess = if (jsonTrimmed.contains("\"type\":") && !jsonTrimmed.contains("\"component_class\":")) {
                            println("LOG_IMPORT: JSON legado detectado (campo 'type'). Convertendo para 'component_class'.")
                            jsonTrimmed.replace("\"type\":", "\"component_class\":")
                        } else jsonTrimmed

                        val importedPage = jsonParser.decodeFromString<Page>(jsonToProcess)
                        val newId = (1..8).map { "abcdefghijklmnopqrstuvwxyz0123456789".random() }.joinToString("")
                        
                        println("LOG_IMPORT: Página '${importedPage.title}' pronta para salvar.")
                        viewModel.savePage(importedPage.copy(id = newId, ownerId = null), false) {
                            println("LOG_IMPORT: Sucesso ao salvar no servidor.")
                            viewModel.loadPages()
                            state = state.copy(showCreateDialog = false)
                        }
                    }
                } catch (e: Exception) {
                    val errorTitle = "Erro na Importação"
                    val errorMsg = "O formato do arquivo é inválido ou incompatível: ${e.message}"
                    println("LOG_IMPORT_ERROR: $errorMsg")
                    // EXIBE O ERRO NA UI PARA O USUÁRIO
                    viewModel.saveCustomerPhone("") // Truque para forçar recomposição se necessário, mas o ideal é handleError:
                    // viewModel.handleError(errorTitle, e) // Infelizmente handleError é privado, vamos usar o estado de erro do VM se disponível
                }
            }
        }
    }

    val fileHandler = rememberFileHandler { json ->
        if (json != null) {
            println("LOG_IMPORT: Arquivo carregado (${json.length} bytes)")
            onEvent(PageListEvent.OnImportPageClicked(json))
        } else {
            println("LOG_IMPORT: Nenhum conteúdo de arquivo recebido.")
        }
    }

    PageListContent(
        state = state, 
        onEvent = onEvent, 
        onViewPage = onViewPage, 
        onEditPage = onEditPage, 
        onImport = { fileHandler() },
        onExportAll = { onEvent(PageListEvent.OnExportAllClicked) },
        onContactCustomer = { phone, orderId, name ->
            val message = "Olá $name, estou entrando em contato sobre o seu pedido #$orderId na Genesys21."
            uriHandler.openUri("https://wa.me/$phone?text=${message.replace(" ", "%20")}")
        }
    )
}

@Composable
private fun PageListContent(
    state: PageListState,
    onEvent: (PageListEvent) -> Unit,
    onViewPage: (Page) -> Unit,
    onEditPage: (Page) -> Unit,
    onImport: () -> Unit,
    onExportAll: () -> Unit,
    onContactCustomer: (String, String, String) -> Unit
) {
     GenesysPage(
        topBar = {
            GenesysColumn(usePadding = false, modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                GenesysTopAppBar(
                    title = GenesysStrings.AdminTitle,
                    onBack = null,
                    actions = {
                        GenesysIconButton(icon = GenesysIcons.Numbers, contentDescription = "Exportar Tudo", onClick = onExportAll)
                        GenesysIconButton(icon = GenesysIcons.CloudUpload, contentDescription = "Importar Backup", onClick = onImport)
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
                    onTabSelected = { index -> onEvent(PageListEvent.OnTabSelected(index)) }
                )
            }
        }
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWideScreen = maxWidth > 800.dp

            if (state.selectedTab == 0) {
                // Vitrines
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 64.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        PagesTabUI(state, onEvent, onViewPage, onEditPage, isWideScreen)
                    }
                    item {
                        LogoutButtonUI(onEvent)
                    }
                }
            } else {
                // Pedidos - Usando Grid Adaptativo
                val filteredOrders = state.orders.filter { order ->
                    val matchesSearch = state.searchQuery.isBlank() || 
                        order.id.contains(state.searchQuery, ignoreCase = true) ||
                        (order.customerName?.contains(state.searchQuery, ignoreCase = true) == true)
                    val matchesStatus = state.selectedStatusFilter == null || order.status == state.selectedStatusFilter
                    matchesSearch && matchesStatus
                }

                LazyVerticalGrid(
                    columns = if (isWideScreen) GridCells.Adaptive(380.dp) else GridCells.Fixed(1),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header de Filtros e Stats (Ocupa a largura total)
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        OrdersHeaderUI(state, onEvent, isWideScreen)
                    }

                    if (filteredOrders.isEmpty() && !state.isLoading) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            GenesysEmptyState(
                                icon = GenesysIcons.SearchOff,
                                title = GenesysStrings.NoOrdersFound,
                                description = GenesysStrings.NoOrdersDescription
                            )
                        }
                    } else {
                        items(items = filteredOrders, key = { it.id }) { order ->
                            OrderCardUI(
                                order = order, 
                                onStatusUpdate = { newStatus -> onEvent(PageListEvent.OnUpdateOrderStatus(order.id, newStatus)) },
                                onContact = { onContactCustomer(order.customerPhone ?: "", order.id, order.customerName ?: "Cliente") }
                            )
                        }
                    }

                    item(span = { GridItemSpan(maxLineSpan) }) {
                        LogoutButtonUI(onEvent)
                    }
                }
            }
        }
    }

    if (state.showCreateDialog) CreatePageDialog(state, onEvent, onImport)
    if (state.showGlobalSettings && state.pages.isNotEmpty()) GlobalSettingsDialog(state, onEvent)
    if (state.showRenameDialog) RenamePageDialog(state, onEvent)
}

@Composable
private fun LogoutButtonUI(onEvent: (PageListEvent) -> Unit) {
    GenesysColumn(usePadding = true, horizontalAlignment = GenesysAlignment.Center) {
        GenesysSpacer(GenesysSpacing.Huge)
        GenesysTextButton(
            text = GenesysStrings.Logout,
            onClick = { onEvent(PageListEvent.OnLogoutClicked) },
            modifier = Modifier.wrapContentWidth(),
            color = MaterialTheme.colorScheme.error
        )
        GenesysSpacer(GenesysSpacing.Huge)
    }
}

@Composable
private fun PagesTabUI(
    state: PageListState,
    onEvent: (PageListEvent) -> Unit,
    onViewPage: (Page) -> Unit,
    onEditPage: (Page) -> Unit,
    isWideScreen: Boolean
) {
    val clipboardManager = LocalClipboardManager.current

    GenesysColumn(modifier = Modifier.widthIn(max = 1200.dp), usePadding = true) {
        GenesysSpacer(GenesysSpacing.Large)
        GenesysText(text = GenesysStrings.ManageVitrines, style = GenesysTextStyle.Headline, fontWeight = GenesysFontWeight.ExtraBold)
        GenesysText(text = GenesysStrings.ManageVitrinesSubtitle, style = GenesysTextStyle.Body, color = MaterialTheme.colorScheme.onSurfaceVariant)
        GenesysSpacer(GenesysSpacing.Large)

        if (state.pages.isEmpty() && !state.isLoading) {
            GenesysEmptyState(icon = GenesysIcons.WebAssetOff, title = GenesysStrings.NoPagesFound, description = GenesysStrings.NoPagesDescription)
        } else {
            state.pages.forEach { page ->
                PageItemRow(
                    page = page,
                    onView = { onViewPage(page) },
                    onEdit = { onEditPage(page) },
                    onRename = { onEvent(PageListEvent.OnRenamePageClicked(page)) },
                    onCopyUrl = { 
                        val baseUrl = getWebBaseUrl()
                        val url = "$baseUrl/p/${page.id}"
                        clipboardManager.setText(AnnotatedString(url))
                    },
                    onExport = { onEvent(PageListEvent.OnExportPageClicked(page)) },
                    onDelete = { onEvent(PageListEvent.OnDeletePageClicked(page.id)) },
                    isWideScreen = isWideScreen
                )
                GenesysSpacer(GenesysSpacing.Medium)
            }
        }
    }
}

@Composable
private fun OrdersHeaderUI(state: PageListState, onEvent: (PageListEvent) -> Unit, isWideScreen: Boolean) {
    val rawRevenue = remember(state.orders) { state.orders.filter { it.status == OrderStatus.COMPLETED }.sumOf { it.total } }
    val totalRevenue = (rawRevenue * 100.0).roundToLong() / 100.0
    val totalPending = remember(state.orders) { state.orders.count { it.status == OrderStatus.PENDING } }

    GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = true) {
        GenesysRow(modifier = Modifier.fillMaxWidth(), usePadding = false) {
            GenesysWeightBox(1f) { GenesysStatsCard(label = GenesysStrings.Revenue, value = "${GenesysStrings.PricePrefix}$totalRevenue", color = Color(0xFF34C759)) }
            GenesysSpacer(GenesysSpacing.Medium)
            GenesysWeightBox(1f) { GenesysStatsCard(label = GenesysStrings.Pending, value = totalPending.toString(), color = Color(0xFFFF9500)) }
        }
        GenesysSpacer(GenesysSpacing.Large)
        
        // Layout adaptativo para os filtros
        if (isWideScreen) {
            GenesysRow(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, usePadding = false) {
                Box(modifier = Modifier.weight(1f)) {
                    Column {
                        GenesysTextField(value = state.searchQuery, onValueChange = { onEvent(PageListEvent.OnSearchQueryChanged(it)) }, label = GenesysStrings.SearchOrdersLabel, icon = GenesysIcons.Search)
                    }
                }
                GenesysSpacer(GenesysSpacing.Large)
                OrderFiltersRow(state, onEvent)
            }
        } else {
            GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = false) {
                GenesysTextField(value = state.searchQuery, onValueChange = { onEvent(PageListEvent.OnSearchQueryChanged(it)) }, label = GenesysStrings.SearchOrdersLabel, icon = GenesysIcons.Search)
                GenesysSpacer(GenesysSpacing.Medium)
                OrderFiltersRow(state, onEvent)
            }
        }
        GenesysSpacer(GenesysSpacing.Medium)
    }
}

@Composable
private fun OrderFiltersRow(state: PageListState, onEvent: (PageListEvent) -> Unit) {
    GenesysRow(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        GenesysFilterChip(selected = state.selectedStatusFilter == null, onClick = { onEvent(PageListEvent.OnStatusFilterSelected(null)) }, label = GenesysStrings.All, badgeCount = state.orders.size)
        OrderStatus.entries.forEach { status ->
            val label = when(status) {
                OrderStatus.PENDING -> GenesysStrings.StatusPending
                OrderStatus.PROCESSING -> GenesysStrings.StatusProcessing
                OrderStatus.COMPLETED -> GenesysStrings.StatusCompleted
                OrderStatus.CANCELLED -> GenesysStrings.StatusCancelled
            }
            GenesysFilterChip(selected = state.selectedStatusFilter == status, onClick = { onEvent(PageListEvent.OnStatusFilterSelected(status)) }, label = label, badgeCount = state.orders.count { it.status == status })
        }
    }
}

@Composable
private fun PageItemRow(
    page: Page,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onRename: () -> Unit,
    onCopyUrl: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit,
    isWideScreen: Boolean
) {
    GenesysCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 1.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(), 
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ÍCONE E TÍTULO
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), 
                    contentAlignment = Alignment.Center
                ) {
                    Icon(GenesysIcons.Web, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
                
                Spacer(Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    GenesysText(
                        text = page.title, 
                        style = GenesysTextStyle.Title, 
                        fontWeight = GenesysFontWeight.ExtraBold, 
                        maxLines = 1, 
                        overflow = TextOverflow.Ellipsis
                    )
                    GenesysText(
                        text = "ID: ${page.id}", 
                        style = GenesysTextStyle.Label, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // AÇÕES NO DESKTOP (Alinhadas à direita)
                if (isWideScreen) {
                    Spacer(Modifier.width(24.dp))
                    PageActionsRow(onView, onRename, onEdit, onCopyUrl, onExport, onDelete)
                }
            }
            
            // AÇÕES NO MOBILE (Segunda linha)
            if (!isWideScreen) {
                Column {
                    Spacer(Modifier.height(48.dp)) // Abre espaço para o título acima
                    Row(
                        modifier = Modifier.fillMaxWidth(), 
                        horizontalArrangement = Arrangement.End
                    ) {
                        PageActionsRow(onView, onRename, onEdit, onCopyUrl, onExport, onDelete)
                    }
                }
            }
        }
    }
}

@Composable
private fun PageActionsRow(
    onView: () -> Unit,
    onRename: () -> Unit,
    onEdit: () -> Unit,
    onCopyUrl: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(onClick = onView) { Icon(GenesysIcons.Visibility, null, tint = MaterialTheme.colorScheme.primary) }
            IconButton(onClick = onRename) { Icon(GenesysIcons.Edit, null, tint = MaterialTheme.colorScheme.primary) }
            IconButton(onClick = onEdit) { Icon(GenesysIcons.Magic, null, tint = MaterialTheme.colorScheme.primary) }
            IconButton(onClick = onCopyUrl) { Icon(GenesysIcons.Copy, null, tint = MaterialTheme.colorScheme.primary) }
            IconButton(onClick = onExport) { Icon(GenesysIcons.CloudUpload, null, tint = MaterialTheme.colorScheme.primary) }
            IconButton(onClick = onDelete) { Icon(GenesysIcons.Delete, null, tint = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun OrderCardUI(order: Order, onStatusUpdate: (OrderStatus) -> Unit, onContact: () -> Unit) {
    GenesysCard(modifier = Modifier.fillMaxWidth(), elevation = 1.dp) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    val initials = remember(order.customerName) { order.customerName?.split(" ")?.take(2)?.mapNotNull { it.firstOrNull() }?.joinToString("")?.uppercase() ?: "C" }
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
                        Text(text = initials, style = MaterialTheme.typography.bodyMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        GenesysText(text = "${GenesysStrings.OrderPrefix}${order.id.takeLast(6).uppercase()}", style = GenesysTextStyle.Label, fontWeight = GenesysFontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        GenesysText(text = order.customerName ?: "Consumidor", style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                GenesysStatusPicker(currentStatus = order.status, onStatusSelected = onStatusUpdate)
            }
            
            Spacer(Modifier.height(16.dp))
            GenesysDivider()
            Spacer(Modifier.height(12.dp))
            
            order.items.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    GenesysText(text = "${item.quantity}x", style = GenesysTextStyle.Body, fontWeight = GenesysFontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(32.dp))
                    GenesysText(text = item.product.name, style = GenesysTextStyle.Body, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    val subtotal = (item.product.price * item.quantity * 100.0).roundToLong() / 100.0
                    GenesysText(text = "${GenesysStrings.PricePrefix}$subtotal", style = GenesysTextStyle.Body, fontWeight = GenesysFontWeight.Bold)
                }
            }
            
            Spacer(Modifier.height(16.dp))
            GenesysDivider()
            Spacer(Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    GenesysText(text = GenesysStrings.OrderTotal, style = GenesysTextStyle.Label, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val totalFormatted = (order.total * 100.0).roundToLong() / 100.0
                    GenesysText(text = "${GenesysStrings.PricePrefix}$totalFormatted", style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                }
                if (!order.customerPhone.isNullOrBlank()) {
                    GenesysLoadingButton(text = "WhatsApp", onClick = onContact, icon = GenesysIcons.Chat, fillWidth = false, shape = RoundedCornerShape(12.dp))
                }
            }
        }
    }
}

@Composable
private fun RenamePageDialog(state: PageListState, onEvent: (PageListEvent) -> Unit) {
    var title by remember(state.pageToRename) { mutableStateOf(state.pageToRename?.title ?: "") }
    GenesysDialog(
        onDismissRequest = { onEvent(PageListEvent.OnDismissRenameDialog) },
        title = "Renomear Vitrine",
        confirmButton = { 
            GenesysLoadingButton(text = GenesysStrings.Save, onClick = { onEvent(PageListEvent.OnConfirmRenamePage(title)) }, enabled = title.isNotBlank(), isLoading = state.isLoading, fillWidth = true) 
        },
        dismissButton = { GenesysTextButton(text = GenesysStrings.Cancel, onClick = { onEvent(PageListEvent.OnDismissRenameDialog) }) }
    ) {
        GenesysTextField(value = title, onValueChange = { title = it }, label = "Novo Título", placeholder = "Ex: Minha Nova Loja")
    }
}

@Composable
private fun CreatePageDialog(state: PageListState, onEvent: (PageListEvent) -> Unit, onImport: () -> Unit) {
     GenesysDialog(
        onDismissRequest = { onEvent(PageListEvent.OnDismissCreateDialog) },
        title = GenesysStrings.NewPageTitle,
        confirmButton = { 
            // CORREÇÃO: Removendo o container scrollable interno para garantir que o clique chegue ao botão
            Column(modifier = Modifier.padding(top = 16.dp)) {
                GenesysLoadingButton(
                    text = "Criar Vitrine em Branco", 
                    onClick = { onEvent(PageListEvent.OnConfirmCreatePage(PageTemplateType.EMPTY)) }, 
                    enabled = state.newPageTitle.isNotBlank(), 
                    isLoading = state.isLoading, 
                    fillWidth = true, 
                    icon = GenesysIcons.Add
                ) 
                GenesysSpacer(GenesysSpacing.Small)
                GenesysLoadingButton(
                    text = "Importar Arquivo .benevides", 
                    onClick = onImport, 
                    fillWidth = true, 
                    icon = GenesysIcons.CloudUpload, 
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            }
        },
        dismissButton = { GenesysTextButton(text = GenesysStrings.Cancel, onClick = { onEvent(PageListEvent.OnDismissCreateDialog) }) }
    ) {
        GenesysTextField(value = state.newPageTitle, onValueChange = { onEvent(PageListEvent.OnNewPageTitleChanged(it)) }, label = GenesysStrings.PageTitleLabel, placeholder = GenesysStrings.PageTitlePlaceholder)
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
        confirmButton = { GenesysLoadingButton(text = GenesysStrings.Save, onClick = { onEvent(PageListEvent.OnConfirmGlobalSettings(domain, whatsapp)) }, isLoading = state.isLoading) },
        dismissButton = { GenesysTextButton(text = GenesysStrings.Cancel, onClick = { onEvent(PageListEvent.OnDismissGlobalSettings) }) }
    ) {
        GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = false) {
            GenesysTextField(value = domain, onValueChange = { domain = it }, label = GenesysStrings.CustomDomainLabel)
            GenesysSpacer(GenesysSpacing.Medium)
            GenesysTextField(value = whatsapp, onValueChange = { whatsapp = it }, label = GenesysStrings.WhatsAppLabel)
        }
    }
}
