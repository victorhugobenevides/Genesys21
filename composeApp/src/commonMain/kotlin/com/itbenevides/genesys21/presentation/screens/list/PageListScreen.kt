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
import com.itbenevides.genesys21.domain.model.BookingService
import com.itbenevides.genesys21.domain.model.OrderStatus
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.getWebBaseUrl
import com.itbenevides.genesys21.navigation.Route
import com.itbenevides.genesys21.navigation.Router
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysIconButton
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysTextButton
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysFilterChip
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysTextField
import com.itbenevides.genesys21.ui.components.atoms.primitives.*
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.*
import com.itbenevides.genesys21.ui.components.molecules.booking.ServiceCard
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysStatsCard
import com.itbenevides.genesys21.ui.components.molecules.feedback.GenesysEmptyState
import com.itbenevides.genesys21.ui.components.molecules.input.GenesysStatusPicker
import com.itbenevides.genesys21.ui.components.molecules.navigation.*
import com.itbenevides.genesys21.ui.components.organisms.feedback.GenesysDialog
import com.itbenevides.genesys21.ui.components.organisms.navigation.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.templates.pages.GenesysPage
import com.itbenevides.genesys21.presentation.screens.admin.SuperAdminDashboard
import com.itbenevides.genesys21.domain.model.UserRole
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.util.downloadFile
import com.itbenevides.genesys21.util.rememberFileHandler
import kotlin.math.roundToLong
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.compose.koinInject

