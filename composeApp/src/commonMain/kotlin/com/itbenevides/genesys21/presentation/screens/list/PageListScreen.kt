package com.itbenevides.genesys21.presentation.screens.list

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
                val newPage = when(event.templateType) {
                    PageTemplateType.PROFESSIONAL_VITRINE -> Page.defaultTemplate(id, state.newPageTitle.trim())
                    PageTemplateType.BIO_PROFILE -> Page.profileTemplate(id, state.newPageTitle.trim())
                    PageTemplateType.EMPTY -> Page(id, state.newPageTitle.trim())
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
            
            // Lógica de Exportação Individual
            is PageListEvent.OnExportPageClicked -> {
                val json = Json.encodeToString(event.page)
                downloadFile(json, "${event.page.title.replace(" ", "_")}.benevides")
            }

            // Lógica de Exportação de Backup (Todas as páginas)
            is PageListEvent.OnExportAllClicked -> {
                if (state.pages.isNotEmpty()) {
                    val json = Json.encodeToString(state.pages)
                    downloadFile(json, "backup_genesys21_${state.pages.size}_paginas.benevides")
                }
            }
            
            // Lógica de Importação (Individual ou Backup)
            is PageListEvent.OnImportPageClicked -> {
                try {
                    val importedPages = runCatching { Json.decodeFromString<List<Page>>(event.json) }.getOrNull()
                    
                    if (importedPages != null) {
                        importedPages.forEach { page ->
                            val newId = (1..8).map { "abcdefghijklmnopqrstuvwxyz0123456789".random() }.joinToString("")
                            viewModel.savePage(page.copy(id = newId), false) { }
                        }
                        viewModel.loadPages()
                        state = state.copy(showCreateDialog = false)
                    } else {
                        val importedPage = Json.decodeFromString<Page>(event.json)
                        val newId = (1..8).map { "abcdefghijklmnopqrstuvwxyz0123456789".random() }.joinToString("")
                        viewModel.savePage(importedPage.copy(id = newId), false) {
                            viewModel.loadPages()
                            state = state.copy(showCreateDialog = false)
                        }
                    }
                } catch (e: Exception) { }
            }
        }
    }

    val fileHandler = rememberFileHandler { json ->
        json?.let { onEvent(PageListEvent.OnImportPageClicked(it)) }
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
                        // BOTÕES DE BACKUP GLOBAL
                        GenesysIconButton(
                            icon = GenesysIcons.Numbers,
                            contentDescription = "Exportar Tudo",
                            onClick = onExportAll
                        )
                        GenesysIconButton(
                            icon = GenesysIcons.CloudUpload, 
                            contentDescription = "Importar Backup",
                            onClick = onImport
                        )
                        
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
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(bottom = 64.dp)
            ) {
                item {
                    GenesysColumn(
                        modifier = Modifier.widthIn(max = 1200.dp),
                        usePadding = false
                    ) {
                        if (state.selectedTab == 0) {
                            PagesTabUI(state, onEvent, onViewPage, onEditPage)
                        } else {
                            OrdersHeaderUI(state, onEvent)
                        }
                    }
                }

                if (state.selectedTab == 1) {
                    val filteredOrders = state.orders.filter { order ->
                        val matchesSearch = state.searchQuery.isBlank() || 
                            order.id.contains(state.searchQuery, ignoreCase = true) ||
                            (order.customerName?.contains(state.searchQuery, ignoreCase = true) == true)
                        val matchesStatus = state.selectedStatusFilter == null || order.status == state.selectedStatusFilter
                        matchesSearch && matchesStatus
                    }

                    if (filteredOrders.isEmpty() && !state.isLoading) {
                        item {
                            GenesysEmptyState(
                                icon = GenesysIcons.SearchOff,
                                title = GenesysStrings.NoOrdersFound,
                                description = GenesysStrings.NoOrdersDescription
                            )
                        }
                    } else {
                        items(items = filteredOrders, key = { it.id }) { order ->
                            GenesysBox(modifier = Modifier.widthIn(max = 1200.dp).padding(horizontal = 16.dp)) {
                                OrderCardUI(
                                    order = order, 
                                    onStatusUpdate = { newStatus -> onEvent(PageListEvent.OnUpdateOrderStatus(order.id, newStatus)) },
                                    onContact = { onContactCustomer(order.customerPhone ?: "", order.id, order.customerName ?: "Cliente") }
                                )
                            }
                            GenesysSpacer(GenesysSpacing.Medium)
                        }
                    }
                }

                item {
                    GenesysSpacer(GenesysSpacing.Huge)
                    GenesysTextButton(
                        text = GenesysStrings.Logout,
                        onClick = { onEvent(PageListEvent.OnLogoutClicked) },
                        modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally),
                        color = MaterialTheme.colorScheme.error
                    )
                    GenesysSpacer(GenesysSpacing.Huge)
                }
            }
        }
    }

    if (state.showCreateDialog) CreatePageDialog(state, onEvent, onImport)
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
        GenesysSpacer(GenesysSpacing.Large)
        GenesysText(
            text = GenesysStrings.ManageVitrines, 
            style = GenesysTextStyle.Headline, 
            fontWeight = GenesysFontWeight.ExtraBold
        )
        GenesysText(
            text = GenesysStrings.ManageVitrinesSubtitle, 
            style = GenesysTextStyle.Body,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
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
                    onExport = { onEvent(PageListEvent.OnExportPageClicked(page)) },
                    onDelete = { onEvent(PageListEvent.OnDeletePageClicked(page.id)) }
                )
                GenesysSpacer(GenesysSpacing.Medium)
            }
        }
    }
}

