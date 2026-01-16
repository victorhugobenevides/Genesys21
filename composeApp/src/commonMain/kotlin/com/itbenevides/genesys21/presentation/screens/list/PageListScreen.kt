package com.itbenevides.genesys21.presentation.screens.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import com.itbenevides.genesys21.getWebBaseUrl
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.theme.iOSSeparator

@Composable
fun PageListScreen(
    viewModel: PageViewModel,
    onAddPage: () -> Unit,
    onEditPage: (Page) -> Unit,
    onViewPage: (Page) -> Unit,
    onLogout: () -> Unit
) {
    val pages by viewModel.pages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var newPageTitle by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.loadPages() }

    PageListContent(
        pages = pages,
        isLoading = isLoading,
        onAddClick = { showCreateDialog = true },
        onLogoutClick = onLogout,
        onPageClick = onViewPage,
        onEditTitleClick = onEditPage,
        onDeleteClick = { viewModel.deletePage(it) { viewModel.loadPages() } }
    )

    if (showCreateDialog) {
        CreatePageDialog(
            title = newPageTitle,
            onTitleChange = { newPageTitle = it },
            onDismiss = { showCreateDialog = false; newPageTitle = "" },
            onConfirm = {
                val id = (1..8).map { "abcdefghijklmnopqrstuvwxyz0123456789".random() }.joinToString("")
                viewModel.savePage(Page(id, newPageTitle), false) {
                    showCreateDialog = false
                    newPageTitle = ""
                    viewModel.loadPages()
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageListContent(
    pages: List<Page>,
    isLoading: Boolean,
    onAddClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onPageClick: (Page) -> Unit,
    onEditTitleClick: (Page) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    val uriHandler = LocalUriHandler.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Páginas", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    TextButton(onClick = onLogoutClick) {
                        Text("Sair", color = MaterialTheme.colorScheme.primary, fontSize = 17.sp)
                    }
                },
                actions = {
                    IconButton(onClick = onAddClick) {
                        Icon(Icons.Default.Add, "Novo", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Text(
                        "Minhas Páginas",
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 34.sp, fontWeight = FontWeight.ExtraBold),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }

                item {
                    Surface(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        tonalElevation = 0.dp
                    ) {
                        Column {
                            if (pages.isEmpty() && !isLoading) {
                                Text("Nenhuma página criada", modifier = Modifier.padding(24.dp).align(Alignment.CenterHorizontally), color = Color.Gray)
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
                                if (index < pages.size - 1) {
                                    HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp, color = iOSSeparator)
                                }
                            }
                        }
                    }
                }
            }

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter), color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun PageItemRow(
    page: Page, 
    onClick: () -> Unit, 
    onEditTitle: () -> Unit, 
    onShare: () -> Unit, 
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(page.title, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp))
            Text("ID: ${page.id}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        
        Box {
            IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.MoreHoriz, "Opções", tint = Color(0xFFC4C4C6))
            }
            
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(
                    text = { Text("Compartilhar") },
                    leadingIcon = { Icon(Icons.Default.Link, null, modifier = Modifier.size(18.dp)) },
                    onClick = { showMenu = false; onShare() }
                )
                DropdownMenuItem(
                    text = { Text("Renomear") },
                    leadingIcon = { Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp)) },
                    onClick = { showMenu = false; onEditTitle() }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                DropdownMenuItem(
                    text = { Text("Excluir", color = Color.Red) },
                    leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(18.dp)) },
                    onClick = { showMenu = false; onDelete() }
                )
            }
        }

        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color(0xFFC4C4C6), modifier = Modifier.size(18.dp))
    }
}

@Composable
fun CreatePageDialog(
    title: String,
    onTitleChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nova Página", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("Título da página") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank(),
                onClick = onConfirm
            ) { Text("Criar", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