@Composable
fun PageListScreen(
    viewModel: PageViewModel,
    onAddPage: () -> Unit,
    onEditPage: (Page) -> Unit,
    onViewPage: (Page) -> Unit,
    onLogout: () -> Unit,
    onShowcase: () -> Unit,
) {
    val pages by viewModel.pages.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val isGlobalLoading by viewModel.isLoading.collectAsState()
    val uriHandler = LocalUriHandler.current
    val router: Router = koinInject()

    var state by remember { mutableStateOf(PageListState()) }

    state =
        state.copy(
            pages = pages,
            orders = orders,
            isLoading = isGlobalLoading,
            pendingOrdersCount = orders.count { it.status == OrderStatus.PENDING },
        )

    LaunchedEffect(Unit) {
        viewModel.loadPages()
        viewModel.loadOrders()
        viewModel.loadBookingServices()
    }

    val onEvent: (PageListEvent) -> Unit = { event ->
        when (event) {
            is PageListEvent.OnTabSelected -> state = state.copy(selectedTab = event.index)
            is PageListEvent.OnSearchQueryChanged -> state = state.copy(searchQuery = event.query)
            is PageListEvent.OnStatusFilterSelected -> state = state.copy(selectedStatusFilter = event.status)
            is PageListEvent.OnDateSelected -> state = state.copy(selectedDate = event.date)
            is PageListEvent.OnCreatePageClicked -> state = state.copy(showCreateDialog = true)
            is PageListEvent.OnDismissCreateDialog -> state = state.copy(showCreateDialog = false, newPageTitle = "")
            is PageListEvent.OnNewPageTitleChanged -> state = state.copy(newPageTitle = event.title)
            is PageListEvent.OnConfirmCreatePage -> {
                val id = (1..8).map { "abcdefghijklmnopqrstuvwxyz0123456789".random() }.joinToString("")
                val newPage =
                    when (event.templateType) {
                        PageTemplateType.PROFESSIONAL_VITRINE -> Page.defaultTemplate(id, state.newPageTitle.trim())
                        PageTemplateType.BIO_PROFILE -> Page.profileTemplate(id, state.newPageTitle.trim())
                        PageTemplateType.BLOG_POST -> Page.blogPostTemplate(id, state.newPageTitle.trim())
                        PageTemplateType.BARBER_SHOP -> Page.barberShopTemplate(id, state.newPageTitle.trim())
                        PageTemplateType.PRO_DESIGN -> Page.proDesignTemplate(id, state.newPageTitle.trim())
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
                    val updatedPage =
                        firstPage.copy(
                            customDomain = event.domain.ifBlank { null },
                            whatsapp = event.whatsapp.ifBlank { null },
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
                } catch (e: Exception) {
                }
            }
        }
    }

    val fileHandler =
        rememberFileHandler { json ->
            json?.let { onEvent(PageListEvent.OnImportPageClicked(it)) }
        }

    PageListContent(
        state = state,
        viewModel = viewModel,
        onEvent = onEvent,
        onViewPage = onViewPage,
        onEditPage = onEditPage,
        onImport = { fileHandler() },
        onExportAll = { onEvent(PageListEvent.OnExportAllClicked) },
        onContactCustomer = { phone, orderId, name ->
            val message = "Olá $name, estou entrando em contato sobre o seu pedido #$orderId na Genesys21."
            uriHandler.openUri("https://wa.me/$phone?text=${message.replace(" ", "%20")}")
        },
        onShowcase = onShowcase,
        onAddService = { router.navigateTo(Route.ServiceEditor(page = null, service = null)) },
        onEditService = { router.navigateTo(Route.ServiceEditor(page = null, service = it)) },
        onDeleteService = { viewModel.deleteBookingService(it) }
    )
}

@Composable
private fun PageListContent(
    state: PageListState,
    viewModel: PageViewModel,
    onEvent: (PageListEvent) -> Unit,
    onViewPage: (Page) -> Unit,
    onEditPage: (Page) -> Unit,
    onImport: () -> Unit,
    onExportAll: () -> Unit,
    onContactCustomer: (String, String, String) -> Unit,
    onShowcase: () -> Unit,
    onAddService: () -> Unit,
    onEditService: (BookingService) -> Unit,
    onDeleteService: (String) -> Unit,
) {
    val services by viewModel.services.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val isSuperAdmin = userProfile?.role == UserRole.SUPERADMIN

    GenesysPage(
        topBar = {
            GenesysColumn(usePadding = false, modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                GenesysTopAppBar(
                    title = GenesysStrings.AdminTitle,
                    onBack = null,
                    actions = {
                        GenesysIconButton(icon = GenesysIcons.Magic, contentDescription = "Design System", onClick = onShowcase)
                        GenesysIconButton(icon = GenesysIcons.Numbers, contentDescription = "Exportar Tudo", onClick = onExportAll)
                        GenesysIconButton(icon = GenesysIcons.CloudUpload, contentDescription = "Importar Backup", onClick = onImport)
                        GenesysIconButton(icon = GenesysIcons.Settings, onClick = { onEvent(PageListEvent.OnGlobalSettingsClicked) })
                        GenesysIconButton(icon = GenesysIcons.Add, onClick = { onEvent(PageListEvent.OnCreatePageClicked) })
                    },
                )

                val tabs = mutableListOf(
                    GenesysTabData(GenesysStrings.VitrineTab, GenesysIcons.Web),
                    GenesysTabData(GenesysStrings.OrdersTab, GenesysIcons.List, badgeCount = state.pendingOrdersCount),
                    GenesysTabData("Agenda", GenesysIcons.Schedule),
                    GenesysTabData("Serviços", GenesysIcons.Inventory),
                )

                if (isSuperAdmin) {
                    tabs.add(GenesysTabData("SuperAdmin", GenesysIcons.AdminPanelSettings))
                }

                GenesysTabRow(
                    selectedTabIndex = state.selectedTab,
                    tabs = tabs,
                    onTabSelected = { index -> onEvent(PageListEvent.OnTabSelected(index)) },
                )
            }
        },
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(bottom = 64.dp),
            ) {
                item {
                    GenesysColumn(modifier = Modifier.widthIn(max = 1200.dp), usePadding = false) {
                        when (state.selectedTab) {
                            0 -> PagesTabUI(state, onEvent, onViewPage, onEditPage)
                            1 -> OrdersHeaderUI(state, onEvent)
                            2 -> MerchantAgendaTabUI(state, viewModel, onEvent)
                            3 -> ServicesTabUI(services, onAddService, onEditService, onDeleteService)
                            4 -> if (isSuperAdmin) SuperAdminDashboard(viewModel)
                        }
                    }
                }

                if (state.selectedTab == 1) {
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
                                title = GenesysStrings.NoOrdersFound,
                                description = GenesysStrings.NoOrdersDescription,
                            )
                        }
                    } else {
                        items(items = filteredOrders, key = { it.id }) { order ->
                            GenesysBox(modifier = Modifier.widthIn(max = 1200.dp).padding(horizontal = 16.dp)) {
                                OrderCardUI(
                                    order = order,
                                    onStatusUpdate = { newStatus -> onEvent(PageListEvent.OnUpdateOrderStatus(order.id, newStatus)) },
                                    onContact = { onContactCustomer(order.customerPhone ?: "", order.id, order.customerName ?: "Cliente") },
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
                        color = MaterialTheme.colorScheme.error,
                    )
                    GenesysSpacer(GenesysSpacing.Huge)
                }
            }
        }
    }

    if (state.showCreateDialog) CreatePageDialog(state, onEvent, onImport)
    if (state.showGlobalSettings && state.pages.isNotEmpty()) GlobalSettingsDialog(state, onEvent)
    if (state.showRenameDialog) RenamePageDialog(state, onEvent)
}

