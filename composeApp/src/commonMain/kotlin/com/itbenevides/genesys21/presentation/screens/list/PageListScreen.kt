package com.itbenevides.genesys21.presentation.screens.list

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.getWebBaseUrl
import com.itbenevides.genesys21.navigation.Route
import com.itbenevides.genesys21.navigation.Router
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.presentation.receipt.ReceiptListScreen
import com.itbenevides.genesys21.presentation.receipt.ReceiptViewModel
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysIconButton
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysTextButton
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysFilterChip
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysTextField
import com.itbenevides.genesys21.ui.components.atoms.primitives.*
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.*
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysStatsCard
import com.itbenevides.genesys21.ui.components.molecules.input.GenesysStatusPicker
import com.itbenevides.genesys21.ui.components.organisms.chat.OrderChatComponent
import com.itbenevides.genesys21.ui.components.organisms.feedback.GenesysDialog
import com.itbenevides.genesys21.ui.components.organisms.navigation.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.templates.pages.GenesysPage
import com.itbenevides.genesys21.presentation.screens.admin.SuperAdminDashboard
import com.itbenevides.genesys21.presentation.screens.profile.ProfileScreen
import com.itbenevides.genesys21.presentation.screens.editor.AIPageBuilderDialog
import com.itbenevides.genesys21.ui.theme.*
import com.itbenevides.genesys21.ui.util.GenesysWindowSizeClass
import com.itbenevides.genesys21.ui.util.LocalWindowSizeClass
import com.itbenevides.genesys21.util.downloadFile
import com.itbenevides.genesys21.util.rememberFileHandler
import com.itbenevides.genesys21.presentation.screens.list.components.AdminMenuItem
import com.itbenevides.genesys21.presentation.screens.list.components.AdminSidebar
import com.itbenevides.genesys21.presentation.screens.list.tabs.*
import kotlin.math.roundToLong
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
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
    val scope = rememberCoroutineScope()
    val windowSizeClass = LocalWindowSizeClass.current
    val isExpanded = windowSizeClass == GenesysWindowSizeClass.EXPANDED

    var state by remember { mutableStateOf(PageListState()) }
    var isRefreshing by remember { mutableStateOf(false) }
    var selectedOrderIdForDetail by remember { mutableStateOf<String?>(null) }

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
                val storeId = viewModel.userProfile.value?.id ?: "admin"
                val newPage =
                    when (event.templateType) {
                        PageTemplateType.PREMIUM_STORE -> Page.createFromTemplate("premium_store", id, storeId, state.newPageTitle.trim())
                        PageTemplateType.SERVICE_BOOKING -> Page.createFromTemplate("service_booking", id, storeId, state.newPageTitle.trim())
                        PageTemplateType.PERSONAL_HUB -> Page.createFromTemplate("personal_hub", id, storeId, state.newPageTitle.trim())
                        PageTemplateType.EMPTY -> Page(id, storeId, state.newPageTitle.trim())
                        else -> Page(id, storeId, state.newPageTitle.trim())
                    }

                viewModel.savePage(newPage, false) {
                    state = state.copy(showCreateDialog = false, newPageTitle = "")
                    onEditPage(newPage)
                }
            }
            is PageListEvent.OnAIDesignClicked -> state = state.copy(showAIBuider = true, showCreateDialog = false)
            is PageListEvent.OnDismissAIBuilder -> state = state.copy(showAIBuider = false)
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

    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                viewModel.loadPages()
                viewModel.loadOrders()
                viewModel.loadBookingServices()
                isRefreshing = false
            }
        },
        state = pullToRefreshState,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = pullToRefreshState,
                isRefreshing = isRefreshing,
                containerColor = GenesysTheme.colors.brandContainer,
                color = GenesysTheme.colors.onBrandContainer,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        },
        modifier = Modifier.fillMaxSize()
    ) {
        PageListContent(
            state = state,
            viewModel = viewModel,
            router = router,
            isExpanded = isExpanded,
            selectedOrderIdForDetail = selectedOrderIdForDetail,
            onSelectOrderForDetail = { selectedOrderIdForDetail = it },
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
            onOpenProfile = { router.navigateTo(Route.Profile) },
            onOpenReceipts = { router.navigateTo(Route.Receipts) },
            onAddService = { router.navigateTo(Route.ServiceEditor(page = null, service = null)) },
            onEditService = { router.navigateTo(Route.ServiceEditor(page = null, service = it)) },
            onDeleteService = { viewModel.deleteBookingService(it) },
            uriHandler = uriHandler,
            scope = scope
        )
    }

    if (state.showCreateDialog) {
        val onImportHandler = { fileHandler() }
        CreatePageDialog(state, onEvent, onImportHandler)
    }
    if (state.showGlobalSettings && state.pages.isNotEmpty()) GlobalSettingsDialog(state, onEvent)
    if (state.showRenameDialog) RenamePageDialog(state, onEvent)

    if (state.showAIBuider) {
        AIPageBuilderDialog(
            onDismiss = { onEvent(PageListEvent.OnDismissAIBuilder) },
            onPageGenerated = { generatedPage ->
                onEvent(PageListEvent.OnDismissAIBuilder)
                viewModel.savePage(generatedPage, false) {
                    onEditPage(generatedPage)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveNavigationSuiteApi::class)
@Composable
private fun PageListContent(
    state: PageListState,
    viewModel: PageViewModel,
    router: Router,
    isExpanded: Boolean,
    selectedOrderIdForDetail: String?,
    onSelectOrderForDetail: (String?) -> Unit,
    onEvent: (PageListEvent) -> Unit,
    onViewPage: (Page) -> Unit,
    onEditPage: (Page) -> Unit,
    onImport: () -> Unit,
    onExportAll: () -> Unit,
    onContactCustomer: (String, String, String) -> Unit,
    onShowcase: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenReceipts: () -> Unit,
    onAddService: () -> Unit,
    onEditService: (BookingService) -> Unit,
    onDeleteService: (String) -> Unit,
    uriHandler: androidx.compose.ui.platform.UriHandler,
    scope: kotlinx.coroutines.CoroutineScope
) {
    val services by viewModel.services.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isSuperAdmin = userProfile?.role == UserRole.SUPERADMIN

    val menuItems = remember(userProfile, state.pendingOrdersCount) {
        AdminMenuItem.getVisibleItems(
            role = userProfile?.role ?: UserRole.CUSTOMER,
            pendingOrders = state.pendingOrdersCount
        )
    }

    // Ajusta aba selecionada se a atual não for permitida
    LaunchedEffect(menuItems) {
        if (menuItems.none { it.id == state.selectedTab }) {
            menuItems.firstOrNull()?.let { onEvent(PageListEvent.OnTabSelected(it.id)) }
        }
    }

    LaunchedEffect(selectedOrderIdForDetail) {
        selectedOrderIdForDetail?.let { orderId ->
            while (true) {
                viewModel.loadChatMessages(orderId)
                kotlinx.coroutines.delay(5000)
            }
        }
    }

    GenesysPage(
        navigationSuiteItems = {
            menuItems.forEach { item ->
                item(
                    selected = state.selectedTab == item.id,
                    onClick = { onEvent(PageListEvent.OnTabSelected(item.id)) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (item.badgeCount > 0) Badge { Text(item.badgeCount.toString()) }
                            }
                        ) {
                            Icon(item.icon, contentDescription = item.label)
                        }
                    },
                    label = { Text(item.label) },
                    alwaysShowLabel = isExpanded
                )
            }
        },
        drawerContent = {
            AdminSidebar(
                items = menuItems,
                selectedItemId = state.selectedTab,
                onItemClick = {
                    onEvent(PageListEvent.OnTabSelected(it.id))
                }
            )
        },
        topBar = {
             GenesysTopAppBar(
                title = "Genesys Console",
                onBack = null,
            )
        },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (state.selectedTab) {
                0 -> MerchantAnalyticsTabUI(viewModel)
                9 -> B2BAnalyticsTabUI(viewModel)
                1 -> PagesTab(state, onEvent, onViewPage, onEditPage)
                2 -> OrdersTab(
                    state = state,
                    viewModel = viewModel,
                    isExpanded = isExpanded,
                    selectedOrderIdForDetail = selectedOrderIdForDetail,
                    onSelectOrderForDetail = onSelectOrderForDetail,
                    onEvent = onEvent,
                    onContactCustomer = onContactCustomer,
                    chatMessages = chatMessages
                )
                3 -> MerchantAgendaTabUI(state, viewModel, onEvent)
                4 -> ServicesTab(services, onAddService, onEditService, onDeleteService)
                5 -> {
                    val receiptViewModel: ReceiptViewModel = koinInject()
                    ReceiptListScreen(
                        viewModel = receiptViewModel,
                        isEmbedded = true,
                        onOpenUrl = { url -> com.itbenevides.genesys21.openUrlInNewTab(url) }
                    )
                }
                6 -> StoreSettingsTab(viewModel, userProfile, uriHandler, scope)
                10 -> StoreSettingsTab(viewModel, userProfile, uriHandler, scope)
                7 -> if (isSuperAdmin) SuperAdminDashboard(viewModel)
                8 -> ProfileScreen(viewModel, router)
                else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Selecione uma opção no menu")
                }
            }
        }
    }
}

