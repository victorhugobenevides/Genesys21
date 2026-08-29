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
import com.itbenevides.genesys21.ui.components.organisms.chat.OrderChatComponent
import com.itbenevides.genesys21.ui.components.organisms.feedback.GenesysDialog
import com.itbenevides.genesys21.ui.components.organisms.navigation.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.templates.pages.GenesysPage
import com.itbenevides.genesys21.presentation.screens.profile.ProfileScreen
import com.itbenevides.genesys21.presentation.screens.editor.AIPageBuilderDialog
import com.itbenevides.genesys21.ui.theme.*
import com.itbenevides.genesys21.ui.util.GenesysWindowSizeClass
import com.itbenevides.genesys21.ui.util.LocalWindowSizeClass
import com.itbenevides.genesys21.util.downloadFile
import com.itbenevides.genesys21.util.rememberFileHandler
import com.itbenevides.genesys21.presentation.screens.list.components.AdminMenuItem
import com.itbenevides.genesys21.presentation.screens.list.components.AdminSidebar
import com.itbenevides.genesys21.presentation.screens.list.components.*
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

    val menuItems = remember(userProfile, state.pendingOrdersCount) {
        AdminMenuItem.getVisibleItems(
            user = userProfile,
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
                0 -> MainDashboardTab(viewModel)
                9 -> B2BInsightsTab(viewModel)
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
                3 -> AgendaTab(state, viewModel, onEvent)
                4 -> ServicesTab(services, onAddService, onEditService, onDeleteService)
                5 -> {
                    val receiptViewModel: ReceiptViewModel = koinInject()
                    ReceiptListScreen(
                        viewModel = receiptViewModel,
                        isEmbedded = true,
                        onOpenUrl = { url -> com.itbenevides.genesys21.openUrlInNewTab(url) }
                    )
                }
                6 -> PaymentsTab(viewModel, userProfile, uriHandler, scope)
                10 -> StoreSettingsTab(viewModel, userProfile, uriHandler, scope)
                11 -> GlobalUsersTab(viewModel)
                12 -> GlobalDomainsTab(viewModel)
                13 -> AuditLogsTab(viewModel)
                8 -> ProfileScreen(viewModel, router)
                else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Selecione uma opção no menu")
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
