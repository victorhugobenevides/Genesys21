package com.itbenevides.genesys21.presentation.screens.list

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.itbenevides.genesys21.domain.model.UserProfile
import com.itbenevides.genesys21.domain.model.UserRole
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.ui.util.GenesysWindowSizeClass
import com.itbenevides.genesys21.ui.util.LocalWindowSizeClass
import com.itbenevides.genesys21.util.downloadFile
import com.itbenevides.genesys21.util.rememberFileHandler
import kotlin.math.roundToLong
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.compose.koinInject

private data class PermittedTab(
    val id: Int,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val badgeCount: Int = 0
)

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
            is PageListEvent.OnCreatePageClicked -> router.navigateTo(Route.TemplateShowcase)
            is PageListEvent.OnDismissCreateDialog -> state = state.copy(showCreateDialog = false, newPageTitle = "")
            is PageListEvent.OnNewPageTitleChanged -> state = state.copy(newPageTitle = event.title)
            is PageListEvent.OnConfirmCreatePage -> {
                val id = (1..8).map { "abcdefghijklmnopqrstuvwxyz0123456789".random() }.joinToString("")
                val storeId = "genesys-official-store"
                val newPage =
                    when (event.templateType) {
                        PageTemplateType.PROFESSIONAL_VITRINE -> Page.createFromTemplate("professional_vitrine", id, storeId, state.newPageTitle.trim())
                        PageTemplateType.BIO_PROFILE -> Page.createFromTemplate("bio_profile", id, storeId, state.newPageTitle.trim())
                        PageTemplateType.BLOG_POST -> Page.createFromTemplate("blog_post", id, storeId, state.newPageTitle.trim())
                        PageTemplateType.BARBER_SHOP -> Page.createFromTemplate("barber_shop", id, storeId, state.newPageTitle.trim())
                        PageTemplateType.PRO_DESIGN -> Page.createFromTemplate("pro_design", id, storeId, state.newPageTitle.trim())
                        PageTemplateType.EMPTY -> Page(id, storeId, state.newPageTitle.trim())
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
        onOpenReceipts = { router.navigateTo(Route.Receipts) },
        onAddService = { router.navigateTo(Route.ServiceEditor(page = null, service = null)) },
        onEditService = { router.navigateTo(Route.ServiceEditor(page = null, service = it)) },
        onDeleteService = { viewModel.deleteBookingService(it) },
        uriHandler = uriHandler
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
    onOpenReceipts: () -> Unit,
    onAddService: () -> Unit,
    onEditService: (BookingService) -> Unit,
    onDeleteService: (String) -> Unit,
    uriHandler: androidx.compose.ui.platform.UriHandler
) {
    val services by viewModel.services.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val isSuperAdmin = userProfile?.role == UserRole.SUPERADMIN

    // Lista de abas permitidas
    val permittedTabs = remember(userProfile) {
        val list = mutableListOf<PermittedTab>()
        val perms = userProfile?.permissions ?: emptySet()

        if (isSuperAdmin || perms.contains(UserPermission.MANAGE_VITRINES)) {
            list.add(PermittedTab(0, GenesysStrings.VitrineTab, GenesysIcons.Web))
        }
        if (isSuperAdmin || perms.contains(UserPermission.MANAGE_ORDERS)) {
            list.add(PermittedTab(1, GenesysStrings.OrdersTab, GenesysIcons.List, state.pendingOrdersCount))
        }
        if (isSuperAdmin || perms.contains(UserPermission.MANAGE_AGENDA)) {
            list.add(PermittedTab(2, "Agenda", GenesysIcons.Schedule))
        }
        if (isSuperAdmin || perms.contains(UserPermission.MANAGE_SERVICES)) {
            list.add(PermittedTab(3, "Serviços", GenesysIcons.Inventory))
        }
        if (isSuperAdmin || perms.contains(UserPermission.MANAGE_RECEIPTS)) {
            list.add(PermittedTab(4, "Notas", GenesysIcons.ReceiptLong))
        }
        if (isSuperAdmin || perms.contains(UserPermission.MANAGE_STORE)) {
            list.add(PermittedTab(5, "Loja", GenesysIcons.Settings))
        }
        if (isSuperAdmin) {
            list.add(PermittedTab(6, "SuperAdmin", GenesysIcons.AdminPanelSettings))
        }
        list
    }

    // Ajusta aba selecionada se a atual não for permitida
    LaunchedEffect(permittedTabs) {
        if (permittedTabs.none { it.id == state.selectedTab }) {
            permittedTabs.firstOrNull()?.let { onEvent(PageListEvent.OnTabSelected(it.id)) }
        }
    }

    GenesysPage(
        topBar = {
            GenesysColumn(usePadding = false, modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                GenesysTopAppBar(
                    title = GenesysStrings.AdminTitle,
                    onBack = null,
                    actions = {
                        GenesysIconButton(icon = GenesysIcons.ReceiptLong, contentDescription = "Notas Fiscais", onClick = onOpenReceipts)
                        GenesysIconButton(icon = GenesysIcons.Magic, contentDescription = "Design System", onClick = onShowcase)
                        GenesysIconButton(icon = GenesysIcons.Numbers, contentDescription = "Exportar Tudo", onClick = onExportAll)
                        GenesysIconButton(icon = GenesysIcons.CloudUpload, contentDescription = "Importar Backup", onClick = onImport)
                        GenesysIconButton(icon = GenesysIcons.Settings, onClick = { onEvent(PageListEvent.OnGlobalSettingsClicked) })
                        GenesysIconButton(icon = GenesysIcons.Add, onClick = { onEvent(PageListEvent.OnCreatePageClicked) })
                    },
                )

                GenesysTabRow(
                    selectedTabIndex = permittedTabs.indexOfFirst { it.id == state.selectedTab }.coerceAtLeast(0),
                    tabs = permittedTabs.map { GenesysTabData(it.label, it.icon, badgeCount = it.badgeCount) },
                    onTabSelected = { index -> onEvent(PageListEvent.OnTabSelected(permittedTabs[index].id)) },
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
                            4 -> {
                                val receiptViewModel: ReceiptViewModel = koinInject()
                                com.itbenevides.genesys21.presentation.receipt.ReceiptListScreen(
                                    viewModel = receiptViewModel,
                                    isEmbedded = true,
                                    onOpenUrl = { url -> com.itbenevides.genesys21.openUrlInNewTab(url) }
                                )
                            }
                            5 -> StoreSettingsTabUI(viewModel, userProfile, uriHandler)
                            6 -> if (isSuperAdmin) SuperAdminDashboard(viewModel)
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
private fun StoreSettingsTabUI(
    viewModel: PageViewModel,
    userProfile: UserProfile?,
    uriHandler: androidx.compose.ui.platform.UriHandler
) {
    val pages by viewModel.pages.collectAsState()
    val firstPage = pages.firstOrNull()
    val storeId = firstPage?.storeId ?: "admin"

    var store by remember { mutableStateOf<com.itbenevides.genesys21.domain.model.Store?>(null) }

    var originZip by remember { mutableStateOf("") }
    var originStreet by remember { mutableStateOf("") }
    var originNumber by remember { mutableStateOf("") }
    var originNeighborhood by remember { mutableStateOf("") }
    var originCity by remember { mutableStateOf("") }
    var originState by remember { mutableStateOf("") }

    var allowPayLocal by remember { mutableStateOf(true) }
    var allowPayApp by remember { mutableStateOf(true) }
    var allowPickup by remember { mutableStateOf(true) }
    var allowDelivery by remember { mutableStateOf(true) }

    var stripePublic by remember { mutableStateOf("") }
    var stripeSecret by remember { mutableStateOf("") }
    var asaasKey by remember { mutableStateOf("") }
    var selectedGateway by remember { mutableStateOf("STRIPE") }

    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(storeId) {
        viewModel.getStore(storeId).onSuccess { s ->
            store = s
            originZip = s.originZipCode ?: ""
            originStreet = s.originStreet ?: ""
            originNumber = s.originNumber ?: ""
            originNeighborhood = s.originNeighborhood ?: ""
            originCity = s.originCity ?: ""
            originState = s.originState ?: ""
            allowPayLocal = s.allowPayOnLocation
            allowPayApp = s.allowPayInApp
            allowPickup = s.allowPickup
            allowDelivery = s.allowDelivery
            stripePublic = s.stripePublicKey ?: ""
            stripeSecret = s.stripeSecretKey ?: ""
            asaasKey = s.asaasApiKey ?: ""
            selectedGateway = s.paymentGateway
        }
    }

    GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = true) {
        GenesysSpacer(GenesysSpacing.Large)
        GenesysText(text = "Configurações da Loja", style = GenesysTextStyle.Headline, fontWeight = GenesysFontWeight.ExtraBold)
        GenesysText(text = "Configure os dados de remetente e as opções do checkout.", style = GenesysTextStyle.Body, color = MaterialTheme.colorScheme.onSurfaceVariant)

        GenesysSpacer(GenesysSpacing.Large)

        GenesysCard {
            GenesysColumn(usePadding = false) {
                GenesysText(text = "Dados do Remetente (Frete)", style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold)
                GenesysSpacer(GenesysSpacing.Medium)

                GenesysTextField(value = originZip, onValueChange = { originZip = it }, label = "CEP de Origem", icon = GenesysIcons.Search)
                GenesysSpacer(GenesysSpacing.Medium)
                GenesysTextField(value = originStreet, onValueChange = { originStreet = it }, label = "Rua/Logradouro")
                GenesysSpacer(GenesysSpacing.Medium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f)) { GenesysTextField(value = originNumber, onValueChange = { originNumber = it }, label = "Número") }
                    Box(Modifier.weight(2f)) { GenesysTextField(value = originNeighborhood, onValueChange = { originNeighborhood = it }, label = "Bairro") }
                }
                GenesysSpacer(GenesysSpacing.Medium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(2f)) { GenesysTextField(value = originCity, onValueChange = { originCity = it }, label = "Cidade") }
                    Box(Modifier.weight(1f)) { GenesysTextField(value = originState, onValueChange = { originState = it }, label = "UF") }
                }
            }
        }

        GenesysSpacer(GenesysSpacing.Large)

        GenesysCard {
            GenesysColumn(usePadding = false) {
                GenesysText(text = "Opções de Pagamento e Entrega", style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold)
                GenesysSpacer(GenesysSpacing.Medium)

                ToggleOptionRow("Permitir Pagar no Local", allowPayLocal) { allowPayLocal = it }
                ToggleOptionRow("Permitir Pagar pelo App", allowPayApp) { allowPayApp = it }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                ToggleOptionRow("Permitir Retirada no Local", allowPickup) { allowPickup = it }
                ToggleOptionRow("Permitir Envio / Entrega", allowDelivery) { allowDelivery = it }
            }
        }

        GenesysSpacer(GenesysSpacing.Large)

    GenesysCard {
        GenesysColumn(usePadding = false) {
            GenesysText(
                text = "Pagamentos (Stripe Connect)",
                style = GenesysTextStyle.Title,
                fontWeight = GenesysFontWeight.Bold
            )
            GenesysText(
                text = "Receba pagamentos diretamente em sua conta bancária.",
                style = GenesysTextStyle.Label,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            GenesysSpacer(GenesysSpacing.Medium)

            if (store?.stripeAccountId.isNullOrBlank()) {
                GenesysLoadingButton(
                    text = "Conectar com Stripe",
                    icon = GenesysIcons.Payments,
                    onClick = {
                        val userEmail = userProfile?.email ?: ""
                        viewModel.connectStripe(storeId, userEmail) { url ->
                            uriHandler.openUri(url)
                        }
                    },
                    isLoading = isLoading,
                    fillWidth = true
                )
            } else {
                GenesysRow(
                    modifier = Modifier.fillMaxWidth().background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    ).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    usePadding = false
                ) {
                    Icon(
                        GenesysIcons.Check,
                        null,
                        tint = Color(0xFF34C759),
                        modifier = Modifier.size(20.dp)
                    )
                    GenesysSpacer(GenesysSpacing.Small)
                    GenesysText(
                        text = "Stripe Conectado",
                        style = GenesysTextStyle.Body,
                        fontWeight = GenesysFontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                GenesysSpacer(GenesysSpacing.Medium)

                GenesysLoadingButton(
                    text = "Abrir Dashboard Stripe",
                    icon = GenesysIcons.Language,
                    containerColor = MaterialTheme.colorScheme.secondary,
                    onClick = {
                        viewModel.openStripeDashboard(storeId) { url ->
                            uriHandler.openUri(url)
                        }
                    },
                    isLoading = isLoading,
                    fillWidth = true
                )

                GenesysSpacer(GenesysSpacing.Small)
                GenesysText(
                    text = "Gerencie seus ganhos, reembolsos e dados bancários.",
                    style = GenesysTextStyle.Label,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = GenesysTextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Manter campos extras apenas para o Asaas (ou remover se não usar mais)
            if (selectedGateway == "ASAAS") {
                GenesysSpacer(GenesysSpacing.Large)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                GenesysSpacer(GenesysSpacing.Medium)
                GenesysTextField(
                    value = asaasKey,
                    onValueChange = { asaasKey = it },
                    label = "Asaas API Key",
                    placeholder = "$"
                )
            }
        }
    }

        GenesysSpacer(GenesysSpacing.Huge)

        GenesysLoadingButton(
            text = "Salvar Configurações",
            onClick = {
                val currentStore = store ?: com.itbenevides.genesys21.domain.model.Store(
                    id = storeId,
                    ownerId = "",
                    name = "Minha Loja"
                )
                val updated = currentStore.copy(
                    originZipCode = originZip,
                    originStreet = originStreet,
                    originNumber = originNumber,
                    originNeighborhood = originNeighborhood,
                    originCity = originCity,
                    originState = originState,
                    allowPayOnLocation = allowPayLocal,
                    allowPayInApp = allowPayApp,
                    allowPickup = allowPickup,
                    allowDelivery = allowDelivery,
                    stripePublicKey = if (selectedGateway == "STRIPE") stripePublic else null,
                    stripeSecretKey = if (selectedGateway == "STRIPE") stripeSecret else null,
                    stripeAccountId = currentStore.stripeAccountId, // Preserva o ID da conta Connect
                    asaasApiKey = asaasKey,
                    paymentGateway = selectedGateway
                )
                viewModel.saveStore(updated) {
                    // Feedback opcional
                }
            },
            fillWidth = true,
            isLoading = isLoading
        )
    }
}

@Composable
private fun ToggleOptionRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
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
    val windowSizeClass = LocalWindowSizeClass.current
    val isCompact = windowSizeClass == GenesysWindowSizeClass.COMPACT

    GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = true) {
        GenesysSpacer(GenesysSpacing.Large)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                GenesysText(text = "Gestão de Serviços", style = GenesysTextStyle.Headline, fontWeight = GenesysFontWeight.ExtraBold)
                GenesysText(
                    text = "Configure os tratamentos e preços do seu negócio.",
                    style = GenesysTextStyle.Body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            GenesysLoadingButton(
                text = if (isCompact) "" else "Novo Serviço",
                icon = GenesysIcons.Add,
                onClick = onAddService,
                fillWidth = false
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
            val columns = if (isCompact) 1 else 2

            Column {
                services.chunked(columns).forEach { rowServices ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowServices.forEach { service ->
                            Box(modifier = Modifier.weight(1f)) {
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
                        }
                        if (rowServices.size < columns) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
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
    val windowSizeClass = LocalWindowSizeClass.current
    val isCompact = windowSizeClass == GenesysWindowSizeClass.COMPACT

    GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = false) {
        GenesysSpacer(GenesysSpacing.Large)
        if (isCompact) {
            GenesysColumn(modifier = Modifier.fillMaxWidth(), usePadding = true) {
                GenesysStatsCard(
                    label = GenesysStrings.Revenue,
                    value = "${GenesysStrings.PricePrefix}$totalRevenue",
                    color = Color(0xFF34C759),
                )
                GenesysSpacer(GenesysSpacing.Small)
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
                GenesysSpacer(GenesysSpacing.Medium)
                GenesysWeightBox(
                    1f,
                ) { GenesysStatsCard(label = GenesysStrings.Pending, value = totalPending.toString(), color = Color(0xFFFF9500)) }
            }
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
                        text = item.name,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    val subtotal = (item.price * item.quantity * 100.0).roundToLong() / 100.0
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
