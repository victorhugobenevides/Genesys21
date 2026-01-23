package com.itbenevides.genesys21.presentation.screens.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.platform.LocalUriHandler
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
    
    var selectedTab by remember { mutableStateOf(0) } // 0 = Páginas, 1 = Pedidos
    
    var showCreateDialog by remember { mutableStateOf(false) }
    var showGlobalSettings by remember { mutableStateOf(false) }
    var newPageTitle by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { 
        viewModel.loadPages() 
        viewModel.loadOrders()
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == 1) viewModel.loadOrders()
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
                        text = { Text("Pedidos", fontWeight = if(selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                        icon = { Icon(Icons.AutoMirrored.Filled.ListAlt, null) }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            if (selectedTab == 0) {
                PagesTabContent(pages, isLoading, onViewPage, onEditPage, { viewModel.deletePage(it) { viewModel.loadPages() } })
            } else {
                OrdersTabContent(orders, isLoading) 
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
                    customDomain = domain.ifBlank { null },
                    whatsapp = whatsapp.ifBlank { null }
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
    onPageClick: (Page) -> Unit,
    onEditTitleClick: (Page) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    val uriHandler = LocalUriHandler.current
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Surface(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White
            ) {
                Column {
                    if (pages.isEmpty() && !isLoading) {
                        Text("Nenhuma página criada", modifier = Modifier.padding(32.dp).align(Alignment.CenterHorizontally), color = Color.Gray)
                    }
                    pages.forEachIndexed { index, page ->
                        PageItemRow(
                            page = page,
                            onClick = { onPageClick(page) },
                            onEditTitle = { onEditTitleClick(page) },
                            onShare = { 
                                val baseUrl = getWebBaseUrl()
                                uriHandler.openUri("$baseUrl/p/${page.id}")
                            },
                            onDelete = { onDeleteClick(page.id) }
                        )
                        if (index < pages.size - 1) HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp, color = iOSSeparator)
                    }
                }
            }
        }
    }
}

@Composable
fun OrdersTabContent(orders: List<Order>, isLoading: Boolean) {
    if (orders.isEmpty() && !isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Inbox, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                Text("Nenhum pedido recebido ainda", color = Color.Gray)
            }
        }
    } else {
        LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
            items(orders) { order ->
                OrderCard(order)
            }
        }
    }
}

@Composable
fun OrderCard(order: Order) {
    val uriHandler = LocalUriHandler.current
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Pedido #${order.id.takeLast(6)}", fontWeight = FontWeight.Bold)
                StatusBadge(order.status)
            }
            Spacer(Modifier.height(8.dp))
            order.items.forEach { item ->
                Text("• ${item.quantity}x ${item.product.name}", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(8.dp))
            Text("Total: R$ ${order.total}", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            
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
        OrderStatus.PROCESSING -> "PROCESSANDO"
        OrderStatus.COMPLETED -> "CONCLUÍDO"
        OrderStatus.CANCELLED -> "CANCELADO"
    }
    Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
        Text(label, color = color, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
    }
}

@Composable
fun PageItemRow(page: Page, onClick: () -> Unit, onEditTitle: () -> Unit, onShare: () -> Unit, onDelete: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp).clickable { onClick() }, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(page.title, fontWeight = FontWeight.Bold)
            Text("ID: ${page.id}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
        Box {
            IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, null) }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(text = { Text("Compartilhar") }, onClick = { onShare(); showMenu = false }, leadingIcon = { Icon(Icons.Default.Share, null) })
                DropdownMenuItem(text = { Text("Renomear") }, onClick = { onEditTitle(); showMenu = false }, leadingIcon = { Icon(Icons.Default.Edit, null) })
                DropdownMenuItem(text = { Text("Excluir", color = Color.Red) }, onClick = { onDelete(); showMenu = false }, leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) })
            }
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.LightGray)
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
