package com.itbenevides.genesys21.presentation.screens.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.OrderStatus
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.PageTemplateType
import com.itbenevides.genesys21.domain.model.PageTemplates
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.components.appbar.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.button.GenesysIconButton
import com.itbenevides.genesys21.ui.components.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.button.GenesysTextButton
import com.itbenevides.genesys21.ui.components.card.GenesysCard
import com.itbenevides.genesys21.ui.components.card.GenesysStatsCard
import com.itbenevides.genesys21.ui.components.feedback.GenesysDialog
import com.itbenevides.genesys21.ui.components.feedback.GenesysEmptyState
import com.itbenevides.genesys21.ui.components.input.GenesysFilterChip
import com.itbenevides.genesys21.ui.components.input.GenesysTextField
import com.itbenevides.genesys21.ui.components.layout.*
import com.itbenevides.genesys21.ui.components.text.*
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.GenesysStrings
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
                // CORREÇÃO: Usando o PageTemplates para criar a página baseada no tipo selecionado
                val newPage = PageTemplates.create(event.templateType, state.newPageTitle.trim())
                viewModel.savePage(newPage, isEditing = false) {
                    state = state.copy(showCreateDialog = false, newPageTitle = "")
                    viewModel.loadPages()
                }
            }
            is PageListEvent.OnDeletePageClicked -> viewModel.deletePage(event.pageId) { viewModel.loadPages() }
            is PageListEvent.OnLogoutClicked -> onLogout()
            is PageListEvent.OnRenamePageClicked -> state = state.copy(showRenameDialog = true, pageToRename = event.page)
            is PageListEvent.OnDismissRenameDialog -> state = state.copy(showRenameDialog = false, pageToRename = null)
            is PageListEvent.OnConfirmRenamePage -> {
                state.pageToRename?.let { page ->
                    viewModel.savePage(page.copy(title = event.newTitle), true) {
                        state = state.copy(showRenameDialog = false, pageToRename = null)
                        viewModel.loadPages()
                    }
                }
            }
            is PageListEvent.OnGlobalSettingsClicked -> state = state.copy(showGlobalSettings = true)
            is PageListEvent.OnDismissGlobalSettings -> state = state.copy(showGlobalSettings = false)
            is PageListEvent.OnUpdateOrderStatus -> viewModel.updateOrderStatus(event.orderId, event.newStatus)
            else -> { }
        }
    }

    PageListContent(
        state = state, 
        onEvent = onEvent, 
        onViewPage = onViewPage, 
        onEditPage = onEditPage,
        onContactCustomer = { phone, orderId, name ->
            val message = "Olá $name, estou entrando em contato sobre o seu pedido #$orderId."
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
    onContactCustomer: (String, String, String) -> Unit
) {
     GenesysPage(
        topBar = {
            GenesysColumn(usePadding = false, modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                GenesysTopAppBar(
                    title = GenesysStrings.AdminTitle,
                    onBack = null,
                    actions = {
                        GenesysIconButton(icon = GenesysIcons.Settings, onClick = { onEvent(PageListEvent.OnGlobalSettingsClicked) })
                        GenesysIconButton(
                            icon = GenesysIcons.Add, 
                            onClick = { onEvent(PageListEvent.OnCreatePageClicked) },
                            modifier = Modifier.testTag("btn_create_page")
                        )
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 64.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item { DashboardSummaryUI(state) }
                    item { PagesTabUI(state, onEvent, onViewPage, onEditPage, isWideScreen) }
                    item { LogoutButtonUI(onEvent) }
                }
            } else {
                OrdersLayoutUI(state, onEvent, isWideScreen, onContactCustomer)
            }
        }
    }

    if (state.showCreateDialog) CreatePageDialog(state, onEvent)
    if (state.showRenameDialog) RenamePageDialog(state, onEvent)
}

@Composable
private fun DashboardSummaryUI(state: PageListState) {
    val totalRevenue = (state.orders.filter { it.status == OrderStatus.COMPLETED }.sumOf { it.total } * 100.0).roundToLong() / 100.0
    
    GenesysColumn(usePadding = true) {
        GenesysText(text = "Resumo Geral", style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold)
        GenesysSpacer(GenesysSpacing.Medium)
        GenesysRow(modifier = Modifier.fillMaxWidth(), usePadding = false) {
            GenesysWeightBox(1f) { 
                GenesysStatsCard(
                    label = "Vendas Totais", 
                    value = "${GenesysStrings.PricePrefix}$totalRevenue", 
                    color = Color(0xFF34C759) 
                ) 
            }
            GenesysSpacer(GenesysSpacing.Medium)
            GenesysWeightBox(1f) { 
                GenesysStatsCard(
                    label = "Aguardando", 
                    value = state.pendingOrdersCount.toString(), 
                    color = Color(0xFFFF9500) 
                ) 
            }
        }
        GenesysSpacer(GenesysSpacing.Large)
    }
}

@Composable
private fun PagesTabUI(state: PageListState, onEvent: (PageListEvent) -> Unit, onViewPage: (Page) -> Unit, onEditPage: (Page) -> Unit, isWideScreen: Boolean) {
    GenesysColumn(modifier = Modifier.widthIn(max = 1200.dp), usePadding = true) {
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
                    onDelete = { onEvent(PageListEvent.OnDeletePageClicked(page.id)) },
                    isWideScreen = isWideScreen
                )
                GenesysSpacer(GenesysSpacing.Medium)
            }
        }
    }
}

