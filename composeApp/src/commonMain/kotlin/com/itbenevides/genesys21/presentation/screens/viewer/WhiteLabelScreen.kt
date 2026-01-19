package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.domain.model.PageThemeConfig
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.theme.AppTheme
import com.itbenevides.genesys21.util.rememberImagePicker
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

    val savedCategories by viewModel.allAvailableCategories.collectAsState()
    
    val allCategories by remember(savedCategories, page) {
        derivedStateOf {
            val currentSessionCategories = page.components
                .filterIsInstance<PageComponent.ProductList>()
                .flatMap { it.products }
                .map { it.category }
                .filter { it.isNotBlank() }
            
            (savedCategories + currentSessionCategories).distinct().sorted()
        }
    }

    AppTheme(themeConfig = page.theme) {
        WhiteLabelContent(
            viewModel = viewModel,
            page = page,
            availableProducts = liveInventory,
            allAvailableCategories = allCategories, 
            isLoading = isLoading,
            onPageUpdate = onPageChange,
            onPublish = { viewModel.savePage(page, true) { onBack() } },
            onBack = onBack,
            onEditProduct = onEditProduct
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhiteLabelContent(
    viewModel: PageViewModel,
    page: Page,
    availableProducts: List<Product>,
    allAvailableCategories: List<String>, 
    isLoading: Boolean,
    onPageUpdate: (Page) -> Unit,
    onPublish: () -> Unit,
    onBack: () -> Unit,
    onEditProduct: (Product?, Int?) -> Unit
) {
    var showCatalog by remember { mutableStateOf(false) }
    var showThemeSelector by remember { mutableStateOf(false) }
    var showPageSettings by remember { mutableStateOf(false) } // NOVO ESTADO
    var editingComponentIndex by remember { mutableStateOf<Int?>(null) }
    var pendingNewComponent by remember { mutableStateOf<PageComponent?>(null) }
    
    var filterQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { showPageSettings = true }) {
                        Text(page.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Configurações", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            Icon(Icons.Default.Settings, null, modifier = Modifier.size(12.dp).padding(start = 2.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Voltar", color = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    IconButton(onClick = { showThemeSelector = true }) {
                        Icon(Icons.Default.Palette, "Temas", tint = MaterialTheme.colorScheme.primary)
                    }
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
                contentColor = MaterialTheme.colorScheme.onPrimary,
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
                            onEdit = { editingComponentIndex = index },
                            onMoveUp = if (index > 0) { {
                                val newList = page.components.toMutableList()
                                val temp = newList[index]
                                newList[index] = newList[index - 1]
                                newList[index - 1] = temp
                                onPageUpdate(page.copy(components = newList))
                            } } else null,
                            onMoveDown = if (index < page.components.size - 1) { {
                                val newList = page.components.toMutableList()
                                val temp = newList[index]
                                newList[index] = newList[index + 1]
                                newList[index + 1] = temp
                                onPageUpdate(page.copy(components = newList))
                            } } else null,
                            filterQuery = filterQuery,
                            onFilterQueryChange = { filterQuery = it },
                            onProductClick = { product -> onEditProduct(product, index) },
                            allAvailableCategories = allAvailableCategories 
                        )
                    }
                }
            }

            if (showPageSettings) {
                PageSettingsModal(
                    page = page,
                    onUpdate = { onPageUpdate(it) },
                    onDismiss = { showPageSettings = false }
                )
            }

            if (showThemeSelector) {
                ThemeSelectorModal(
                    currentTheme = page.theme,
                    onThemeSelected = { 
                        onPageUpdate(page.copy(theme = it))
                        showThemeSelector = false 
                    },
                    onDismiss = { showThemeSelector = false }
                )
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
                    viewModel = viewModel,
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
                        viewModel = viewModel,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageSettingsModal(
    page: Page,
    onUpdate: (Page) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var title by remember { mutableStateOf(page.title) }
    var customDomain by remember { mutableStateOf(page.customDomain ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(Modifier.fillMaxWidth().padding(24.dp).navigationBarsPadding()) {
            Text("Configurações da Página", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))
            
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título da Página") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(Modifier.height(16.dp))
            
            OutlinedTextField(
                value = customDomain,
                onValueChange = { customDomain = it },
                label = { Text("Domínio Customizado") },
                placeholder = { Text("ex: meusite.com") },
                modifier = Modifier.fillMaxWidth(),
                helperText = { Text("Aponte o DNS tipo A para 18.230.62.165", style = MaterialTheme.typography.labelSmall) }
            )
            
            Spacer(Modifier.height(32.dp))
            
            Button(
                onClick = { 
                    onUpdate(page.copy(title = title, customDomain = customDomain.ifBlank { null }))
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Salvar Configurações")
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditComponentModal(
    viewModel: PageViewModel,
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
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 24.dp).navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(if (isNew) "Configurar" else "Ajustar Bloco", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                TextButton(onClick = onDismiss) { Text("OK", fontWeight = FontWeight.Bold) }
            }
            
            Spacer(Modifier.height(16.dp))

            var currentCustomLabel by remember { mutableStateOf(component.customLabel ?: "") }
            var isTransparent by remember { mutableStateOf(component.isTransparent) }
            var isRounded by remember { mutableStateOf(component.isRounded) }
            var isFilterable by remember { mutableStateOf(component.isFilterable) }
            var headerTitle by remember { mutableStateOf(if (component is PageComponent.Header) component.title else "") }
            var textContent by remember { mutableStateOf(if (component is PageComponent.Text) component.content else "") }
            var imageUrl by remember { mutableStateOf(if (component is PageComponent.Image) component.url else "") }
            var imageDesc by remember { mutableStateOf(if (component is PageComponent.Image) component.string else "") }
            var imageSize by remember { mutableStateOf(if (component is PageComponent.Image) component.size.toString() else "200") }
            var btnText by remember { mutableStateOf(if (component is PageComponent.Button) component.text else "") }
            var btnUrl by remember { mutableStateOf(if (component is PageComponent.Button) component.url else "") }
            var btnIcon by remember { mutableStateOf(if (component is PageComponent.Button) component.iconName ?: "" else "") }
            var filterPlaceholder by remember { mutableStateOf(if (component is PageComponent.Filter) component.placeholder else "Filtrar conteúdo...") }

            var isUploading by remember { mutableStateOf(false) }
            val picker = rememberImagePicker { bytes ->
                bytes?.let {
                    isUploading = true
                    viewModel.uploadImage(it, "image_component.jpg") { url ->
                        imageUrl = url
                        isUploading = false
                    }
                }
            }

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                val previewComp = when (component) {
                    is PageComponent.Header -> PageComponent.Header(headerTitle, currentCustomLabel.ifBlank { null }, isTransparent, isRounded, isFilterable)
                    is PageComponent.Text -> PageComponent.Text(textContent, customLabel = currentCustomLabel.ifBlank { null }, isTransparent = isTransparent, isRounded = isRounded, isFilterable = isFilterable)
                    is PageComponent.Image -> PageComponent.Image(imageUrl, imageDesc, imageSize.toIntOrNull() ?: 200, currentCustomLabel.ifBlank { null }, isTransparent, isRounded, isFilterable)
                    is PageComponent.Button -> PageComponent.Button(btnText, btnUrl, btnIcon.ifBlank { null }, currentCustomLabel.ifBlank { null }, isTransparent, isRounded, isFilterable)
                    is PageComponent.Filter -> PageComponent.Filter(filterPlaceholder, currentCustomLabel.ifBlank { null }, isTransparent, isRounded, isFilterable)
                    is PageComponent.CategoryFilter -> PageComponent.CategoryFilter(currentCustomLabel.ifBlank { null }, isTransparent, isRounded, isFilterable)
                    else -> component
                }

                Box(Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
                    if (isUploading) CircularProgressIndicator()
                    else PageComponentRenderer(component = previewComp, onProductClick = {})
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(value = currentCustomLabel, onValueChange = { currentCustomLabel = it }, label = { Text("Nome de Identificação") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
                    Checkbox(checked = isTransparent, onCheckedChange = { isTransparent = it })
                    Text("Fundo Transparente", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.width(16.dp))
                    Checkbox(checked = isRounded, onCheckedChange = { isRounded = it })
                    Text("Redondo", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }

                when (component) {
                    is PageComponent.Header -> OutlinedTextField(value = headerTitle, onValueChange = { headerTitle = it }, label = { Text("Texto do Título") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                    is PageComponent.Text -> OutlinedTextField(value = textContent, onValueChange = { textContent = it }, label = { Text("Conteúdo") }, modifier = Modifier.fillMaxWidth(), minLines = 4, shape = RoundedCornerShape(10.dp))
                    is PageComponent.Image -> {
                        Button(onClick = { picker() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                            Icon(Icons.Default.CloudUpload, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Selecionar Foto Local")
                        }
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("URL da Imagem") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = imageDesc, onValueChange = { imageDesc = it }, label = { Text("Legenda") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = imageSize, onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) imageSize = it }, label = { Text("Tamanho (px)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                    }
                    is PageComponent.Button -> {
                        OutlinedTextField(value = btnText, onValueChange = { btnText = it }, label = { Text("Texto do Botão") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = btnUrl, onValueChange = { btnUrl = it }, label = { Text("URL de Destino") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = btnIcon, onValueChange = { btnIcon = it }, label = { Text("Nome do Ícone (opcional)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                    }
                    else -> {}
                }
                Spacer(Modifier.height(40.dp))
            }

            ActionButtons(isNew, true, {
                val finalComp = when (component) {
                    is PageComponent.Header -> PageComponent.Header(headerTitle, currentCustomLabel.ifBlank { null }, isTransparent, isRounded, isFilterable)
                    is PageComponent.Text -> PageComponent.Text(textContent, customLabel = currentCustomLabel.ifBlank { null }, isTransparent = isTransparent, isRounded = isRounded, isFilterable = isFilterable)
                    is PageComponent.Image -> PageComponent.Image(imageUrl, imageDesc, imageSize.toIntOrNull() ?: 200, currentCustomLabel.ifBlank { null }, isTransparent, isRounded, isFilterable)
                    is PageComponent.Button -> PageComponent.Button(btnText, btnUrl, btnIcon.ifBlank { null }, currentCustomLabel.ifBlank { null }, isTransparent, isRounded, isFilterable)
                    is PageComponent.Filter -> PageComponent.Filter(filterPlaceholder, currentCustomLabel.ifBlank { null }, isTransparent, isRounded, isFilterable)
                    is PageComponent.CategoryFilter -> PageComponent.CategoryFilter(currentCustomLabel.ifBlank { null }, isTransparent, isRounded, isFilterable)
                    else -> component
                }
                onComponentUpdated(finalComp)
            }, onDeleteRequest)
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
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 24.dp).navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Gerenciar Produtos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                TextButton(onClick = onDismiss) { Text("OK", fontWeight = FontWeight.Bold) }
            }
            
            Spacer(Modifier.height(16.dp))

            var currentCustomLabel by remember { mutableStateOf(component.customLabel ?: "") }
            var isTransparent by remember { mutableStateOf(component.isTransparent) }
            var isRounded by remember { mutableStateOf(component.isRounded) }
            var isHorizontal by remember { mutableStateOf(component.isHorizontal) }
            var isFilterable by remember { mutableStateOf(component.isFilterable) }

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
                    Text("Transparente", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.width(16.dp))
                    Checkbox(checked = isRounded, onCheckedChange = { 
                        isRounded = it
                        onComponentUpdated(component.copy(isRounded = it))
                    })
                    Text("Redondo", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                    Checkbox(checked = isHorizontal, onCheckedChange = { 
                        isHorizontal = it
                        onComponentUpdated(component.copy(isHorizontal = it))
                    })
                    Text("Exibir Horizontalmente (Carrossel)", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                    Checkbox(checked = isFilterable, onCheckedChange = { 
                        isFilterable = it
                        onComponentUpdated(component.copy(isFilterable = it))
                    })
                    Text("Permitir Filtragem", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                
                if (availableProducts.isNotEmpty()) {
                    val filteredSuggestions = availableProducts.filter { p -> component.products.none { it.id == p.id } }
                    if (filteredSuggestions.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Text("Aproveitar do Inventário", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
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
                                        if (product.imageUrl.isNotEmpty()) {
                                            AsyncImage(
                                                model = product.imageUrl,
                                                contentDescription = null,
                                                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Icon(Icons.Default.ShoppingBag, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                                        }
                                        Text(product.name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                                        Text("R$ ${product.price}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                Text("Produtos nesta Lista", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))

                component.products.forEach { product ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onEditProduct(product) },
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (product.imageUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = product.imageUrl,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.ShoppingBag, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), modifier = Modifier.size(40.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(product.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
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
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
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
fun ThemeSelectorModal(
    currentTheme: PageThemeConfig,
    onThemeSelected: (PageThemeConfig) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 24.dp).navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Cores Harmonizadas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                TextButton(onClick = onDismiss) { Text("OK", fontWeight = FontWeight.Bold) }
            }
            
            Spacer(Modifier.height(20.dp))
            
            val themes = listOf(
                Triple(PageThemeConfig.DEFAULT, "Royal", Color(0xFF2D3142)),
                Triple(PageThemeConfig.OCEAN, "Soft Ocean", Color(0xFF4A90E2)),
                Triple(PageThemeConfig.FOREST, "Eco Forest", Color(0xFF386641)),
                Triple(PageThemeConfig.CANDY, "Rose Gold", Color(0xFFB08968)),
                Triple(PageThemeConfig.DARK, "Midnight", Color(0xFFE0E1DD)),
                Triple(PageThemeConfig.SUNSET, "Sunset", Color(0xFFF4A261)),
                Triple(PageThemeConfig.BERRY, "Berry", Color(0xFF9D0208)),
                Triple(PageThemeConfig.MINIMAL, "Minimal", Color(0xFF000000)),
                Triple(PageThemeConfig.VINTAGE, "Vintage", Color(0xFF6D597A)),
                Triple(PageThemeConfig.NEON, "Neon", Color(0xFF39FF14))
            )
            
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                themes.forEach { (config, label, color) ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { onThemeSelected(config) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (currentTheme == config) color.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        border = if (currentTheme == config) androidx.compose.foundation.BorderStroke(2.dp, color) else null
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(24.dp).clip(CircleShape).background(color))
                            Spacer(Modifier.width(16.dp))
                            Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = if (currentTheme == config) FontWeight.Bold else FontWeight.Normal, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.weight(1f))
                            if (currentTheme == config) Icon(Icons.Default.Check, null, tint = color)
                        }
                    }
                }
                Spacer(Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun ActionButtons(isNew: Boolean, enabled: Boolean, onConfirm: () -> Unit, onCancel: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text(if (isNew) "Descartar" else "Remover", color = MaterialTheme.colorScheme.error) }
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
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 24.dp).navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Adicionar", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                TextButton(onClick = onDismiss) { Text("Fechar") }
            }
            Spacer(Modifier.height(16.dp))
            
            Text("Componentes", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            val catalogItems = listOf(
                Triple("Barra de Busca", PageComponent.Filter(), Icons.Default.Search),
                Triple("Filtro de Categorias", PageComponent.CategoryFilter(), Icons.Default.Category),
                Triple("Título", PageComponent.Header(""), Icons.Default.Title),
                Triple("Texto", PageComponent.Text(""), Icons.AutoMirrored.Filled.Notes),
                Triple("Imagem", PageComponent.Image("", ""), Icons.Default.Image),
                Triple("Botão de Link", PageComponent.Button("", ""), Icons.Default.SmartButton),
                Triple("Lista de Produtos", PageComponent.ProductList(emptyList()), Icons.Default.ShoppingBag)
            )
            catalogItems.forEach { (name, component, icon) -> CatalogItemRow(name, icon) { onComponentSelected(component) } }

            Spacer(Modifier.height(24.dp))
            Text("Templates", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
            
            val mockCarouselProducts = (1..10).map { 
                Product(id = "c$it", name = "Produto Carrossel $it", price = 10.0 * it, imageUrl = "", category = "template") 
            }
            val mockListProducts = (1..10).map { 
                Product(id = "l$it", name = "Produto Lista $it", price = 15.0 * it, imageUrl = "", category = "template") 
            }

            Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { 
                    onTemplateSelected(listOf(
                        PageComponent.Image("", "", 80, isTransparent = true, isRounded = true), 
                        PageComponent.Header("Bem-vindo à Loja!", isTransparent = true), 
                        PageComponent.Filter("Buscar na loja...", isRounded = true),
                        PageComponent.CategoryFilter(isRounded = true),
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
                    Text("Loja Completa", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { 
                    onTemplateSelected(listOf(
                        PageComponent.Image("", "", 120, isTransparent = true, isRounded = true, customDomain = null, customLabel = "Foto de Perfil"), 
                        PageComponent.Header("Seu Nome Aqui", isTransparent = true), 
                        PageComponent.Text("Sua biografia curta e inspiradora aparece aqui.", isTransparent = true, isRounded = false),
                        PageComponent.Button("WhatsApp", "https://wa.me/seunumeroaqui", "whatsapp", isTransparent = false),
                        PageComponent.Button("Instagram", "https://instagram.com/seuuser", "instagram", isTransparent = false),
                        PageComponent.Button("Email", "mailto:seuemail@exemplo.com", "email", isTransparent = false),
                        PageComponent.Button("Portfólio", "https://seusite.com", "web", isTransparent = true)
                    )) 
                }, 
                shape = RoundedCornerShape(12.dp), 
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.tertiary)
                    Spacer(Modifier.width(16.dp))
                    Text("Perfil Profissional (Link in Bio)", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
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
            Text(name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