@Composable
private fun PagesTabUI(
    state: PageListState,
    onEvent: (PageListEvent) -> Unit,
    onViewPage: (Page) -> Unit,
    onEditPage: (Page) -> Unit,
) {
    val clipboardManager = LocalClipboardManager.current

    GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = true) {
        GenesysSpacer(GenesysSpacing.Large)
        GenesysText(text = GenesysStrings.ManageVitrines, style = GenesysTextStyle.Headline, fontWeight = GenesysFontWeight.ExtraBold)
        GenesysText(
            text = GenesysStrings.ManageVitrinesSubtitle,
            style = GenesysTextStyle.Body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        GenesysSpacer(GenesysSpacing.Large)

        if (state.pages.isEmpty() && !state.isLoading) {
            GenesysEmptyState(
                icon = GenesysIcons.WebAssetOff,
                title = GenesysStrings.NoPagesFound,
                description = GenesysStrings.NoPagesDescription,
            )
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
                )
                GenesysSpacer(GenesysSpacing.Medium)
            }
        }
    }
}

@Composable
private fun ServicesTabUI(
    services: List<BookingService>,
    onAddService: () -> Unit,
    onEditService: (BookingService) -> Unit,
    onDeleteService: (String) -> Unit,
) {
    GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = true) {
        GenesysSpacer(GenesysSpacing.Large)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                GenesysText(text = "Catálogo de Serviços", style = GenesysTextStyle.Headline, fontWeight = GenesysFontWeight.ExtraBold)
                GenesysText(
                    text = "Gerencie os serviços oferecidos em suas vitrines.",
                    style = GenesysTextStyle.Body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            GenesysLoadingButton(
                text = "Novo Serviço",
                icon = GenesysIcons.Add,
                onClick = onAddService
            )
        }
        GenesysSpacer(GenesysSpacing.Large)

        if (services.isEmpty()) {
            GenesysEmptyState(
                icon = GenesysIcons.Inventory,
                title = "Nenhum serviço cadastrado",
                description = "Comece adicionando o primeiro serviço do seu negócio.",
                action = {
                    GenesysLoadingButton(text = "Cadastrar Primeiro Serviço", onClick = onAddService)
                }
            )
        } else {
            services.forEach { service ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    ServiceCard(
                        service = service,
                        onClick = { onEditService(service) }
                    )
                    Row(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                        GenesysIconButton(
                            icon = GenesysIcons.Delete,
                            tint = Color.Red.copy(alpha = 0.6f),
                            onClick = { onDeleteService(service.id) }
                        )
                    }
                }
                GenesysSpacer(GenesysSpacing.Small)
            }
        }
    }
}