@Composable
internal fun OrderCardUI(
    order: com.itbenevides.genesys21.domain.model.Order,
    isSelected: Boolean = false,
    onStatusUpdate: (OrderStatus) -> Unit,
    onContact: () -> Unit,
    onClick: (() -> Unit)? = null
) {
    GenesysCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = if (isSelected) 4.dp else 1.dp,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, GenesysTheme.colors.brand) else null,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(GenesysTheme.spacing.s)) {
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
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(GenesysTheme.colors.accent.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                            color = GenesysTheme.colors.accent,
                        )
                    }
                    Spacer(Modifier.width(GenesysTheme.spacing.xs))
                    Column {
                        Text(
                            text = "${GenesysStrings.OrderPrefix}${order.id.takeLast(6).uppercase()}",
                            style = GenesysTheme.typography.label,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = GenesysTheme.colors.brand,
                        )
                        Text(
                            text = order.customerName ?: "Consumidor",
                            style = GenesysTheme.typography.title,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                GenesysStatusPicker(currentStatus = order.status, onStatusSelected = onStatusUpdate)
            }
            Spacer(Modifier.height(GenesysTheme.spacing.xs))
            order.items.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = GenesysTheme.spacing.xxxs), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${item.quantity}x",
                        style = GenesysTheme.typography.body,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = GenesysTheme.colors.brand,
                        modifier = Modifier.width(GenesysTheme.spacing.xl),
                    )
                    Text(
                        text = item.name,
                        style = GenesysTheme.typography.body,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    val subtotal = (item.price * item.quantity * 100.0).roundToLong() / 100.0
                    Text(
                        text = "${GenesysStrings.PricePrefix}$subtotal",
                        style = GenesysTheme.typography.body,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    )
                }
            }
            Spacer(Modifier.height(GenesysTheme.spacing.xs))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = GenesysStrings.OrderTotal,
                        style = GenesysTheme.typography.label,
                        color = GenesysTheme.colors.onSurfaceVariant,
                    )
                    val totalFormatted = (order.total * 100.0).roundToLong() / 100.0
                    Text(
                        text = "${GenesysStrings.PricePrefix}$totalFormatted",
                        style = GenesysTheme.typography.headline,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                    )
                }
                if (!order.customerPhone.isNullOrBlank()) {
                    GenesysLoadingButton(
                        text = "Falar com o cliente",
                        onClick = onContact,
                        icon = GenesysIcons.Chat,
                        fillWidth = false,
                        shape = RoundedCornerShape(GenesysTheme.spacing.xs),
                    )
                }
            }
        }
    }
}

