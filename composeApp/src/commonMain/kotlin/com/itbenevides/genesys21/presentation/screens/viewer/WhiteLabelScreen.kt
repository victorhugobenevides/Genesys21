package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.presentation.PageViewModel
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhiteLabelScreen(viewModel: PageViewModel, page: Page, onBack: () -> Unit) {
    var currentPage by remember { mutableStateOf(page) }
    var showCatalog by remember { mutableStateOf(false) }
    
    var editingComponentIndex by remember { mutableStateOf<Int?>(null) }
    var pendingNewComponent by remember { mutableStateOf<PageComponent?>(null) }
    
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(currentPage.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Editor White Label", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Voltar", color = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.savePage(currentPage, true) { onBack() } }, enabled = !isLoading) {
                        if (isLoading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        else Text("Publicar", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCatalog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            if (currentPage.components.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Comece a montar sua página\nclicando no botão +", 
                        textAlign = TextAlign.Center, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    itemsIndexed(currentPage.components) { index, component ->
                        ComponentWrapper(
                            component = component,
                            onDelete = {
                                currentPage = currentPage.copy(components = currentPage.components.toMutableList().apply { removeAt(index) })
                            },
                            onEdit = {
                                editingComponentIndex = index
                            }
                        )
                    }
                }
            }

            if (showCatalog) {
                ComponentCatalogModal(
                    onComponentSelected = { newComponent ->
                        pendingNewComponent = newComponent
                        showCatalog = false
                    },
                    onTemplateSelected = { templateComponents ->
                        currentPage = currentPage.copy(components = currentPage.components + templateComponents)
                        showCatalog = false
                    },
                    onDismiss = { showCatalog = false }
                )
            }

            pendingNewComponent?.let { component ->
                EditComponentModal(
                    component = component,
                    isNew = true,
                    onComponentUpdated = { updated ->
                        currentPage = currentPage.copy(components = currentPage.components + updated)
                        pendingNewComponent = null
                    },
                    onDeleteRequest = { pendingNewComponent = null },
                    onDismiss = { pendingNewComponent = null }
                )
            }

            editingComponentIndex?.let { index ->
                EditComponentModal(
                    component = currentPage.components[index],
                    isNew = false,
                    onComponentUpdated = { updated ->
                        currentPage = currentPage.copy(
                            components = currentPage.components.toMutableList().apply { set(index, updated) }
                        )
                        editingComponentIndex = null
                    },
                    onDeleteRequest = {
                        currentPage = currentPage.copy(
                            components = currentPage.components.toMutableList().apply { removeAt(index) }
                        )
                        editingComponentIndex = null
                    },
                    onDismiss = { editingComponentIndex = null }
                )
            }
        }
    }
}

@Composable
fun PageComponentRenderer(component: PageComponent) {
    val commonShape = if (component.isRounded) CircleShape else RoundedCornerShape(12.dp)
    
    when (component) {
        is PageComponent.Logo -> {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier.size(component.size.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                ) {
                    if (component.url.isNotEmpty()) {
                        Box(contentAlignment = Alignment.Center) { Text("LOGO", style = MaterialTheme.typography.labelSmall) }
                    } else {
                        Icon(Icons.Default.Store, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(12.dp))
                    }
                }
            }
        }
        is PageComponent.ProductList -> {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Produtos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (component.products.isEmpty()) {
                    Text("Sem produtos", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                } else {
                    component.products.chunked(2).forEach { rowProducts ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            rowProducts.forEach { product ->
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = commonShape,
                                    color = if (component.isTransparent) Color.Transparent else MaterialTheme.colorScheme.surface,
                                    border = if (component.isTransparent) null else androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                ) {
                                    Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            Modifier.fillMaxWidth().aspectRatio(1f).clip(commonShape).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)), 
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.ShoppingBag, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(24.dp))
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        Text(product.name, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("R$ ${product.price}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                            if (rowProducts.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        is PageComponent.Header -> Text(
            text = component.title.ifBlank { "Título" }, 
            style = MaterialTheme.typography.headlineMedium, 
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = if (component.isRounded) TextAlign.Center else TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
        is PageComponent.Text -> Text(
            text = component.content.ifBlank { "Conteúdo..." }, 
            style = MaterialTheme.typography.bodyMedium, 
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = if (component.isRounded) TextAlign.Center else TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
        is PageComponent.Image -> {
            val imgShape = if (component.isRounded) CircleShape else RoundedCornerShape(12.dp)
            Column(horizontalAlignment = if (component.isRounded) Alignment.CenterHorizontally else Alignment.Start) {
                Box(
                    Modifier.fillMaxWidth().aspectRatio(if (component.isRounded) 1f else 1.7f).clip(imgShape).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)), 
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Image, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(48.dp))
                }
                if (component.string.isNotEmpty()) {
                    Text(component.string, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp).fillMaxWidth(), textAlign = if (component.isRounded) TextAlign.Center else TextAlign.Start)
                }
            }
        }
    }
}

