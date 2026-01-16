package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.presentation.PageViewModel
import kotlin.random.Random

@Composable
fun WhiteLabelScreen(
    viewModel: PageViewModel,
    page: Page,
    onPageChange: (Page) -> Unit,
    onBack: () -> Unit,
    onEditProduct: (Product?, Int?) -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val serverProducts by viewModel.allAvailableProducts.collectAsState()

    val liveInventory by remember(serverProducts, page) {
        derivedStateOf {
            val sessionProducts = page.components
                .filterIsInstance<PageComponent.ProductList>()
                .flatMap { it.products }
            (serverProducts + sessionProducts).distinctBy { it.id }
        }
    }

    WhiteLabelContent(
        page = page,
        availableProducts = liveInventory,
        isLoading = isLoading,
        onPageUpdate = onPageChange,
        onPublish = { viewModel.savePage(page, true) { onBack() } },
        onBack = onBack,
        onEditProduct = onEditProduct
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhiteLabelContent(
    page: Page,
    availableProducts: List<Product>,
    isLoading: Boolean,
    onPageUpdate: (Page) -> Unit,
    onPublish: () -> Unit,
    onBack: () -> Unit,
    onEditProduct: (Product?, Int?) -> Unit
) {
    var showCatalog by remember { mutableStateOf(false) }
    var editingComponentIndex by remember { mutableStateOf<Int?>(null) }
    var pendingNewComponent by remember { mutableStateOf<PageComponent?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(page.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Editor White Label", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Voltar", color = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    TextButton(onClick = onPublish, enabled = !isLoading) {
                        if (isLoading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        else Text("Publicar", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
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
            if (page.components.isEmpty()) {
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
                    contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    itemsIndexed(page.components) { index, component ->
                        ComponentWrapper(
                            component = component,
                            onDelete = {
                                val newList = page.components.toMutableList().apply { removeAt(index) }
                                onPageUpdate(page.copy(components = newList))
                            },
                            onEdit = { editingComponentIndex = index }
                        )
                    }
                }
            }

            if (showCatalog) {
                ComponentCatalogModal(
                    onComponentSelected = { newComponent ->
                        if (newComponent is PageComponent.ProductList) {
                            val newList = page.components + newComponent
                            onPageUpdate(page.copy(components = newList))
                            editingComponentIndex = newList.size - 1
                        } else {
                            pendingNewComponent = newComponent
                        }
                        showCatalog = false
                    },
                    onTemplateSelected = {
                        onPageUpdate(page.copy(components = page.components + it))
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
                        onPageUpdate(page.copy(components = page.components + updated))
                        pendingNewComponent = null
                    },
                    onDeleteRequest = { pendingNewComponent = null },
                    onDismiss = { pendingNewComponent = null }
                )
            }

            editingComponentIndex?.let { index ->
                val component = page.components[index]
                if (component is PageComponent.ProductList) {
                    ProductListManagementModal(
                        component = component,
                        availableProducts = availableProducts,
                        onAddProduct = { onEditProduct(null, index) },
                        onEditProduct = { onEditProduct(it, index) },
                        onComponentUpdated = { updated ->
                            val newList = page.components.toMutableList().apply { set(index, updated) }
                            onPageUpdate(page.copy(components = newList))
                        },
                        onDeleteComponent = {
                            val newList = page.components.toMutableList().apply { removeAt(index) }
                            onPageUpdate(page.copy(components = newList))
                            editingComponentIndex = null
                        },
                        onDismiss = { editingComponentIndex = null }
                    )
                } else {
                    EditComponentModal(
                        component = component,
                        isNew = false,
                        onComponentUpdated = { updated ->
                            val newList = page.components.toMutableList().apply { set(index, updated) }
                            onPageUpdate(page.copy(components = newList))
                            editingComponentIndex = null
                        },
                        onDeleteRequest = {
                            val newList = page.components.toMutableList().apply { removeAt(index) }
                            onPageUpdate(page.copy(components = newList))
                            editingComponentIndex = null
                        },
                        onDismiss = { editingComponentIndex = null }
                    )
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
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        // TOOLBAR EXTERNA
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp, start = 4.dp, end = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = (component.customLabel ?: component::class.simpleName ?: "Bloco").uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Row {
                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                }
            }
        }

        // CARD DO DESIGN
        val isImage = component is PageComponent.Image
        val shape = if (component.isRounded && component !is PageComponent.ProductList) CircleShape else RoundedCornerShape(12.dp)
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = shape,
            color = if (component.isTransparent || isImage) Color.Transparent else MaterialTheme.colorScheme.surface,
            border = if (component.isTransparent || isImage) null else androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            shadowElevation = if (component.isTransparent || isImage) 0.dp else 1.dp
        ) {
            Box(Modifier.padding(if (component.isTransparent || isImage) 0.dp else 16.dp)) {
                PageComponentRenderer(component)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListManagementModal(
    component: PageComponent.ProductList,
    availableProducts: List<Product>,
    onAddProduct: () -> Unit,
    onEditProduct: (Product) -> Unit,
    onComponentUpdated: (PageComponent.ProductList) -> Unit,
    onDeleteComponent: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 24.dp).navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Gerenciar Produtos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                TextButton(onClick = onDismiss) { Text("OK", fontWeight = FontWeight.Bold) }
            }
            
            Spacer(Modifier.height(16.dp))

            var currentCustomLabel by remember { mutableStateOf(component.customLabel ?: "") }
            var isTransparent by remember { mutableStateOf(component.isTransparent) }
            var isRounded by remember { mutableStateOf(component.isRounded) }
            var isHorizontal by remember { mutableStateOf(component.isHorizontal) }

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = currentCustomLabel, 
                    onValueChange = { 
                        currentCustomLabel = it
                        onComponentUpdated(component.copy(customLabel = it.ifBlank { null }))
                    }, 
                    label = { Text("Nome do Bloco") }, 
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(10.dp)
                )
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
                    Checkbox(checked = isTransparent, onCheckedChange = { 
                        isTransparent = it
                        onComponentUpdated(component.copy(isTransparent = it))
                    })
                    Text("Transparente", fontSize = 14.sp)
                    Spacer(Modifier.width(16.dp))
                    Checkbox(checked = isRounded, onCheckedChange = { 
                        isRounded = it
                        onComponentUpdated(component.copy(isRounded = it))
                    })
                    Text("Redondo", fontSize = 14.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                    Checkbox(checked = isHorizontal, onCheckedChange = { 
                        isHorizontal = it
                        onComponentUpdated(component.copy(isHorizontal = it))
                    })
                    Text("Exibir Horizontalmente (Carrossel)", fontSize = 14.sp)
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                
                if (availableProducts.isNotEmpty()) {
                    val filteredSuggestions = availableProducts.filter { p -> component.products.none { it.id == p.id } }
                    if (filteredSuggestions.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Text("Aproveitar do Inventário", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(filteredSuggestions) { product ->
                                Surface(
                                    modifier = Modifier
                                        .width(140.dp)
                                        .clickable { onComponentUpdated(component.copy(products = component.products + product)) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.5f))
                                ) {
                                    Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.ShoppingBag, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                                        Text(product.name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall)
                                        Text("R$ ${product.price}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                Text("Produtos nesta Lista", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                component.products.forEach { product ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onEditProduct(product) },
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.5f)),
                        color = Color.White
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ShoppingBag, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), modifier = Modifier.size(40.dp))
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(product.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                Text("R$ ${product.price}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = {
                                onComponentUpdated(component.copy(products = component.products.filter { it.id != product.id }))
                            }) {
                                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onAddProduct,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Cadastrar Novo Produto")
                }
                
                Spacer(Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onDeleteComponent,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                ) {
                    Text("Remover Todo o Bloco")
                }
                Spacer(Modifier.height(32.dp))
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
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(if (isNew) "Configurar" else "Ajustar Bloco", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                TextButton(onClick = onDismiss) { Text("OK", fontWeight = FontWeight.Bold) }
            }
            
            Spacer(Modifier.height(16.dp))

            var currentCustomLabel by remember { mutableStateOf(component.customLabel ?: "") }
            var isTransparent by remember { mutableStateOf(component.isTransparent) }
            var isRounded by remember { mutableStateOf(component.isRounded) }
            var headerTitle by remember { mutableStateOf(if (component is PageComponent.Header) component.title else "") }
            var textContent by remember { mutableStateOf(if (component is PageComponent.Text) component.content else "") }
            var imageUrl by remember { mutableStateOf(if (component is PageComponent.Image) component.url else "") }
            var imageDesc by remember { mutableStateOf(if (component is PageComponent.Image) component.string else "") }
            var imageSize by remember { mutableStateOf(if (component is PageComponent.Image) component.size.toString() else "200") }

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                val previewComp = when (component) {
                    is PageComponent.Header -> PageComponent.Header(headerTitle, currentCustomLabel.ifBlank { null }, isTransparent, isRounded)
                    is PageComponent.Text -> PageComponent.Text(textContent, customLabel = currentCustomLabel.ifBlank { null }, isTransparent = isTransparent, isRounded = isRounded)
                    is PageComponent.Image -> PageComponent.Image(imageUrl, imageDesc, imageSize.toIntOrNull() ?: 200, currentCustomLabel.ifBlank { null }, isTransparent, isRounded)
                    else -> component
                }

                Box(Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
                    PageComponentRenderer(previewComp)
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(value = currentCustomLabel, onValueChange = { currentCustomLabel = it }, label = { Text("Nome de Identificação") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
                    Checkbox(checked = isTransparent, onCheckedChange = { isTransparent = it })
                    Text("Fundo Transparente", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.width(16.dp))
                    Checkbox(checked = isRounded, onCheckedChange = { isRounded = it })
                    Text("Redondo", style = MaterialTheme.typography.bodyMedium)
                }

                when (component) {
                    is PageComponent.Header -> OutlinedTextField(value = headerTitle, onValueChange = { headerTitle = it }, label = { Text("Texto do Título") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                    is PageComponent.Text -> OutlinedTextField(value = textContent, onValueChange = { textContent = it }, label = { Text("Conteúdo") }, modifier = Modifier.fillMaxWidth(), minLines = 4, shape = RoundedCornerShape(10.dp))
                    is PageComponent.Image -> {
                        OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("URL da Imagem") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = imageDesc, onValueChange = { imageDesc = it }, label = { Text("Legenda") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = imageSize, onValueChange = { 
                            // Sanitização: Aceita apenas números para o tamanho da imagem
                            if (it.isEmpty() || it.all { c -> c.isDigit() }) {
                                imageSize = it 
                            }
                        }, label = { Text("Tamanho (px)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                    }
                    else -> {}
                }
                Spacer(Modifier.height(40.dp))
            }

            val isDataValid = when (component) {
                is PageComponent.Header -> headerTitle.isNotBlank()
                is PageComponent.Text -> textContent.isNotBlank()
                is PageComponent.Image -> imageUrl.isNotBlank()
                else -> true
            }

            ActionButtons(isNew, isDataValid, {
                val finalComp = when (component) {
                    is PageComponent.Header -> PageComponent.Header(headerTitle, currentCustomLabel.ifBlank { null }, isTransparent, isRounded)
                    is PageComponent.Text -> PageComponent.Text(textContent, customLabel = currentCustomLabel.ifBlank { null }, isTransparent = isTransparent, isRounded = isRounded)
                    is PageComponent.Image -> PageComponent.Image(imageUrl, imageDesc, imageSize.toIntOrNull() ?: 200, currentCustomLabel.ifBlank { null }, isTransparent, isRounded)
                    else -> component
                }
                onComponentUpdated(finalComp)
            }, onDeleteRequest)
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
                Triple("Título", PageComponent.Header(""), Icons.Default.Title),
                Triple("Texto", PageComponent.Text(""), Icons.AutoMirrored.Filled.Notes),
                Triple("Imagem", PageComponent.Image("", ""), Icons.Default.Image),
                Triple("Lista de Produtos", PageComponent.ProductList(emptyList()), Icons.Default.ShoppingBag)
            )
            catalogItems.forEach { (name, component, icon) -> CatalogItemRow(name, icon) { onComponentSelected(component) } }

            Spacer(Modifier.height(24.dp))
            Text("Templates", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
            
            val mockCarouselProducts = (1..10).map { 
                Product(id = "c$it", name = "Produto Carrossel $it", price = 10.0 * it, imageUrl = "") 
            }
            val mockListProducts = (1..10).map { 
                Product(id = "l$it", name = "Produto Lista $it", price = 15.0 * it, imageUrl = "") 
            }

            Surface(
                modifier = Modifier.fillMaxWidth().clickable { 
                    onTemplateSelected(listOf(
                        PageComponent.Image("", "", 80, isTransparent = true, isRounded = true), 
                        PageComponent.Header("Bem-vindo à Loja!", isTransparent = true), 
                        PageComponent.ProductList(mockCarouselProducts, isHorizontal = true, customLabel = "Destaques", isTransparent = true),
                        PageComponent.ProductList(mockListProducts, isHorizontal = false, customLabel = "Nossos Produtos", isTransparent = true)
                    )) 
                }, 
                shape = RoundedCornerShape(12.dp), 
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
            ) {
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