@Composable
internal fun OrderDetailContent(
    order: com.itbenevides.genesys21.domain.model.Order,
    chatMessages: List<ChatMessage>,
    onStatusUpdate: (OrderStatus) -> Unit,
    onContact: () -> Unit,
    onSendMessage: (String) -> Unit,
) {
    GenesysCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            GenesysText("Detalhes do Pedido", style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.ExtraBold)
            GenesysSpacer(GenesysTheme.spacing.m)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    GenesysText("Pedido #${order.id.uppercase()}", style = GenesysTextStyle.Body, fontWeight = GenesysFontWeight.Bold, color = GenesysTheme.colors.brand, isSelectable = true)
                    GenesysText(order.customerName ?: "Consumidor", style = GenesysTextStyle.Headline, isSelectable = true)
                }
                GenesysStatusPicker(currentStatus = order.status, onStatusSelected = onStatusUpdate)
            }

            GenesysSpacer(GenesysTheme.spacing.l)

            OrderChatComponent(
                messages = chatMessages,
                currentNick = "Lojista",
                isMerchantView = true,
                onSendMessage = onSendMessage
            )

            GenesysSpacer(GenesysTheme.spacing.l)
            GenesysDivider()
            GenesysSpacer(GenesysTheme.spacing.l)

            GenesysText("Itens", style = GenesysTextStyle.Body, fontWeight = GenesysFontWeight.Bold)
            GenesysSpacer(GenesysTheme.spacing.s)

            order.items.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${item.quantity}x ${item.name}", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    val subtotal = (item.price * item.quantity * 100.0).roundToLong() / 100.0
                    Text("${GenesysStrings.PricePrefix}$subtotal", fontWeight = FontWeight.Bold)
                }
            }

            GenesysSpacer(GenesysTheme.spacing.l)
            GenesysDivider()
            GenesysSpacer(GenesysTheme.spacing.l)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", style = MaterialTheme.typography.titleLarge)
                val totalFormatted = (order.total * 100.0).roundToLong() / 100.0
                Text("${GenesysStrings.PricePrefix}$totalFormatted", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = GenesysTheme.colors.brand)
            }

            if (!order.customerPhone.isNullOrBlank()) {
                GenesysSpacer(GenesysTheme.spacing.huge)
                GenesysLoadingButton(
                    text = "Falar com o cliente",
                    onClick = onContact,
                    icon = GenesysIcons.Chat,
                    fillWidth = true
                )
            }
        }
    }
}