@Composable
fun ComponentWrapper(
    component: PageComponent, 
    onDelete: () -> Unit, 
    onEdit: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // CONTROLES FORA DO CARD
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = component.customLabel ?: component::class.simpleName ?: "Componente",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onEdit, contentPadding = PaddingValues(horizontal = 8.dp), modifier = Modifier.height(32.dp)) {
                    Text("Editar", fontSize = 13.sp)
                }
                TextButton(onClick = onDelete, contentPadding = PaddingValues(horizontal = 8.dp), modifier = Modifier.height(32.dp)) {
                    Text("Remover", color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }
            }
        }

        // CARD DE CONTEÚDO (LIMPO)
        val shape = if (component.isRounded && component !is PageComponent.ProductList) CircleShape else RoundedCornerShape(12.dp)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = shape,
            color = if (component.isTransparent) Color.Transparent else MaterialTheme.colorScheme.surface,
            border = if (component.isTransparent) null else androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Box(Modifier.padding(if (component.isTransparent) 0.dp else 16.dp)) {
                PageComponentRenderer(component)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditComponentModal(
    component: PageComponent, 
    isNew: Boolean = false,
    onComponentUpdated: (PageComponent) -> Unit, 
    onDeleteRequest: () -> Unit,
    onDismiss: () -> Unit
) {
    // CORREÇÃO: Força o preenchimento total da tela no Modal
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 24.dp).navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(if (isNew) "Novo Item" else "Editar", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                TextButton(onClick = onDismiss) { Text("OK", fontWeight = FontWeight.Bold) }
            }
            
            Spacer(Modifier.height(16.dp))

            // Estados locais
            var currentCustomLabel by remember { mutableStateOf(component.customLabel ?: "") }
            var isTransparent by remember { mutableStateOf(component.isTransparent) }
            var isRounded by remember { mutableStateOf(component.isRounded) }
            var headerTitle by remember { mutableStateOf(if (component is PageComponent.Header) component.title else "") }
            var textContent by remember { mutableStateOf(if (component is PageComponent.Text) component.content else "") }
            var imageUrl by remember { mutableStateOf(if (component is PageComponent.Image) component.url else "") }
            var imageDesc by remember { mutableStateOf(if (component is PageComponent.Image) component.string else "") }
            var logoUrl by remember { mutableStateOf(if (component is PageComponent.Logo) component.url else "") }
            var logoSize by remember { mutableStateOf(if (component is PageComponent.Logo) component.size.toString() else "64") }
            var productList by remember { mutableStateOf(if (component is PageComponent.ProductList) component.products else emptyList()) }

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                // MINIATURA REALISTA
                val previewComp = when (component) {
                    is PageComponent.Header -> PageComponent.Header(headerTitle, currentCustomLabel.ifBlank { null }, isTransparent, isRounded)
                    is PageComponent.Text -> PageComponent.Text(textContent, customLabel = currentCustomLabel.ifBlank { null }, isTransparent = isTransparent, isRounded = isRounded)
                    is PageComponent.Image -> PageComponent.Image(imageUrl, imageDesc, currentCustomLabel.ifBlank { null }, isTransparent, isRounded)
                    is PageComponent.Logo -> PageComponent.Logo(logoUrl, logoSize.toIntOrNull() ?: 64, currentCustomLabel.ifBlank { null }, isTransparent, isRounded)
                    is PageComponent.ProductList -> PageComponent.ProductList(productList, currentCustomLabel.ifBlank { null }, isTransparent, isRounded)
                }

                Box(Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().widthIn(max = 280.dp),
                        shape = if (isRounded && component !is PageComponent.ProductList) CircleShape else RoundedCornerShape(12.dp),
                        color = if (isTransparent) Color.Transparent else Color(0xFFF9FAFB),
                        border = if (isTransparent) null else androidx.compose.foundation.BorderStroke(0.5.dp, Color.LightGray)
                    ) {
                        Box(Modifier.padding(12.dp)) { PageComponentRenderer(previewComp) }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                Spacer(Modifier.height(16.dp))

                // CAMPOS DE EDIÇÃO
                OutlinedTextField(value = currentCustomLabel, onValueChange = { currentCustomLabel = it }, label = { Text("Nome do Bloco") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                    Checkbox(checked = isTransparent, onCheckedChange = { isTransparent = it })
                    Text("Transparente", fontSize = 14.sp)
                    Spacer(Modifier.width(16.dp))
                    Checkbox(checked = isRounded, onCheckedChange = { isRounded = it })
                    Text("Redondo", fontSize = 14.sp)
                }

                when (component) {
                    is PageComponent.Header -> OutlinedTextField(value = headerTitle, onValueChange = { headerTitle = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                    is PageComponent.Text -> OutlinedTextField(value = textContent, onValueChange = { textContent = it }, label = { Text("Texto") }, modifier = Modifier.fillMaxWidth(), minLines = 3, shape = RoundedCornerShape(10.dp))
                    is PageComponent.Image -> {
                        OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("URL da Imagem") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = imageDesc, onValueChange = { imageDesc = it }, label = { Text("Legenda") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                    }
                    is PageComponent.Logo -> {
                        OutlinedTextField(value = logoUrl, onValueChange = { logoUrl = it }, label = { Text("URL do Logo") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = logoSize, onValueChange = { if(it.all { c -> c.isDigit() }) logoSize = it }, label = { Text("Tamanho (px)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                    }
                    is PageComponent.ProductList -> {
                        productList.forEachIndexed { pIndex, product ->
                            ProductEditCard(product, { productList = productList.toMutableList().apply { set(pIndex, it) } }, { productList = productList.toMutableList().apply { removeAt(pIndex) } })
                        }
                        Button(onClick = { productList = productList + Product(Random.nextInt().toString(), "", 0.0, "") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                            Text("+ Adicionar Produto")
                        }
                    }
                }
                Spacer(Modifier.height(100.dp)) // Espaço para não cobrir botões
            }

            val isDataValid = when (component) {
                is PageComponent.Header -> headerTitle.isNotBlank()
                is PageComponent.Text -> textContent.isNotBlank()
                is PageComponent.Image -> imageUrl.isNotBlank()
                is PageComponent.Logo -> logoUrl.isNotBlank()
                is PageComponent.ProductList -> productList.isNotEmpty()
            }

            ActionButtons(isNew, isDataValid, {
                val finalComp = when (component) {
                    is PageComponent.Header -> PageComponent.Header(headerTitle, currentCustomLabel.ifBlank { null }, isTransparent, isRounded)
                    is PageComponent.Text -> PageComponent.Text(textContent, customLabel = currentCustomLabel.ifBlank { null }, isTransparent = isTransparent, isRounded = isRounded)
                    is PageComponent.Image -> PageComponent.Image(imageUrl, imageDesc, currentCustomLabel.ifBlank { null }, isTransparent, isRounded)
                    is PageComponent.Logo -> PageComponent.Logo(logoUrl, logoSize.toIntOrNull() ?: 64, currentCustomLabel.ifBlank { null }, isTransparent, isRounded)
                    is PageComponent.ProductList -> PageComponent.ProductList(productList, currentCustomLabel.ifBlank { null }, isTransparent, isRounded)
                }
                onComponentUpdated(finalComp)
            }, onDeleteRequest)
        }
    }
}

@Composable
fun ProductEditCard(product: Product, onUpdate: (Product) -> Unit, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.LightGray), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Produto", style = MaterialTheme.typography.labelMedium)
                IconButton(onClick = onDelete, modifier = Modifier.size(20.dp)) { Icon(Icons.Default.Close, null, tint = Color.Red) }
            }
            OutlinedTextField(value = product.name, onValueChange = { onUpdate(product.copy(name = it)) }, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = product.price.toString(), onValueChange = { it.toDoubleOrNull()?.let { p -> onUpdate(product.copy(price = p)) } }, label = { Text("Preço") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = product.imageUrl, onValueChange = { onUpdate(product.copy(imageUrl = it)) }, label = { Text("URL Imagem") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp))
            }
        }
    }
}

@Composable
fun ActionButtons(isNew: Boolean, enabled: Boolean, onConfirm: () -> Unit, onCancel: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text(if (isNew) "Descartar" else "Remover", color = Color.Red) }
        Button(onClick = onConfirm, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), enabled = enabled) { Text(if (isNew) "Adicionar" else "Salvar") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentCatalogModal(
    onComponentSelected: (PageComponent) -> Unit, 
    onTemplateSelected: (List<PageComponent>) -> Unit,
    onDismiss: () -> Unit
) {
    // CORREÇÃO: Força o preenchimento total da tela no Catálogo
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 24.dp).navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Adicionar", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                TextButton(onClick = onDismiss) { Text("Fechar") }
            }
            Spacer(Modifier.height(16.dp))
            
            Text("Componentes", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            val catalogItems = listOf(
                Triple("Logo", PageComponent.Logo(""), Icons.Default.Store),
                Triple("Título", PageComponent.Header(""), Icons.Default.Title),
                Triple("Texto", PageComponent.Text(""), Icons.AutoMirrored.Filled.Notes),
                Triple("Imagem", PageComponent.Image("", ""), Icons.Default.Image),
                Triple("Lista de Produtos", PageComponent.ProductList(emptyList()), Icons.Default.ShoppingBag)
            )
            catalogItems.forEach { (name, component, icon) -> CatalogItemRow(name, icon) { onComponentSelected(component) } }

            Spacer(Modifier.height(24.dp))
            Text("Templates", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
            Surface(modifier = Modifier.fillMaxWidth().clickable { onTemplateSelected(listOf(PageComponent.Logo("", 80, isTransparent = true, isRounded = true), PageComponent.Header("Bem-vindo!", isTransparent = true), PageComponent.ProductList(listOf(Product("1", "Item", 99.0, "")), isTransparent = true))) }, shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Dashboard, null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.width(16.dp))
                    Text("Loja Completa", style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}

@Composable
fun CatalogItemRow(name: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(name, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
