package com.itbenevides.genesys21.presentation.screens.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.Order
import com.itbenevides.genesys21.domain.model.OrderStatus
import com.itbenevides.genesys21.getWebBaseUrl
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.theme.iOSSeparator

@OptIn(ExperimentalMaterial3Api::class)
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
    val isLoading by viewModel.isLoading.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showGlobalSettings by remember { mutableStateOf(false) }
    var newPageTitle by remember { mutableStateOf("") }

    val pendingOrdersCount = remember(orders) {
        orders.count { it.status == OrderStatus.PENDING }
    }

    LaunchedEffect(Unit) { 
        viewModel.loadPages() 
        viewModel.loadOrders()
    }

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text("Administração", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                    navigationIcon = {
                        TextButton(onClick = onLogout) {
                            Text("Sair", color = MaterialTheme.colorScheme.primary, fontSize = 17.sp)
                        }
                    },
                    actions = {
                        IconButton(onClick = { showGlobalSettings = true }) {
                            Icon(Icons.Default.Settings, "Configurações Globais", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { showCreateDialog = true }) {
                            Icon(Icons.Default.Add, "Novo", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
                
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Vitrine", fontWeight = if(selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                        icon = { Icon(Icons.Default.Web, null) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { 
                            BadgedBox(
                                badge = { if (pendingOrdersCount > 0) Badge { Text(pendingOrdersCount.toString()) } }
                            ) {
                                Text("Pedidos", fontWeight = if(selectedTab == 1) FontWeight.Bold else FontWeight.Normal) 
                            }
                        },
                        icon = { Icon(Icons.AutoMirrored.Filled.ListAlt, null) }
                    )
                }
            }
        }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            val maxWidthContent = 1000.dp
            val horizontalPadding = if (maxWidth > maxWidthContent) (maxWidth - maxWidthContent) / 2 else 12.dp

            Box(Modifier.fillMaxSize()) {
                if (selectedTab == 0) {
                    PagesTabContent(pages, isLoading, horizontalPadding, onViewPage, onEditPage, { viewModel.deletePage(it) { viewModel.loadPages() } })
                } else {
                    OrdersTabContent(orders, isLoading, horizontalPadding, viewModel) 
                }
            }
        }
    }

    if (showCreateDialog) {
        CreatePageDialog(
            title = newPageTitle,
            onTitleChange = { newPageTitle = it },
            onDismiss = { showCreateDialog = false; newPageTitle = "" },
            onConfirm = {
                val id = (1..8).map { "abcdefghijklmnopqrstuvwxyz0123456789".random() }.joinToString("")
                val newPage = Page(id, newPageTitle)
                viewModel.savePage(newPage, false) {
                    showCreateDialog = false
                    newPageTitle = ""
                    onEditPage(newPage)
                }
            }
        )
    }

    if (showGlobalSettings && pages.isNotEmpty()) {
        val firstPage = pages.first()
        GlobalSettingsDialog(
            initialDomain = firstPage.customDomain ?: "",
            initialWhatsapp = firstPage.whatsapp ?: "",
            onDismiss = { showGlobalSettings = false },
            onConfirm = { domain, whatsapp ->
                val updatedPage = firstPage.copy(
                    customDomain = if (domain.isBlank()) null else domain,
                    whatsapp = if (whatsapp.isBlank()) null else whatsapp
                )
                viewModel.savePage(updatedPage, true) {
                    showGlobalSettings = false
                    viewModel.loadPages()
                }
            }
        )
    }
}

@Composable
fun PagesTabContent(
    pages: List<Page>,
    isLoading: Boolean,
    horizontalPadding: androidx.compose.ui.unit.Dp,
    onViewClick: (Page) -> Unit,
    onEditClick: (Page) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (pages.isEmpty() && !isLoading) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Nenhuma página criada", color = Color.Gray)
                }
            }
        }
        
        items(pages) { page ->
            PageItemRow(
                page = page,
                onView = { onViewClick(page) },
                onEdit = { onEditClick(page) },
                onCopyUrl = { 
                    val baseUrl = getWebBaseUrl()
                    val url = "$baseUrl/p/${page.id}"
                    clipboardManager.setText(AnnotatedString(url))
                },
                onDelete = { onDeleteClick(page.id) }
            )
        }
    }
}