@Composable
internal fun OrdersHeaderUI(
    state: PageListState,
    onEvent: (PageListEvent) -> Unit,
) {
    val rawRevenue = remember(state.orders) { state.orders.filter { it.status == OrderStatus.COMPLETED }.sumOf { it.total } }
    val totalRevenue = (rawRevenue * 100.0).roundToLong() / 100.0
    val totalPending = remember(state.orders) { state.orders.count { it.status == OrderStatus.PENDING } }
    val windowSizeClass = LocalWindowSizeClass.current
    val isCompact = windowSizeClass == GenesysWindowSizeClass.COMPACT

    GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = false) {
        GenesysSpacer(GenesysTheme.spacing.l)
        if (isCompact) {
            GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = true) {
                GenesysStatsCard(
                    label = GenesysStrings.Revenue,
                    value = "${GenesysStrings.PricePrefix}$totalRevenue",
                    color = Color(0xFF34C759),
                )
                GenesysSpacer(GenesysTheme.spacing.s)
                GenesysStatsCard(label = GenesysStrings.Pending, value = totalPending.toString(), color = Color(0xFFFF9500))
            }
        } else {
            GenesysRow(modifier = Modifier.fillMaxWidth(), usePadding = true) {
                GenesysWeightBox(1f) {
                    GenesysStatsCard(
                        label = GenesysStrings.Revenue,
                        value = "${GenesysStrings.PricePrefix}$totalRevenue",
                        color = Color(0xFF34C759),
                    )
                }
                GenesysSpacer(GenesysTheme.spacing.m)
                GenesysWeightBox(
                    1f,
                ) { GenesysStatsCard(label = GenesysStrings.Pending, value = totalPending.toString(), color = Color(0xFFFF9500)) }
            }
        }
        GenesysSpacer(GenesysTheme.spacing.l)
        GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = true) {
            GenesysTextField(value = state.searchQuery, onValueChange = {
                onEvent(PageListEvent.OnSearchQueryChanged(it))
            }, label = GenesysStrings.SearchOrdersLabel, icon = GenesysIcons.Search)
            GenesysSpacer(GenesysTheme.spacing.m)
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
                            OrderStatus.AWAITING_PAYMENT -> "Aguardando Pagamento"
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
        GenesysSpacer(GenesysTheme.spacing.m)
    }
}