@Composable
private fun OrdersHeaderUI(
    state: PageListState,
    onEvent: (PageListEvent) -> Unit
) {
    val rawRevenue = remember(state.orders) { state.orders.filter { it.status == OrderStatus.COMPLETED }.sumOf { it.total } }
    val totalRevenue = (rawRevenue * 100.0).roundToLong() / 100.0
    val totalPending = remember(state.orders) { state.orders.count { it.status == OrderStatus.PENDING } }

    GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = false) {
        GenesysSpacer(GenesysSpacing.Large)
        
        GenesysRow(modifier = Modifier.fillMaxWidth(), usePadding = true) {
            GenesysWeightBox(1f) {
                GenesysStatsCard(label = GenesysStrings.Revenue, value = "${GenesysStrings.PricePrefix}$totalRevenue", color = Color(0xFF34C759))
            }
            GenesysSpacer(GenesysSpacing.Medium)
            GenesysWeightBox(1f) {
                GenesysStatsCard(label = GenesysStrings.Pending, value = totalPending.toString(), color = Color(0xFFFF9500))
            }
        }

        GenesysSpacer(GenesysSpacing.Large)

        GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = true) {
            GenesysTextField(
                value = state.searchQuery,
                onValueChange = { onEvent(PageListEvent.OnSearchQueryChanged(it)) },
                label = GenesysStrings.SearchOrdersLabel,
                icon = GenesysIcons.Search
            )

            GenesysSpacer(GenesysSpacing.Medium)

            GenesysRow(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), 
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GenesysFilterChip(
                    selected = state.selectedStatusFilter == null,
                    onClick = { onEvent(PageListEvent.OnStatusFilterSelected(null)) },
                    label = GenesysStrings.All,
                    badgeCount = state.orders.size
                )

                OrderStatus.entries.forEach { status ->
                    val label = when(status) {
                        OrderStatus.PENDING -> GenesysStrings.StatusPending
                        OrderStatus.PROCESSING -> GenesysStrings.StatusProcessing
                        OrderStatus.COMPLETED -> GenesysStrings.StatusCompleted
                        OrderStatus.CANCELLED -> GenesysStrings.StatusCancelled
                    }
                    GenesysFilterChip(
                        selected = state.selectedStatusFilter == status,
                        onClick = { onEvent(PageListEvent.OnStatusFilterSelected(status)) },
                        label = label,
                        badgeCount = state.orders.count { it.status == status }
                    )
                }
            }
        }
        GenesysSpacer(GenesysSpacing.Medium)
    }
}

