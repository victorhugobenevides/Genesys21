package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
            TopAppBar(
                title = { 
                    Column {
                        Text(currentPage.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Editor White Label", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                actions = {
                    Button(
                        onClick = { viewModel.savePage(currentPage, true) { onBack() } }, 
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) CircularProgressIndicator(Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary)
                        else {
                            Icon(Icons.Default.CloudUpload, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Publicar")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCatalog = true },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Adicionar") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            if (currentPage.components.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Página vazia.\nAdicione componentes para começar.", 
                        textAlign = TextAlign.Center, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(currentPage.components.size) { index ->
                        val component = currentPage.components[index]
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
    val commonShape = if (component.isRounded) CircleShape else RoundedCornerShape(8.dp)
    
    when (component) {
        is PageComponent.Logo -> {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier.size(component.size.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                ) {
                    if (component.url.isNotEmpty()) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("LOGO", style = MaterialTheme.typography.labelSmall)
                        }
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
                    Text("Nenhum produto cadastrado", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                } else {
                    component.products.chunked(2).forEach { rowProducts ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            rowProducts.forEach { product ->
                                // Estilo do produto baseado no componente pai
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = commonShape,
                                    color = if (component.isTransparent) Color.Transparent else MaterialTheme.colorScheme.surface,
                                    border = if (component.isTransparent) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                ) {
                                    Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(1f) // Imagem quadrada ou circular perfeita
                                                .clip(commonShape)
                                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)), 
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (product.imageUrl.isNotEmpty()) {
                                                Text("IMG", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                            } else {
                                                Icon(Icons.Default.ShoppingBag, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(32.dp))
                                            }
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            product.name.ifBlank { "Sem nome" }, 
                                            style = MaterialTheme.typography.labelLarge, 
                                            maxLines = 1, 
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            "R$ ${product.price}", 
                                            style = MaterialTheme.typography.bodyMedium, 
                                            fontWeight = FontWeight.Bold, 
                                            color = MaterialTheme.colorScheme.primary
                                        )
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
            text = component.title.ifBlank { "Título do Cabeçalho" }, 
            style = MaterialTheme.typography.headlineMedium, 
            color = if (component.title.isBlank()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
            textAlign = if (component.isRounded) TextAlign.Center else TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
        is PageComponent.Text -> Text(
            text = component.content.ifBlank { "Conteúdo do Texto" }, 
            style = MaterialTheme.typography.bodyMedium, 
            color = if (component.content.isBlank()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
            textAlign = if (component.isRounded) TextAlign.Center else TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
        is PageComponent.Image -> {
            val imgShape = if (component.isRounded) CircleShape else RoundedCornerShape(12.dp)
            Column(horizontalAlignment = if (component.isRounded) Alignment.CenterHorizontally else Alignment.Start) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(if (component.isRounded) 1f else 1.7f)
                        .clip(imgShape)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)), 
                    contentAlignment = Alignment.Center
                ) {
                    if (component.url.isNotEmpty()) {
                        Text("[Imagem: ${component.url}]", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    } else {
                        Icon(Icons.Default.Image, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(48.dp))
                    }
                }
                if (component.string.isNotEmpty()) {
                    Text(
                        component.string, 
                        style = MaterialTheme.typography.labelSmall, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant, 
                        modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                        textAlign = if (component.isRounded) TextAlign.Center else TextAlign.Start
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
    val wrapperShape = if (component.isRounded && component !is PageComponent.ProductList) CircleShape else RoundedCornerShape(12.dp)
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = wrapperShape,
        color = if (component.isTransparent) Color.Transparent else MaterialTheme.colorScheme.surface,
        border = if (component.isTransparent) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                if (!component.customLabel.isNullOrBlank()) {
                    Text(
                        component.customLabel!!, 
                        style = MaterialTheme.typography.labelSmall, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        component::class.simpleName ?: "", 
                        style = MaterialTheme.typography.labelSmall, 
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                    }
                }
            }
            
            Spacer(Modifier.height(8.dp))
            PageComponentRenderer(component)
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
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 16.dp)
                .navigationBarsPadding()
        ) {
            Text(
                if (isNew) "Novo Componente" else "Editar Componente", 
                style = MaterialTheme.typography.titleLarge, 
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(24.dp))

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
                Text("Visualização em miniatura", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                
                val previewComp = when (component) {
                    is PageComponent.Header -> PageComponent.Header(headerTitle, currentCustomLabel.ifBlank { null }, isTransparent, isRounded)
                    is PageComponent.Text -> PageComponent.Text(textContent, customLabel = currentCustomLabel.ifBlank { null }, isTransparent = isTransparent, isRounded = isRounded)
                    is PageComponent.Image -> PageComponent.Image(imageUrl, imageDesc, currentCustomLabel.ifBlank { null }, isTransparent, isRounded)
                    is PageComponent.Logo -> PageComponent.Logo(logoUrl, logoSize.toIntOrNull() ?: 64, currentCustomLabel.ifBlank { null }, isTransparent, isRounded)
                    is PageComponent.ProductList -> PageComponent.ProductList(productList, currentCustomLabel.ifBlank { null }, isTransparent, isRounded)
                }

                Surface(
                    modifier = Modifier.fillMaxWidth().widthIn(max = 300.dp).align(Alignment.CenterHorizontally),
                    shape = if (isRounded && component !is PageComponent.ProductList) CircleShape else RoundedCornerShape(12.dp),
                    color = if (isTransparent) Color.Transparent else MaterialTheme.colorScheme.surface,
                    shadowElevation = if (isTransparent) 0.dp else 2.dp,
                    border = if (isTransparent) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            text = currentCustomLabel.ifBlank { component::class.simpleName ?: "Componente" },
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = if (currentCustomLabel.isBlank()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
                        )
                        Box(modifier = Modifier.padding(top = 4.dp)) {
                            PageComponentRenderer(previewComp)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isTransparent, onCheckedChange = { isTransparent = it })
                    Text("Fundo Transparente", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.width(16.dp))
                    Checkbox(checked = isRounded, onCheckedChange = { isRounded = it })
                    Text("Formato Redondo", style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = currentCustomLabel, 
                    onValueChange = { currentCustomLabel = it }, 
                    label = { Text("Nome de Identificação (Opcional)") }, 
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(Modifier.height(16.dp))

                when (component) {
                    is PageComponent.Header -> {
                        OutlinedTextField(value = headerTitle, onValueChange = { headerTitle = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    }
                    is PageComponent.Text -> {
                        OutlinedTextField(value = textContent, onValueChange = { textContent = it }, label = { Text("Conteúdo") }, modifier = Modifier.fillMaxWidth(), minLines = 3, shape = RoundedCornerShape(12.dp))
                    }
                    is PageComponent.Image -> {
                        OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("URL da Imagem") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(value = imageDesc, onValueChange = { imageDesc = it }, label = { Text("Legenda") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    }
                    is PageComponent.Logo -> {
                        OutlinedTextField(value = logoUrl, onValueChange = { logoUrl = it }, label = { Text("URL do Logo") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(value = logoSize, onValueChange = { if(it.all { c -> c.isDigit() }) logoSize = it }, label = { Text("Tamanho (px)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }
                    is PageComponent.ProductList -> {
                        Text("Gerenciar Produtos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        
                        productList.forEachIndexed { pIndex, product ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("Produto ${pIndex + 1}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                        IconButton(onClick = { productList = productList.toMutableList().apply { removeAt(pIndex) } }) {
                                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    OutlinedTextField(
                                        value = product.name, 
                                        onValueChange = { newName -> productList = productList.toMutableList().apply { set(pIndex, product.copy(name = newName)) } },
                                        label = { Text("Nome do Produto") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = product.price.toString(), 
                                            onValueChange = { newPrice -> 
                                                newPrice.toDoubleOrNull()?.let { 
                                                    productList = productList.toMutableList().apply { set(pIndex, product.copy(price = it)) } 
                                                }
                                            },
                                            label = { Text("Preço") },
                                            modifier = Modifier.weight(1f),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        OutlinedTextField(
                                            value = product.imageUrl, 
                                            onValueChange = { newUrl -> productList = productList.toMutableList().apply { set(pIndex, product.copy(imageUrl = newUrl)) } },
                                            label = { Text("URL Imagem") },
                                            modifier = Modifier.weight(2f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                        
                        Button(
                            onClick = {
                                productList = productList + Product(
                                    id = Random.nextInt().toString(),
                                    name = "",
                                    price = 0.0,
                                    imageUrl = ""
                                )
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Adicionar Novo Produto")
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            val isDataValid = when (component) {
                is PageComponent.Header -> headerTitle.isNotBlank()
                is PageComponent.Text -> textContent.isNotBlank()
                is PageComponent.Image -> imageUrl.isNotBlank()
                is PageComponent.Logo -> logoUrl.isNotBlank()
                is PageComponent.ProductList -> productList.isNotEmpty()
            }

            ActionButtons(
                isNew = isNew,
                enabled = isDataValid,
                onConfirm = {
                    val finalComp = when (component) {
                        is PageComponent.Header -> PageComponent.Header(headerTitle, currentCustomLabel.ifBlank { null }, isTransparent, isRounded)
                        is PageComponent.Text -> PageComponent.Text(textContent, customLabel = currentCustomLabel.ifBlank { null }, isTransparent = isTransparent, isRounded = isRounded)
                        is PageComponent.Image -> PageComponent.Image(imageUrl, imageDesc, currentCustomLabel.ifBlank { null }, isTransparent, isRounded)
                        is PageComponent.Logo -> PageComponent.Logo(logoUrl, logoSize.toIntOrNull() ?: 64, currentCustomLabel.ifBlank { null }, isTransparent, isRounded)
                        is PageComponent.ProductList -> PageComponent.ProductList(productList, currentCustomLabel.ifBlank { null }, isTransparent, isRounded)
                    }
                    onComponentUpdated(finalComp)
                },
                onCancel = onDeleteRequest
            )
        }
    }
}

@Composable
fun ActionButtons(isNew: Boolean, enabled: Boolean, onConfirm: () -> Unit, onCancel: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
            Text(if (isNew) "Descartar" else "Remover")
        }
        Button(onClick = onConfirm, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), enabled = enabled) {
            Text(if (isNew) "Adicionar" else "Salvar")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentCatalogModal(
    onComponentSelected: (PageComponent) -> Unit, 
    onTemplateSelected: (List<PageComponent>) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 24.dp).padding(top = 8.dp, bottom = 32.dp).fillMaxWidth().navigationBarsPadding()) {
            Text("Catálogo & Templates", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            
            Text("Componentes Individuais", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            
            val catalogItems = listOf(
                Triple("Logo", PageComponent.Logo(""), Icons.Default.Store),
                Triple("Título", PageComponent.Header(""), Icons.Default.Title),
                Triple("Texto", PageComponent.Text(""), Icons.AutoMirrored.Filled.Notes),
                Triple("Imagem", PageComponent.Image("", ""), Icons.Default.Image),
                Triple("Lista de Produtos", PageComponent.ProductList(emptyList()), Icons.Default.ShoppingBag)
            )

            catalogItems.forEach { (name, component, icon) ->
                CatalogItemRow(name, icon) { onComponentSelected(component) }
            }

            Spacer(Modifier.height(24.dp))
            
            Text("Sugestões de Templates", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth().clickable { 
                    onTemplateSelected(listOf(
                        PageComponent.Logo("", 80, "Logo da Minha Loja", isTransparent = true, isRounded = true),
                        PageComponent.Header("Bem-vindo à nossa Loja!", isTransparent = true),
                        PageComponent.Text("Confira nossas ofertas exclusivas abaixo.", isTransparent = true),
                        PageComponent.ProductList(listOf(
                            Product("1", "Camiseta Básica", 49.90, ""),
                            Product("2", "Tênis Esportivo", 199.00, ""),
                            Product("3", "Calça Jeans", 89.90, "")
                        ), isTransparent = true)
                    ))
                },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Dashboard, null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Template: Loja Completa", style = MaterialTheme.typography.titleSmall)
                        Text("Logo + Títulos + Grade de Produtos", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun CatalogItemRow(name: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(name, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