@Composable
private fun OrdersHeaderUI(
    state: PageListState,
    onEvent: (PageListEvent) -> Unit,
) {
    val rawRevenue = remember(state.orders) { state.orders.filter { it.status == OrderStatus.COMPLETED }.sumOf { it.total } }
    val totalRevenue = (rawRevenue * 100.0).roundToLong() / 100.0
    val totalPending = remember(state.orders) { state.orders.count { it.status == OrderStatus.PENDING } }

    GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = false) {
        GenesysSpacer(GenesysSpacing.Large)
        GenesysRow(modifier = Modifier.fillMaxWidth(), usePadding = true) {
            GenesysWeightBox(1f) {
                GenesysStatsCard(
                    label = GenesysStrings.Revenue,
                    value = "${GenesysStrings.PricePrefix}$totalRevenue",
                    color = Color(0xFF34C759),
                )
            }
            GenesysSpacer(GenesysSpacing.Medium)
            GenesysWeightBox(
                1f,
            ) { GenesysStatsCard(label = GenesysStrings.Pending, value = totalPending.toString(), color = Color(0xFFFF9500)) }
        }
        GenesysSpacer(GenesysSpacing.Large)
        GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = true) {
            GenesysTextField(value = state.searchQuery, onValueChange = {
                onEvent(PageListEvent.OnSearchQueryChanged(it))
            }, label = GenesysStrings.SearchOrdersLabel, icon = GenesysIcons.Search)
            GenesysSpacer(GenesysSpacing.Medium)
            GenesysRow(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GenesysFilterChip(selected = state.selectedStatusFilter == null, onClick = {
                    onEvent(PageListEvent.OnStatusFilterSelected(null))
                }, label = GenesysStrings.All, badgeCount = state.orders.size)
                OrderStatus.entries.forEach { status ->
                    val label =
                        when (status) {
                            OrderStatus.PENDING -> GenesysStrings.StatusPending
                            OrderStatus.PROCESSING -> GenesysStrings.StatusProcessing
                            OrderStatus.COMPLETED -> GenesysStrings.StatusCompleted
                            OrderStatus.CANCELLED -> GenesysStrings.StatusCancelled
                        }
                    GenesysFilterChip(selected = state.selectedStatusFilter == status, onClick = {
                        onEvent(PageListEvent.OnStatusFilterSelected(status))
                    }, label = label, badgeCount = state.orders.count { it.status == status })
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
    onRename: () -> Unit,
    onCopyUrl: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit,
) {
    GenesysCard(modifier = Modifier.fillMaxWidth()) {
        GenesysColumn(usePadding = false) {
            GenesysRow(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                GenesysBox(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(GenesysIcons.Web, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
                GenesysSpacer(GenesysSpacing.Medium)
                GenesysColumn(modifier = Modifier.weight(1f), usePadding = false) {
                    GenesysText(
                        text = page.title,
                        style = GenesysTextStyle.Title,
                        fontWeight = GenesysFontWeight.ExtraBold,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    GenesysText(text = "ID: ${page.id}", style = GenesysTextStyle.Label, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            GenesysSpacer(GenesysSpacing.Medium)
            GenesysRow(fillWidth = true, horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                GenesysIconButton(icon = GenesysIcons.Visibility, onClick = onView)
                GenesysIconButton(icon = GenesysIcons.Edit, onClick = onRename)
                GenesysIconButton(icon = GenesysIcons.Magic, onClick = onEdit)
                GenesysIconButton(icon = GenesysIcons.Copy, onClick = onCopyUrl)
                GenesysIconButton(icon = GenesysIcons.CloudUpload, onClick = onExport)
                GenesysIconButton(icon = GenesysIcons.Delete, onClick = onDelete, tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun OrderCardUI(
    order: com.itbenevides.genesys21.domain.model.Order,
    onStatusUpdate: (OrderStatus) -> Unit,
    onContact: () -> Unit,
) {
    GenesysCard(modifier = Modifier.fillMaxWidth(), elevation = 1.dp) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    val initials =
                        remember(order.customerName) {
                            order.customerName?.split(" ")?.take(2)?.mapNotNull { it.firstOrNull() }?.joinToString("")?.uppercase() ?: "C"
                        }
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "${GenesysStrings.OrderPrefix}${order.id.takeLast(6).uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = order.customerName ?: "Consumidor",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                GenesysStatusPicker(currentStatus = order.status, onStatusSelected = onStatusUpdate)
            }
            Spacer(Modifier.height(8.dp))
            order.items.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${item.quantity}x",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.width(28.dp),
                    )
                    Text(
                        text = item.product.name,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    val subtotal = (item.product.price * item.quantity * 100.0).roundToLong() / 100.0
                    Text(
                        text = "${GenesysStrings.PricePrefix}$subtotal",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = GenesysStrings.OrderTotal,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    val totalFormatted = (order.total * 100.0).roundToLong() / 100.0
                    Text(
                        text = "${GenesysStrings.PricePrefix}$totalFormatted",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                    )
                }
                if (!order.customerPhone.isNullOrBlank()) {
                    GenesysLoadingButton(
                        text = "Falar com o cliente",
                        onClick = onContact,
                        icon = GenesysIcons.Chat,
                        fillWidth = false,
                        shape = RoundedCornerShape(8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun RenamePageDialog(
    state: PageListState,
    onEvent: (PageListEvent) -> Unit,
) {
    var title by remember(state.pageToRename) { mutableStateOf(state.pageToRename?.title ?: "") }
    GenesysDialog(
        onDismissRequest = { onEvent(PageListEvent.OnDismissRenameDialog) },
        title = "Renomear Vitrine",
        confirmButton = {
            GenesysLoadingButton(text = GenesysStrings.Save, onClick = {
                onEvent(PageListEvent.OnConfirmRenamePage(title))
            }, enabled = title.isNotBlank(), isLoading = state.isLoading, fillWidth = true)
        },
        dismissButton = { GenesysTextButton(text = GenesysStrings.Cancel, onClick = { onEvent(PageListEvent.OnDismissRenameDialog) }) },
    ) {
        GenesysTextField(value = title, onValueChange = { title = it }, label = "Novo Título", placeholder = "Ex: Minha Nova Loja")
    }
}

@Composable
private fun CreatePageDialog(
    state: PageListState,
    onEvent: (PageListEvent) -> Unit,
    onImport: () -> Unit,
) {
    GenesysDialog(
        onDismissRequest = { onEvent(PageListEvent.OnDismissCreateDialog) },
        title = GenesysStrings.NewPageTitle,
        confirmButton = {
            GenesysColumn(usePadding = false) {
                GenesysLoadingButton(text = "Criar Vitrine de Vendas", onClick = {
                    onEvent(PageListEvent.OnConfirmCreatePage(PageTemplateType.PROFESSIONAL_VITRINE))
                }, enabled = state.newPageTitle.isNotBlank(), isLoading = state.isLoading, fillWidth = true, icon = GenesysIcons.ShoppingBag)
                GenesysSpacer(GenesysSpacing.Small)
                GenesysLoadingButton(text = "Criar Link na Bio (Perfil)", onClick = {
                    onEvent(PageListEvent.OnConfirmCreatePage(PageTemplateType.BIO_PROFILE))
                }, enabled = state.newPageTitle.isNotBlank(), isLoading = state.isLoading, fillWidth = true, icon = GenesysIcons.Person)
                GenesysSpacer(GenesysSpacing.Small)
                GenesysLoadingButton(text = "Criar Post de Blog", onClick = {
                    onEvent(PageListEvent.OnConfirmCreatePage(PageTemplateType.BLOG_POST))
                }, enabled = state.newPageTitle.isNotBlank(), isLoading = state.isLoading, fillWidth = true, icon = GenesysIcons.List)
                GenesysSpacer(GenesysSpacing.Small)
                GenesysLoadingButton(text = "Criar Página de Barbearia 💈", onClick = {
                    onEvent(PageListEvent.OnConfirmCreatePage(PageTemplateType.BARBER_SHOP))
                }, enabled = state.newPageTitle.isNotBlank(), isLoading = state.isLoading, fillWidth = true, icon = GenesysIcons.Schedule, containerColor = Color(0xFF5D4037))
                GenesysSpacer(GenesysSpacing.Small)
                GenesysLoadingButton(text = "Criar Vitrine Design PRO 💎", onClick = {
                    onEvent(PageListEvent.OnConfirmCreatePage(PageTemplateType.PRO_DESIGN))
                }, enabled = state.newPageTitle.isNotBlank(), isLoading = state.isLoading, fillWidth = true, icon = GenesysIcons.Magic, containerColor = MaterialTheme.colorScheme.tertiary)
                GenesysSpacer(GenesysSpacing.Small)
                GenesysLoadingButton(
                    text = "Importar Arquivo .benevides",
                    onClick = onImport,
                    fillWidth = true,
                    icon = GenesysIcons.CloudUpload,
                    containerColor = MaterialTheme.colorScheme.secondary,
                )
                GenesysSpacer(GenesysSpacing.Small)
                GenesysTextButton(text = GenesysStrings.CreateEmptyVitrine, onClick = {
                    onEvent(PageListEvent.OnConfirmCreatePage(PageTemplateType.EMPTY))
                }, enabled = state.newPageTitle.isNotBlank(), modifier = Modifier.fillMaxWidth())
            }
        },
        dismissButton = { GenesysTextButton(text = GenesysStrings.Cancel, onClick = { onEvent(PageListEvent.OnDismissCreateDialog) }) },
    ) {
        GenesysTextField(value = state.newPageTitle, onValueChange = {
            onEvent(PageListEvent.OnNewPageTitleChanged(it))
        }, label = GenesysStrings.PageTitleLabel, placeholder = GenesysStrings.PageTitlePlaceholder)
    }
}

@Composable
private fun GlobalSettingsDialog(
    state: PageListState,
    onEvent: (PageListEvent) -> Unit,
) {
    val firstPage = state.pages.firstOrNull()
    var domain by remember { mutableStateOf(firstPage?.customDomain ?: "") }
    var whatsapp by remember { mutableStateOf(firstPage?.whatsapp ?: "") }
    GenesysDialog(
        onDismissRequest = { onEvent(PageListEvent.OnDismissGlobalSettings) },
        title = GenesysStrings.GlobalSettings,
        confirmButton = {
            GenesysLoadingButton(text = GenesysStrings.Save, onClick = {
                onEvent(PageListEvent.OnConfirmGlobalSettings(domain, whatsapp))
            }, isLoading = state.isLoading)
        },
        dismissButton = { GenesysTextButton(text = GenesysStrings.Cancel, onClick = { onEvent(PageListEvent.OnDismissGlobalSettings) }) },
    ) {
        GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = false) {
            GenesysTextField(value = domain, onValueChange = { domain = it }, label = GenesysStrings.CustomDomainLabel)
            GenesysSpacer(GenesysSpacing.Medium)
            GenesysTextField(value = whatsapp, onValueChange = { whatsapp = it }, label = GenesysStrings.WhatsAppLabel)
        }
    }
}