@Composable
private fun PageItemRow(
    page: Page,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onCopyUrl: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit
) {
    GenesysCard(modifier = Modifier.fillMaxWidth()) {
        GenesysColumn(usePadding = false) {
            GenesysRow(
                modifier = Modifier.fillMaxWidth(), 
                verticalAlignment = Alignment.CenterVertically
            ) {
                GenesysBox(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(GenesysIcons.Web, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
                
                GenesysSpacer(GenesysSpacing.Medium)
                
                GenesysColumn(modifier = Modifier.weight(1f), usePadding = false) {
                    GenesysText(
                        text = page.title, 
                        style = GenesysTextStyle.Title, 
                        fontWeight = GenesysFontWeight.ExtraBold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    GenesysText(
                        text = "ID: ${page.id}", 
                        style = GenesysTextStyle.Label, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            GenesysSpacer(GenesysSpacing.Medium)
            
            GenesysRow(
                fillWidth = true,
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GenesysIconButton(icon = GenesysIcons.Visibility, onClick = onView)
                GenesysIconButton(icon = GenesysIcons.Edit, onClick = onEdit)
                GenesysIconButton(icon = GenesysIcons.Copy, onClick = onCopyUrl)
                GenesysIconButton(icon = GenesysIcons.CloudUpload, onClick = onExport) // ÍCONE DE EXPORTAR
                GenesysIconButton(icon = GenesysIcons.Delete, onClick = onDelete, tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun OrderCardUI(order: Order, onStatusUpdate: (OrderStatus) -> Unit, onContact: () -> Unit) {
    GenesysCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) { // Usando Column padrão para controle total do padding
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    val initials = remember(order.customerName) {
                        order.customerName?.split(" ")?.take(2)?.mapNotNull { it.firstOrNull() }?.joinToString("")?.uppercase() ?: "C"
                    }
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text( 
                            text = initials, 
                            style = MaterialTheme.typography.bodyMedium, 
                            fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold, 
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    
                    Spacer(Modifier.width(8.dp))
                    
                    Column {
                        Text(
                            text = "${GenesysStrings.OrderPrefix}${order.id.takeLast(6).uppercase()}", 
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = order.customerName ?: "Consumidor", 
                            style = MaterialTheme.typography.titleMedium, 
                            fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                GenesysStatusPicker(currentStatus = order.status, onStatusSelected = onStatusUpdate)
            }
            
            Spacer(Modifier.height(8.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(Modifier.height(8.dp))

            order.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${item.quantity}x", 
                        style = MaterialTheme.typography.bodyMedium, 
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.width(28.dp)
                    )
                    Text(
                        text = item.product.name, 
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val subtotal = (item.product.price * item.quantity * 100.0).roundToLong() / 100.0
                    Text(
                        text = "${GenesysStrings.PricePrefix}$subtotal", 
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = GenesysStrings.OrderTotal, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val totalFormatted = (order.total * 100.0).roundToLong() / 100.0
                    Text(
                        text = "${GenesysStrings.PricePrefix}$totalFormatted", 
                        style = MaterialTheme.typography.titleLarge, 
                        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
                    )
                }
                
                // BOTÃO DE CONTATO CORRIGIDO: Agora sempre tenta aparecer se houver qualquer indício de telefone
                val phone = order.customerPhone ?: ""
                if (phone.isNotBlank()) {
                    GenesysLoadingButton(
                        text = "Falar com o cliente",
                        onClick = onContact,
                        icon = GenesysIcons.Chat,
                        fillWidth = false,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CreatePageDialog(state: PageListState, onEvent: (PageListEvent) -> Unit, onImport: () -> Unit) {
     GenesysDialog(
        onDismissRequest = { onEvent(PageListEvent.OnDismissCreateDialog) },
        title = GenesysStrings.NewPageTitle,
        confirmButton = { 
            GenesysColumn(usePadding = false) {
                GenesysLoadingButton(
                    text = "Criar Vitrine de Vendas", 
                    onClick = { onEvent(PageListEvent.OnConfirmCreatePage(PageTemplateType.PROFESSIONAL_VITRINE)) }, 
                    enabled = state.newPageTitle.isNotBlank(),
                    isLoading = state.isLoading,
                    fillWidth = true,
                    icon = GenesysIcons.ShoppingBag
                ) 
                GenesysSpacer(GenesysSpacing.Small)
                GenesysLoadingButton(
                    text = "Criar Link na Bio (Perfil)", 
                    onClick = { onEvent(PageListEvent.OnConfirmCreatePage(PageTemplateType.BIO_PROFILE)) }, 
                    enabled = state.newPageTitle.isNotBlank(),
                    isLoading = state.isLoading,
                    fillWidth = true,
                    icon = GenesysIcons.Person
                ) 
                GenesysSpacer(GenesysSpacing.Small)
                GenesysLoadingButton(
                    text = "Importar Arquivo .benevides", 
                    onClick = onImport,
                    fillWidth = true,
                    icon = GenesysIcons.CloudUpload,
                    containerColor = MaterialTheme.colorScheme.secondary
                )
                GenesysSpacer(GenesysSpacing.Small)
                GenesysTextButton(
                    text = GenesysStrings.CreateEmptyVitrine, 
                    onClick = { onEvent(PageListEvent.OnConfirmCreatePage(PageTemplateType.EMPTY)) },
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
