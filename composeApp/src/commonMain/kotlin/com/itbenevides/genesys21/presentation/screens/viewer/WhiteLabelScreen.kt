package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.presentation.PageViewModel

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
    when (component) {
        is PageComponent.Header -> Text(
            text = component.title.ifBlank { "Título do Cabeçalho" }, 
            style = MaterialTheme.typography.headlineMedium, 
            color = if (component.title.isBlank()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
        )
        is PageComponent.Text -> Text(
            text = component.content.ifBlank { "Conteúdo do Texto" }, 
            style = MaterialTheme.typography.bodyMedium, 
            color = if (component.content.isBlank()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
        )
        is PageComponent.Image -> {
            Column {
                Box(
                    Modifier.fillMaxWidth().height(180.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(8.dp)), 
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
                        modifier = Modifier.padding(top = 8.dp)
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
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
    // Força o preenchimento total da tela
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

            // Estado para o preview e campos
            var currentCustomLabel by remember { mutableStateOf(component.customLabel ?: "") }
            var headerTitle by remember { mutableStateOf(if (component is PageComponent.Header) component.title else "") }
            var textContent by remember { mutableStateOf(if (component is PageComponent.Text) component.content else "") }
            var imageUrl by remember { mutableStateOf(if (component is PageComponent.Image) component.url else "") }
            var imageDesc by remember { mutableStateOf(if (component is PageComponent.Image) component.string else "") }

            // ÁREA DE ROLAGEM (Formulário e Miniatura)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Visualização em miniatura", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                
                Surface(
                    modifier = Modifier.fillMaxWidth().widthIn(max = 300.dp).align(Alignment.CenterHorizontally),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            text = currentCustomLabel.ifBlank { component::class.simpleName ?: "Componente" },
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = if (currentCustomLabel.isBlank()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary,
                            fontWeight = if (currentCustomLabel.isNotBlank()) FontWeight.Bold else FontWeight.Normal
                        )
                        Spacer(Modifier.height(4.dp))
                        
                        val previewComp = when (component) {
                            is PageComponent.Header -> PageComponent.Header(headerTitle, currentCustomLabel.ifBlank { null })
                            is PageComponent.Text -> PageComponent.Text(textContent, customLabel = currentCustomLabel.ifBlank { null })
                            is PageComponent.Image -> PageComponent.Image(imageUrl, imageDesc, currentCustomLabel.ifBlank { null })
                        }

                        Box(modifier = Modifier.padding(top = 4.dp)) {
                            PageComponentRenderer(previewComp)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = currentCustomLabel, 
                    onValueChange = { currentCustomLabel = it }, 
                    label = { Text("Nome de Identificação (Opcional)") }, 
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ex: Banner Principal") },
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(Modifier.height(16.dp))

                when (component) {
                    is PageComponent.Header -> {
                        OutlinedTextField(value = headerTitle, onValueChange = { headerTitle = it }, label = { Text("Título do Cabeçalho") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    }
                    is PageComponent.Text -> {
                        OutlinedTextField(value = textContent, onValueChange = { textContent = it }, label = { Text("Conteúdo do Texto") }, modifier = Modifier.fillMaxWidth(), minLines = 3, shape = RoundedCornerShape(12.dp))
                    }
                    is PageComponent.Image -> {
                        OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("URL da Imagem") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(value = imageDesc, onValueChange = { imageDesc = it }, label = { Text("Legenda (Opcional)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // BOTÕES FIXOS NA BASE
            val isDataValid = when (component) {
                is PageComponent.Header -> headerTitle.isNotBlank()
                is PageComponent.Text -> textContent.isNotBlank()
                is PageComponent.Image -> imageUrl.isNotBlank()
            }

            ActionButtons(
                isNew = isNew,
                enabled = isDataValid,
                onConfirm = {
                    val finalComp = when (component) {
                        is PageComponent.Header -> PageComponent.Header(headerTitle, currentCustomLabel.ifBlank { null })
                        is PageComponent.Text -> PageComponent.Text(textContent, customLabel = currentCustomLabel.ifBlank { null })
                        is PageComponent.Image -> PageComponent.Image(imageUrl, imageDesc, currentCustomLabel.ifBlank { null })
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onCancel, 
            modifier = Modifier.weight(1f), 
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text(if (isNew) "Descartar" else "Remover")
        }
        Button(
            onClick = onConfirm, 
            modifier = Modifier.weight(1f), 
            shape = RoundedCornerShape(12.dp),
            enabled = enabled
        ) {
            Text(if (isNew) "Adicionar" else "Salvar")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentCatalogModal(onComponentSelected: (PageComponent) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 24.dp).padding(top = 8.dp, bottom = 32.dp).fillMaxWidth().navigationBarsPadding()) {
            Text("Escolha um Componente", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            
            val catalogItems = listOf(
                Pair("Título", PageComponent.Header("")),
                Pair("Texto", PageComponent.Text("")),
                Pair("Imagem", PageComponent.Image("", ""))
            )

            catalogItems.forEach { (name, component) ->
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable { onComponentSelected(component) }.padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        val icon = when(component) {
                            is PageComponent.Header -> Icons.Default.Title
                            is PageComponent.Text -> Icons.AutoMirrored.Filled.Notes
                            else -> Icons.Default.Image
                        }
                        Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(16.dp))
                        Text(name, style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