@Composable
internal fun PageItemRow(
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
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(GenesysTheme.colors.brandContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(GenesysIcons.Web, null, tint = GenesysTheme.colors.brand, modifier = Modifier.size(20.dp))
                }
                GenesysSpacer(GenesysTheme.spacing.m)
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
            GenesysSpacer(GenesysTheme.spacing.m)
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
internal fun ToggleOptionRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        GenesysText(text = label, style = GenesysTextStyle.Body)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
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
                // OPÇÃO IA - DESTAQUE
                GenesysLoadingButton(
                    text = "Criar com Inteligência Artificial ✨",
                    onClick = { onEvent(PageListEvent.OnAIDesignClicked) },
                    fillWidth = true,
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    icon = GenesysIcons.Magic
                )

                GenesysSpacer(GenesysTheme.spacing.m)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                GenesysSpacer(GenesysTheme.spacing.m)

                // TEMPLATES ELITE
                GenesysLoadingButton(text = "Vitrine de Vendas Premium", onClick = {
                    onEvent(PageListEvent.OnConfirmCreatePage(PageTemplateType.PREMIUM_STORE))
                }, enabled = state.newPageTitle.isNotBlank(), isLoading = state.isLoading, fillWidth = true, icon = GenesysIcons.ShoppingBag)
                GenesysSpacer(GenesysTheme.spacing.s)

                GenesysLoadingButton(text = "Agendamento Profissional", onClick = {
                    onEvent(PageListEvent.OnConfirmCreatePage(PageTemplateType.SERVICE_BOOKING))
                }, enabled = state.newPageTitle.isNotBlank(), isLoading = state.isLoading, fillWidth = true, icon = GenesysIcons.Schedule)
                GenesysSpacer(GenesysTheme.spacing.s)

                GenesysLoadingButton(text = "Personal Hub (Links & Bio)", onClick = {
                    onEvent(PageListEvent.OnConfirmCreatePage(PageTemplateType.PERSONAL_HUB))
                }, enabled = state.newPageTitle.isNotBlank(), isLoading = state.isLoading, fillWidth = true, icon = GenesysIcons.Person)
                GenesysSpacer(GenesysTheme.spacing.s)

                GenesysLoadingButton(
                    text = "Importar Backup .benevides",
                    onClick = onImport,
                    fillWidth = true,
                    icon = GenesysIcons.CloudUpload,
                    containerColor = MaterialTheme.colorScheme.secondary,
                )
                GenesysSpacer(GenesysTheme.spacing.s)
                GenesysTextButton(text = GenesysStrings.CreateEmptyVitrine, onClick = {
                    onEvent(PageListEvent.OnConfirmCreatePage(PageTemplateType.EMPTY))
                }, enabled = state.newPageTitle.isNotBlank(), modifier = Modifier.fillMaxWidth())
            }
        },
        dismissButton = { GenesysTextButton(text = GenesysStrings.Cancel, onClick = { onEvent(PageListEvent.OnDismissCreateDialog) }) },
    ) {
        GenesysTextField(value = state.newPageTitle, onValueChange = {
            onEvent(PageListEvent.OnNewPageTitleChanged(it))
        }, label = GenesysStrings.PageTitleLabel, placeholder = "Ex: Minha Loja 2026")
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
            GenesysSpacer(GenesysTheme.spacing.m)
            GenesysTextField(value = whatsapp, onValueChange = { whatsapp = it }, label = GenesysStrings.WhatsAppLabel)
        }
    }
}
