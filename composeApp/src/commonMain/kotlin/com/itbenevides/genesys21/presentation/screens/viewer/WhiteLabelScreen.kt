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
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
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
import com.itbenevides.genesys21.navigation.Route
import com.itbenevides.genesys21.navigation.Router
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.theme.AppTheme
import com.itbenevides.genesys21.di.getBaseUrl
import com.itbenevides.genesys21.util.rememberImagePicker
import org.koin.compose.koinInject

@Composable
fun WhiteLabelScreen(
    viewModel: PageViewModel,
    page: Page,
    onPageChange: (Page) -> Unit,
    onBack: () -> Unit,
    onEditProduct: (Product?, Int?) -> Unit
) {
    val isLoading: Boolean by viewModel.isLoading.collectAsState()
    val serverProducts: List<Product> by viewModel.allAvailableProducts.collectAsState()

    val liveInventory: List<Product> by remember(serverProducts, page) {
        derivedStateOf {
            val sessionProducts = page.components
                .filterIsInstance<PageComponent.ProductList>()
                .flatMap { it.products }
            (serverProducts + sessionProducts).distinctBy { it.id }
        }
    }

    val savedCategories: List<String> by viewModel.allAvailableCategories.collectAsState(initial = emptyList())
    
    val allCategoriesList: List<String> by remember(savedCategories, page) {
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
            allAvailableCategories = allCategoriesList, 
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
    val router: Router = koinInject()
    var showCatalog by remember { mutableStateOf(false) }
    var showThemeSelector by remember { mutableStateOf(false) }
    var showPageSettings by remember { mutableStateOf(false) }
    var editingComponentIndex by remember { mutableStateOf<Int?>(null) }
    var pendingNewComponent by remember { mutableStateOf<PageComponent?>(null) }
    
    var filterQuery by remember { mutableStateOf("") }
    val userPages by viewModel.pages.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { showPageSettings = true }) {
                        Text(page.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Ajustes da Página", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            Icon(Icons.Default.ExpandMore, null, modifier = Modifier.size(12.dp).padding(start = 2.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Voltar", color = MaterialTheme.colorScheme.primary, fontSize = 17.sp)
                    }
                },
                actions = {
                    IconButton(onClick = { router.navigateTo(Route.PageList) }) {
                        Icon(Icons.Default.Settings, "Administração", tint = MaterialTheme.colorScheme.primary)
                    }

                    IconButton(onClick = { showThemeSelector = true }) {
                        Icon(Icons.Default.Palette, "Temas", tint = MaterialTheme.colorScheme.primary)
                    }
                    TextButton(onClick = onPublish, enabled = !isLoading) {
                        if (isLoading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        else Text("Publicar", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 17.sp)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            if (!isLoading) {
                FloatingActionButton(
                    onClick = { showCatalog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, "Adicionar Bloco")
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 1000.dp)
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
            ) {
                if (page.components.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyEditorState { showCatalog = true }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        itemsIndexed(page.components) { index, component ->
                            ComponentWrapper(
                                component = component,
                                onDelete = {
                                    val newList = page.components.toMutableList().apply { removeAt(index) }
                                    onPageUpdate(page.copy(components = newList))
                                },
                                onDuplicate = {
                                    val newList = page.components.toMutableList().apply { add(index + 1, component) }
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
                                onFilterQueryChange = { query: String -> filterQuery = query },
                                onProductClick = { product: Product -> 
                                    onEditProduct(product, index) 
                                },
                                allAvailableCategories = allAvailableCategories 
                            )
                        }
                    }
                }
            }

            if (showPageSettings) {
                PageSettingsModal(
                    page = page,
                    onUpdate = { updatedPage: Page -> onPageUpdate(updatedPage) },
                    onDismiss = { showPageSettings = false }
                )
            }

            if (showThemeSelector) {
                ThemeSelectorModal(
                    currentTheme = page.theme,
                    onThemeSelected = { config: PageThemeConfig -> 
                        onPageUpdate(page.copy(theme = config))
                        showThemeSelector = false 
                    },
                    onDismiss = { showThemeSelector = false }
                )
            }

            if (showCatalog) {
                ComponentCatalogModal(
                    onComponentSelected = { newComponent: PageComponent ->
                        if (newComponent is PageComponent.ProductList) {
                            onPageUpdate(page.copy(components = page.components + newComponent))
                        } else {
                            pendingNewComponent = newComponent
                        }
                        showCatalog = false
                    },
                    onTemplateSelected = { components: List<PageComponent> ->
                        onPageUpdate(page.copy(components = page.components + components))
                        showCatalog = false
                    },
                    onDismiss = { showCatalog = false }
                )
            }

            pendingNewComponent?.let { component ->
                EditComponentModal(
                    viewModel = viewModel,
                    userPages = userPages,
                    component = component,
                    isNew = true,
                    onComponentUpdated = { updated: PageComponent ->
                        onPageUpdate(page.copy(components = page.components + updated))
                        pendingNewComponent = null
                    },
                    onDeleteRequest = { pendingNewComponent = null },
                    onDismiss = { pendingNewComponent = null }
                )
            }

            editingComponentIndex?.let { index ->
                if (index < page.components.size) {
                    val component = page.components[index]
                    if (component is PageComponent.ProductList) {
                        ProductListManagementModal(
                            component = component,
                            availableProducts = availableProducts,
                            onAddProduct = { onEditProduct(null, index) },
                            onEditProduct = { product: Product -> onEditProduct(product, index) },
                            onComponentUpdated = { updated: PageComponent.ProductList ->
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
                            userPages = userPages,
                            component = component,
                            isNew = false,
                            onComponentUpdated = { updated: PageComponent ->
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
                } else {
                    editingComponentIndex = null
                }
            }
        }
    }
}

@Composable
fun EmptyEditorState(onAddClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        Spacer(Modifier.height(24.dp))
        Text(
            "Sua vitrine está vazia",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            "Adicione blocos de título, imagens ou listas de produtos para começar a vender.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(Modifier.height(32.dp))
        Button(onClick = onAddClick, shape = RoundedCornerShape(12.dp)) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("Adicionar Primeiro Bloco")
        }
    }
}

@Composable
fun ComponentWrapper(
    component: PageComponent,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onEdit: () -> Unit,
    onMoveUp: (() -> Unit)? = null,
    onMoveDown: (() -> Unit)? = null,
    filterQuery: String = "",
    onFilterQueryChange: (String) -> Unit = {},
    onProductClick: (Product) -> Unit,
    allAvailableCategories: List<String> = emptyList() 
) {
    var showConfirmDelete by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, start = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = (component.customLabel ?: component::class.simpleName ?: "Bloco").uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                
                IconButton(onClick = onEdit, modifier = Modifier.padding(start = 8.dp).size(32.dp)) {
                    Icon(Icons.Default.Edit, "Configurações do Bloco", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.secondary)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onMoveUp != null) {
                    IconButton(onClick = onMoveUp, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ArrowUpward, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }
                }
                if (onMoveDown != null) {
                    IconButton(onClick = onMoveDown, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ArrowDownward, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = onDuplicate, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.ContentCopy, "Duplicar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = { showConfirmDelete = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.DeleteOutline, "Excluir", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.Transparent,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
        ) {
            Box(Modifier.padding(8.dp)) {
                PageComponentRenderer(
                    component = component,
                    filterQuery = filterQuery,
                    onFilterQueryChange = onFilterQueryChange,
                    onProductClick = onProductClick,
                    allAvailableCategories = allAvailableCategories,
                    isEditMode = true,
                    onEditClick = onEdit 
                )
            }
        }
    }

    if (showConfirmDelete) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            title = { Text("Excluir Bloco?") },
            text = { Text("Você tem certeza que deseja remover este conteúdo da sua página?") },
            confirmButton = { 
                TextButton(onClick = { onDelete(); showConfirmDelete = false }) { 
                    Text("Excluir", color = MaterialTheme.colorScheme.error) 
                } 
            },
            dismissButton = { 
                TextButton(onClick = { showConfirmDelete = false }) { Text("Cancelar") } 
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditComponentModal(
    viewModel: PageViewModel,
    userPages: List<Page>,
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
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 24.dp).navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(if (isNew) "Configurar" else "Ajustar Bloco", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                TextButton(onClick = onDismiss) { Text("Concluir", fontWeight = FontWeight.Bold) }
            }
            
            Spacer(Modifier.height(16.dp))

            var currentCustomLabel by remember { mutableStateOf(component.customLabel ?: "") }
            var isFilterable by remember { mutableStateOf(component.isFilterable) }
            var headerTitle by remember { mutableStateOf(if (component is PageComponent.Header) component.title else "") }
            var headerFontSize by remember { mutableIntStateOf(if (component is PageComponent.Header) component.fontSize else 28) }
            var headerAlign by remember { mutableStateOf(if (component is PageComponent.Header) component.textAlign else "LEFT") }
            
            var textContent by remember { mutableStateOf(if (component is PageComponent.Text) component.content else "") }
            var textFontSize by remember { mutableIntStateOf(if (component is PageComponent.Text) component.fontSize else 16) }
            var textAlign by remember { mutableStateOf(if (component is PageComponent.Text) component.textAlign else "LEFT") }

            var imageUrl by remember { mutableStateOf(if (component is PageComponent.Image) component.url else "") }
            var imageDesc by remember { mutableStateOf(if (component is PageComponent.Image) component.string else "") }
            var imageSize by remember { mutableIntStateOf(if (component is PageComponent.Image) component.size else 200) }
            var isRounded by remember { mutableStateOf(if (component is PageComponent.Image) component.isRounded else false) }
            var destPageId by remember { mutableStateOf(if (component is PageComponent.Image) component.destinationPageId ?: "" else "") }
            var isFullWidth by remember { mutableStateOf(if (component is PageComponent.Image) component.isFullWidth else false) }
            
            var btnText by remember { mutableStateOf(if (component is PageComponent.Button) component.text else "") }
            var btnUrl by remember { mutableStateOf(if (component is PageComponent.Button) component.url else "") }
            var btnIcon by remember { mutableStateOf(if (component is PageComponent.Button) component.iconName ?: "" else "") }
            
            var filterPlaceholder by remember { mutableStateOf(if (component is PageComponent.Filter) component.placeholder else "Filtrar conteúdo...") }

            var isUploading by remember { mutableStateOf(false) }
            val picker = rememberImagePicker { bytes: ByteArray? ->
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
                    is PageComponent.Header -> PageComponent.Header(headerTitle, headerFontSize, headerAlign, currentCustomLabel.ifBlank { null }, isFilterable)
                    is PageComponent.Text -> PageComponent.Text(textContent, textFontSize, textAlign, currentCustomLabel.ifBlank { null }, isFilterable)
                    is PageComponent.Image -> PageComponent.Image(imageUrl, imageDesc, imageSize, destPageId.ifBlank { null }, isFullWidth, isRounded, currentCustomLabel.ifBlank { null }, isFilterable)
                    is PageComponent.Button -> PageComponent.Button(btnText, btnUrl, btnIcon.ifBlank { null }, currentCustomLabel.ifBlank { null }, isFilterable)
                    is PageComponent.Filter -> PageComponent.Filter(filterPlaceholder, currentCustomLabel.ifBlank { null }, isFilterable)
                    is PageComponent.CategoryFilter -> PageComponent.CategoryFilter(currentCustomLabel.ifBlank { null }, isFilterable)
                    else -> component
                }

                Box(Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
                    if (isUploading) CircularProgressIndicator()
                    else PageComponentRenderer(component = previewComp, onProductClick = {})
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(value = currentCustomLabel, onValueChange = { currentCustomLabel = it }, label = { Text("Nome de Identificação (Interno)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                
                when (component) {
                    is PageComponent.Header -> {
                        OutlinedTextField(value = headerTitle, onValueChange = { headerTitle = it }, label = { Text("Texto do Título") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("Tamanho da Fonte: $headerFontSize", style = MaterialTheme.typography.labelMedium)
                        Slider(value = headerFontSize.toFloat(), onValueChange = { headerFontSize = it.toInt() }, valueRange = 12f..64f)
                        TextAlignSelector(headerAlign) { headerAlign = it }
                    }
                    is PageComponent.Text -> {
                        OutlinedTextField(value = textContent, onValueChange = { textContent = it }, label = { Text("Conteúdo") }, modifier = Modifier.fillMaxWidth(), minLines = 4, shape = RoundedCornerShape(10.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("Tamanho da Fonte: $textFontSize", style = MaterialTheme.typography.labelMedium)
                        Slider(value = textFontSize.toFloat(), onValueChange = { textFontSize = it.toInt() }, valueRange = 8f..32f)
                        TextAlignSelector(textAlign) { textAlign = it }
                    }
                    is PageComponent.Image -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isFullWidth, onCheckedChange = { isFullWidth = it })
                            Text("Largura Total", fontSize = 14.sp)
                            Spacer(Modifier.width(16.dp))
                            Checkbox(checked = isRounded, onCheckedChange = { isRounded = it })
                            Text("Redondo", fontSize = 14.sp)
                        }
                        
                        Button(onClick = { picker() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                            Icon(Icons.Default.CloudUpload, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Mudar Foto")
                        }
                        
                        Spacer(Modifier.height(12.dp))
                        
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            OutlinedTextField(
                                value = userPages.find { it.id == destPageId }?.title ?: "Nenhuma (Apenas Foto)",
                                onValueChange = {},
                                label = { Text("Ao Clicar, abrir página:") },
                                modifier = Modifier.fillMaxWidth(),
                                readOnly = true,
                                trailingIcon = { IconButton(onClick = { expanded = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                                shape = RoundedCornerShape(10.dp)
                            )
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                DropdownMenuItem(text = { Text("Nenhuma (Apenas Foto)") }, onClick = { destPageId = ""; expanded = false })
                                userPages.forEach { p ->
                                    DropdownMenuItem(text = { Text(p.title) }, onClick = { destPageId = p.id; expanded = false })
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(8.dp))
                        Text("Tamanho da Imagem: ${imageSize}px", style = MaterialTheme.typography.labelMedium)
                        Slider(value = imageSize.toFloat(), onValueChange = { imageSize = it.toInt() }, valueRange = 50f..1000f)
                    }
                    is PageComponent.Button -> {
                        OutlinedTextField(value = btnText, onValueChange = { btnText = it }, label = { Text("Texto do Botão") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = btnUrl, onValueChange = { btnUrl = it }, label = { Text("Link de Destino") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                    }
                    else -> {}
                }
                Spacer(Modifier.height(40.dp))
            }

            ActionButtons(isNew, true, {
                val finalComp = when (component) {
                    is PageComponent.Header -> PageComponent.Header(headerTitle, headerFontSize, headerAlign, currentCustomLabel.ifBlank { null }, isFilterable)
                    is PageComponent.Text -> PageComponent.Text(textContent, textFontSize, textAlign, currentCustomLabel.ifBlank { null }, isFilterable)
                    is PageComponent.Image -> PageComponent.Image(imageUrl, imageDesc, imageSize, destPageId.ifBlank { null }, isFullWidth, isRounded, currentCustomLabel.ifBlank { null }, isFilterable)
                    is PageComponent.Button -> PageComponent.Button(btnText, btnUrl, btnIcon.ifBlank { null }, currentCustomLabel.ifBlank { null }, isFilterable)
                    is PageComponent.Filter -> PageComponent.Filter(filterPlaceholder, currentCustomLabel.ifBlank { null }, isFilterable)
                    is PageComponent.CategoryFilter -> PageComponent.CategoryFilter(currentCustomLabel.ifBlank { null }, isFilterable)
                    else -> component
                }
                onComponentUpdated(finalComp)
            }, onDeleteRequest)
        }
    }
}

@Composable
fun TextAlignSelector(current: String, onSelected: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
        val options = listOf("LEFT" to Icons.AutoMirrored.Filled.FormatAlignLeft, "CENTER" to Icons.Default.FormatAlignCenter, "RIGHT" to Icons.AutoMirrored.Filled.FormatAlignRight)
        options.forEach { (valStr, icon) ->
            FilterChip(
                selected = current == valStr,
                onClick = { onSelected(valStr) },
                label = { Icon(icon, null, modifier = Modifier.size(20.dp)) }
            )
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
    var productSearchQuery by remember { mutableStateOf("") }
    val backendUrl = remember { getBaseUrl() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 24.dp).navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Gerenciar Produtos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                TextButton(onClick = onDismiss) { Text("OK", fontWeight = FontWeight.Bold) }
            }
            
            Spacer(Modifier.height(16.dp))

            var currentCustomLabel by remember { mutableStateOf(component.customLabel ?: "") }
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
                    Checkbox(checked = isHorizontal, onCheckedChange = { 
                        isHorizontal = it
                        onComponentUpdated(component.copy(isHorizontal = it))
                    })
                    Text("Exibir Horizontalmente (Carrossel)", fontSize = 14.sp)
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                
                if (availableProducts.isNotEmpty()) {
                    val filteredSuggestions = availableProducts
                        .filter { p -> component.products.none { it.id == p.id } }
                        .filter { p -> p.name.contains(productSearchQuery, ignoreCase = true) }

                    Spacer(Modifier.height(16.dp))
                    Text("Adicionar do Inventário", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    
                    OutlinedTextField(
                        value = productSearchQuery,
                        onValueChange = { productSearchQuery = it },
                        placeholder = { Text("Buscar no estoque...") },
                        leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        shape = CircleShape,
                        singleLine = true
                    )

                    if (filteredSuggestions.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(filteredSuggestions) { product ->
                                val pImageUrl = product.imageUrls.firstOrNull()?.let { if (it.startsWith("/")) "$backendUrl$it" else it } ?: ""
                                Surface(
                                    modifier = Modifier
                                        .width(140.dp)
                                        .clickable { onComponentUpdated(component.copy(products = component.products + product)) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.5f))
                                ) {
                                    Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        if (pImageUrl.isNotEmpty()) {
                                            AsyncImage(model = pImageUrl, contentDescription = null, modifier = Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Crop)
                                        }
                                        Text(product.name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall)
                                        Text("R$ ${product.price}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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
                    val pImageUrl = product.imageUrls.firstOrNull()?.let { if (it.startsWith("/")) "$backendUrl$it" else it } ?: ""
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onEditProduct(product) },
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (pImageUrl.isNotEmpty()) {
                                AsyncImage(model = pImageUrl, contentDescription = null, modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(product.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                Text("R$ ${product.price}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
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
                Button(onClick = onAddProduct, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Cadastrar Novo Produto")
                }
                
                Spacer(Modifier.height(16.dp))
                OutlinedButton(onClick = onDeleteComponent, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
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
                Text("Estilo Visual", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                TextButton(onClick = onDismiss) { Text("OK", fontWeight = FontWeight.Bold) }
            }
            
            Spacer(Modifier.height(20.dp))
            
            val themes = listOf(
                Triple(PageThemeConfig.ROYAL, "Royal", Color(0xFF14213D)),
                Triple(PageThemeConfig.OCEAN, "Soft Ocean", Color(0xFF00ADB5)),
                Triple(PageThemeConfig.FOREST, "Eco Forest", Color(0xFF283618)),
                Triple(PageThemeConfig.CANDY, "Rose Gold", Color(0xFFD81159)),
                Triple(PageThemeConfig.SUNSET, "Sunset", Color(0xFFE76F51)),
                Triple(PageThemeConfig.BERRY, "Berry", Color(0xFF6A0572)),
                Triple(PageThemeConfig.MINIMAL, "Minimal", Color(0xFF000000)),
                Triple(PageThemeConfig.VINTAGE, "Vintage", Color(0xFF8B5E3C)),
                Triple(PageThemeConfig.NORDIC, "Nordic", Color(0xFF4A90E2)),
                Triple(PageThemeConfig.COFFEE, "Coffee", Color(0xFF6F4E37)),
                Triple(PageThemeConfig.LUXURY_GOLD, "Luxury Gold", Color(0xFFD4AF37))
            )
            
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                themes.forEach { triple ->
                    val config = triple.first
                    val label = triple.second
                    val color = triple.third
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
                            Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = if (currentTheme == config) FontWeight.Bold else FontWeight.Normal)
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
                Text("Adicionar", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                TextButton(onClick = onDismiss) { Text("Fechar") }
            }
            Spacer(Modifier.height(16.dp))
            
            Text("Componentes", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            val catalogItems = listOf(
                Triple("Barra de Busca", PageComponent.Filter(), Icons.Default.Search),
                Triple("Filtro de Categorias", PageComponent.CategoryFilter(), Icons.Default.Category),
                Triple("Título", PageComponent.Header(title = ""), Icons.Default.Title),
                Triple("Texto", PageComponent.Text(content = ""), Icons.AutoMirrored.Filled.Notes),
                Triple("Imagem", PageComponent.Image(url = "", string = ""), Icons.Default.Image),
                Triple("Botão de Link", PageComponent.Button(text = "", url = ""), Icons.Default.SmartButton),
                Triple("Lista de Produtos", PageComponent.ProductList(products = emptyList()), Icons.Default.ShoppingBag)
            )
            catalogItems.forEach { triple -> 
                val name = triple.first
                val component = triple.second
                val icon = triple.third
                CatalogItemRow(name, icon) { onComponentSelected(component) } 
            }

            Spacer(Modifier.height(24.dp))
            Text("Templates", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
            
            Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { 
                    onTemplateSelected(listOf(
                        PageComponent.Image(url = "", string = "", size = 80, destinationPageId = null, isFullWidth = false, isRounded = true, customLabel = null, isFilterable = false), 
                        PageComponent.Header(title = "Bem-vindo!", fontSize = 34, textAlign = "CENTER", isFilterable = false), 
                        PageComponent.Filter(placeholder = "Buscar na loja..."),
                        PageComponent.CategoryFilter(),
                        PageComponent.ProductList(products = emptyList(), isHorizontal = true, customLabel = "Destaques")
                    )) 
                }, 
                shape = RoundedCornerShape(12.dp), 
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Dashboard, null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.width(16.dp))
                    Text("Layout Loja Padrão", style = MaterialTheme.typography.titleSmall)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageSettingsModal(
    page: Page,
    onUpdate: (Page) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var title by remember { mutableStateOf(page.title) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(Modifier.fillMaxWidth().padding(24.dp).navigationBarsPadding()) {
            Text("Nome da Página", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))
            
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título da Vitrine") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(Modifier.height(32.dp))
            
            Button(
                onClick = { 
                    onUpdate(page.copy(title = title))
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Salvar Nome")
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