@Composable
private fun PageItemRow(page: Page, onView: () -> Unit, onEdit: () -> Unit, onRename: () -> Unit, onDelete: () -> Unit, isWideScreen: Boolean) {
    GenesysCard(modifier = Modifier.fillMaxWidth(), elevation = 1.dp) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) { 
                Icon(GenesysIcons.Web, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp)) 
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                GenesysText(text = page.title, style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                GenesysText(text = page.customDomain ?: "ID: ${page.id}", style = GenesysTextStyle.Label, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GenesysIconButton(icon = GenesysIcons.Visibility, onClick = onView, contentDescription = "Ver")
                GenesysIconButton(icon = GenesysIcons.Edit, onClick = onEdit, contentDescription = "Editar")
                
                var showMenu by remember { mutableStateOf(false) }
                Box {
                    GenesysIconButton(icon = GenesysIcons.Numbers, onClick = { showMenu = true })
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(text = { Text("Renomear") }, onClick = { showMenu = false; onRename() }, leadingIcon = { Icon(GenesysIcons.Edit, null) })
                        DropdownMenuItem(text = { Text("Excluir", color = Color.Red) }, onClick = { showMenu = false; onDelete() }, leadingIcon = { Icon(GenesysIcons.Delete, null, tint = Color.Red) })
                    }
                }
            }
        }
    }
}

@Composable
private fun LogoutButtonUI(onEvent: (PageListEvent) -> Unit) {
    GenesysColumn(usePadding = true, horizontalAlignment = GenesysAlignment.Center) {
        GenesysSpacer(GenesysSpacing.Huge)
        GenesysTextButton(text = GenesysStrings.Logout, onClick = { onLogoutClicked(onEvent) }, color = MaterialTheme.colorScheme.error)
        GenesysSpacer(GenesysSpacing.Huge)
    }
}

private fun onLogoutClicked(onEvent: (PageListEvent) -> Unit) {
    onEvent(PageListEvent.OnLogoutClicked)
}

@Composable
private fun CreatePageDialog(state: PageListState, onEvent: (PageListEvent) -> Unit) {
    var title by remember { mutableStateOf("") }
    var selectedTemplate by remember { mutableStateOf(PageTemplateType.STORE) }

    GenesysDialog(
        onDismissRequest = { onEvent(PageListEvent.OnDismissCreateDialog) },
        title = GenesysStrings.NewPageTitle,
        confirmButton = { 
            GenesysLoadingButton(
                text = "Criar", 
                onClick = { onEvent(PageListEvent.OnConfirmCreatePage(selectedTemplate)) }, 
                enabled = title.isNotBlank(), 
                isLoading = state.isLoading,
                fillWidth = true,
                modifier = Modifier.testTag("btn_confirm_create_page")
            ) 
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            GenesysTextField(
                value = title, 
                onValueChange = { title = it; onEvent(PageListEvent.OnNewPageTitleChanged(it)) }, 
                label = "Título da Vitrine", 
                placeholder = "Ex: Minha Loja Premium",
                modifier = Modifier.testTag("input_new_page_title")
            )

            Text("Selecione um Modelo:", style = MaterialTheme.typography.labelMedium)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TemplateOption(
                    label = "Loja", 
                    icon = GenesysIcons.ShoppingBag, 
                    isSelected = selectedTemplate == PageTemplateType.STORE,
                    onClick = { selectedTemplate = PageTemplateType.STORE }
                )
                TemplateOption(
                    label = "Bio", 
                    icon = GenesysIcons.Person, 
                    isSelected = selectedTemplate == PageTemplateType.BIO,
                    onClick = { selectedTemplate = PageTemplateType.BIO }
                )
                TemplateOption(
                    label = "Landing", 
                    icon = GenesysIcons.Web, 
                    isSelected = selectedTemplate == PageTemplateType.LANDING,
                    onClick = { selectedTemplate = PageTemplateType.LANDING }
                )
            }
        }
    }
}

@Composable
private fun RowScope.TemplateOption(
    label: String, 
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    isSelected: Boolean, 
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.weight(1f).clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RenamePageDialog(state: PageListState, onEvent: (PageListEvent) -> Unit) {
    var title by remember(state.pageToRename) { mutableStateOf(state.pageToRename?.title ?: "") }
    GenesysDialog(
        onDismissRequest = { onEvent(PageListEvent.OnDismissRenameDialog) },
        title = "Renomear Vitrine",
        confirmButton = { GenesysLoadingButton(text = "Salvar", onClick = { onEvent(PageListEvent.OnConfirmRenamePage(title)) }, enabled = title.isNotBlank(), isLoading = state.isLoading, fillWidth = true) }
    ) {
        GenesysTextField(value = title, onValueChange = { title = it }, label = "Novo Título")
    }
}

@Composable
private fun OrdersLayoutUI(state: PageListState, onEvent: (PageListEvent) -> Unit, isWideScreen: Boolean, onContact: (String, String, String) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        OrderFiltersRow(state, onEvent)
    }
}

@Composable
private fun OrderFiltersRow(state: PageListState, onEvent: (PageListEvent) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        GenesysFilterChip(selected = state.selectedStatusFilter == null, onClick = { onEvent(PageListEvent.OnStatusFilterSelected(null)) }, label = "Todos", badgeCount = state.orders.size)
    }
}