@Composable
fun PageItemRow(
    page: Page,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onCopyUrl: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        tonalElevation = 1.dp,
        shadowElevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = CircleShape,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Language, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(page.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text("ID: ${page.id}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onView) {
                    Icon(Icons.Default.Visibility, "Visualizar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Editar", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onCopyUrl) {
                    Icon(Icons.Default.ContentCopy, "Copiar Link", tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, "Excluir", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun OrdersTabContent(
    orders: List<Order>, 
    isLoading: Boolean, 
    horizontalPadding: androidx.compose.ui.unit.Dp,
    viewModel: PageViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf<OrderStatus?>(null) }

    val filteredOrders = remember(orders, searchQuery, selectedStatusFilter) {
        orders.filter { order ->
            val matchesSearch = searchQuery.isBlank() || 
                order.id.contains(searchQuery, ignoreCase = true) ||
                (order.customerName?.contains(searchQuery, ignoreCase = true) == true)
            
            val matchesStatus = selectedStatusFilter == null || order.status == selectedStatusFilter
            
            matchesSearch && matchesStatus
        }
    }

    val totalRevenue = remember(orders) { orders.filter { it.status == OrderStatus.COMPLETED }.sumOf { it.total } }
    val totalPending = remember(orders) { orders.filter { it.status == OrderStatus.PENDING }.size }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = horizontalPadding, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatsCard(
                label = "Vendas (Concluídas)",
                value = "R$ $totalRevenue",
                color = Color(0xFF388E3C),
                modifier = Modifier.weight(1f)
            )
            StatsCard(
                label = "Novos Pedidos",
                value = totalPending.toString(),
                color = Color(0xFFFBC02D),
                modifier = Modifier.weight(1f)
            )
        }

        Column(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = horizontalPadding, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar por ID ou Cliente...") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                leadingIcon = { Icon(Icons.Default.Search, null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedStatusFilter == null,
                    onClick = { selectedStatusFilter = null },
                    label = { Text("Todos (${orders.size})") }
                )

                OrderStatus.entries.forEach { status ->
                    val count = orders.count { it.status == status }
                    val label = when(status) {
                        OrderStatus.PENDING -> "Pendentes"
                        OrderStatus.PROCESSING -> "Em Andamento"
                        OrderStatus.COMPLETED -> "Concluídos"
                        OrderStatus.CANCELLED -> "Cancelados"
                    }
                    FilterChip(
                        selected = selectedStatusFilter == status,
                        onClick = { selectedStatusFilter = status },
                        label = { Text("$label ($count)") }
                    )
                }
            }
        }

        if (filteredOrders.isEmpty() && !isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.SearchOff, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Text("Nenhum pedido encontrado", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(horizontal = horizontalPadding)) {
                items(filteredOrders) { order ->
                    OrderCard(order, onStatusUpdate = { viewModel.updateOrderStatus(order.id, it) })
                }
            }
        }
    }
}

@Composable
fun StatsCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
fun OrderCard(order: Order, onStatusUpdate: (OrderStatus) -> Unit) {
    val uriHandler = LocalUriHandler.current
    var showStatusMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Pedido #${order.id.takeLast(6).uppercase()}", fontWeight = FontWeight.Bold)
                    if (!order.customerName.isNullOrBlank()) {
                        Text("De: ${order.customerName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
                
                Box {
                    Surface(
                        onClick = { showStatusMenu = true },
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Transparent
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(4.dp)) {
                            StatusBadge(order.status)
                            Icon(Icons.Default.ArrowDropDown, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                        }
                    }
                    
                    DropdownMenu(expanded = showStatusMenu, onDismissRequest = { showStatusMenu = false }) {
                        Text("Mudar Status:", modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        DropdownMenuItem(text = { Text("Pendente") }, onClick = { onStatusUpdate(OrderStatus.PENDING); showStatusMenu = false })
                        DropdownMenuItem(text = { Text("Processando") }, onClick = { onStatusUpdate(OrderStatus.PROCESSING); showStatusMenu = false })
                        DropdownMenuItem(text = { Text("Concluído") }, onClick = { onStatusUpdate(OrderStatus.COMPLETED); showStatusMenu = false })
                        DropdownMenuItem(text = { Text("Cancelado", color = Color.Red) }, onClick = { onStatusUpdate(OrderStatus.CANCELLED); showStatusMenu = false })
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(Modifier.height(12.dp))

            order.items.forEach { item ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("• ${item.quantity}x ${item.product.name}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text("R$ ${item.product.price * item.quantity}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
            
            Spacer(Modifier.height(12.dp))
            Text("Total: R$ ${order.total}", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
            
            if (!order.whatsappContact.isNullOrBlank()) {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { 
                        val url = "https://wa.me/${order.whatsappContact}"
                        uriHandler.openUri(url)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, null) 
                    Spacer(Modifier.width(8.dp))
                    Text("Falar com Cliente")
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: OrderStatus) {
    val color = when(status) {
        OrderStatus.PENDING -> Color(0xFFFBC02D)
        OrderStatus.PROCESSING -> Color(0xFF1976D2)
        OrderStatus.COMPLETED -> Color(0xFF388E3C)
        OrderStatus.CANCELLED -> Color(0xFFD32F2F)
    }
    val label = when(status) {
        OrderStatus.PENDING -> "PENDENTE"
        OrderStatus.PROCESSING -> "EM ANDAMENTO"
        OrderStatus.COMPLETED -> "CONCLUÍDO"
        OrderStatus.CANCELLED -> "CANCELADO"
    }
    Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            Box(Modifier.size(6.dp).background(color, CircleShape))
            Spacer(Modifier.width(6.dp))
            Text(label, color = color, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
fun CreatePageDialog(title: String, onTitleChange: (String) -> Unit, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nova Página", fontWeight = FontWeight.Bold) },
        text = { OutlinedTextField(value = title, onValueChange = onTitleChange, label = { Text("Título") }, modifier = Modifier.fillMaxWidth()) },
        confirmButton = { Button(onClick = onConfirm, enabled = title.isNotBlank()) { Text("Criar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun GlobalSettingsDialog(initialDomain: String, initialWhatsapp: String, onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var domain by remember { mutableStateOf(initialDomain) }
    var whatsapp by remember { mutableStateOf(initialWhatsapp) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurações Globais", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = domain, 
                    onValueChange = { domain = it }, 
                    label = { Text("Domínio Customizado") },
                    placeholder = { Text("ex: meusite.com") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = whatsapp, 
                    onValueChange = { whatsapp = it }, 
                    label = { Text("WhatsApp") },
                    placeholder = { Text("Ex: 5511999999999") },
                    supportingText = { Text("Digite apenas números com DDD") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = { Button(onClick = { onConfirm(domain, whatsapp) }) { Text("Salvar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
