package com.itbenevides.genesys21

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.presentation.PageViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel
import kotlin.random.Random

enum class Screen { Login, List, Editor, WhiteLabel }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
    KoinContext {
        val viewModel: PageViewModel = koinViewModel()
        var currentScreen by remember { mutableStateOf(Screen.Login) }
        var selectedPage by remember { mutableStateOf<Page?>(null) }

        LaunchedEffect(currentScreen, selectedPage) {
            syncUrlWithScreen(currentScreen, selectedPage?.id)
        }

        MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF101828), background = Color(0xFFF2F4F7))) {
            Surface(modifier = Modifier.fillMaxSize()) {
                AnimatedContent(targetState = currentScreen) { screen ->
                    when (screen) {
                        Screen.Login -> LoginScreen(viewModel = viewModel, onLoginSuccess = { currentScreen = Screen.List })
                        Screen.List -> PageListScreen(
                            viewModel = viewModel,
                            onAddPage = { selectedPage = null; currentScreen = Screen.Editor },
                            onEditPage = { page -> selectedPage = page; currentScreen = Screen.Editor },
                            onViewPage = { page -> selectedPage = page; currentScreen = Screen.WhiteLabel },
                            onLogout = { currentScreen = Screen.Login }
                        )
                        Screen.Editor -> PageEditorScreen(viewModel = viewModel, page = selectedPage, onBack = { currentScreen = Screen.List })
                        Screen.WhiteLabel -> WhiteLabelScreen(viewModel = viewModel, page = selectedPage!!, onBack = { currentScreen = Screen.List })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhiteLabelScreen(viewModel: PageViewModel, page: Page, onBack: () -> Unit) {
    var currentPage by remember { mutableStateOf(page) }
    var showCatalog by remember { mutableStateOf(false) }
    var editingComponentIndex by remember { mutableStateOf<Int?>(null) }
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(currentPage.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Editor White Label", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                actions = {
                    Button(onClick = { viewModel.savePage(currentPage, true) { onBack() } }, shape = RoundedCornerShape(8.dp)) {
                        Icon(Icons.Default.CloudUpload, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Publicar")
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
                contentColor = Color.White
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            if (currentPage.components.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Página vazia.\nAdicione componentes para começar.", textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = Color.Gray)
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
                        currentPage = currentPage.copy(components = currentPage.components + newComponent)
                        showCatalog = false
                    },
                    onDismiss = { showCatalog = false }
                )
            }

            editingComponentIndex?.let { index ->
                EditComponentModal(
                    component = currentPage.components[index],
                    onComponentUpdated = { updated ->
                        currentPage = currentPage.copy(
                            components = currentPage.components.toMutableList().apply { set(index, updated) }
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
fun ComponentWrapper(component: PageComponent, onDelete: () -> Unit, onEdit: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE4E7EC), RoundedCornerShape(12.dp)).background(Color.White, RoundedCornerShape(12.dp)).padding(16.dp)) {
        Column {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(component::class.simpleName ?: "Componente", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            when (component) {
                is PageComponent.Header -> Text(component.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                is PageComponent.Text -> Text(component.content, style = MaterialTheme.typography.bodyMedium)
                is PageComponent.Image -> {
                    Column {
                        Box(Modifier.fillMaxWidth().height(150.dp).background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                            if (component.url.isNotEmpty()) {
                                Text("[Imagem: ${component.url}]", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            } else {
                                Icon(Icons.Default.Image, null, tint = Color.LightGray)
                            }
                        }
                        if (component.string.isNotEmpty()) {
                            Text(component.string, style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditComponentModal(component: PageComponent, onComponentUpdated: (PageComponent) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(24.dp).fillMaxWidth().navigationBarsPadding()) {
            Text("Editar Componente", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            when (component) {
                is PageComponent.Header -> {
                    var title by remember { mutableStateOf(component.title) }
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = { onComponentUpdated(component.copy(title = title)) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Confirmar")
                    }
                }
                is PageComponent.Text -> {
                    var content by remember { mutableStateOf(component.content) }
                    OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Conteúdo") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = { onComponentUpdated(component.copy(content = content)) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Confirmar")
                    }
                }
                is PageComponent.Image -> {
                    var url by remember { mutableStateOf(component.url) }
                    var description by remember { mutableStateOf(component.string) }
                    OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("URL da Imagem") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = { onComponentUpdated(component.copy(url = url, string = description)) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Confirmar")
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentCatalogModal(onComponentSelected: (PageComponent) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(24.dp).fillMaxWidth().navigationBarsPadding()) {
            Text("Catálogo de Componentes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            
            val catalogItems = listOf(
                Pair("Título", PageComponent.Header("Novo Título")),
                Pair("Texto", PageComponent.Text("Novo parágrafo de conteúdo...")),
                Pair("Imagem", PageComponent.Image("", "Descrição da imagem"))
            )

            catalogItems.forEach { (name, component) ->
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable { onComponentSelected(component) }.padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF9FAFB),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE4E7EC))
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        val icon = when(component) {
                            is PageComponent.Header -> Icons.Default.Title
                            is PageComponent.Text -> Icons.AutoMirrored.Filled.Notes
                            else -> Icons.Default.Image
                        }
                        Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(16.dp))
                        Text(name, fontWeight = FontWeight.Medium)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun LoginScreen(viewModel: PageViewModel, onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.fillMaxWidth().widthIn(max = 360.dp)) {
            Text("Login", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(48.dp))
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Senha") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    isLoading = true
                    viewModel.signIn(email, password, 
                        onSuccess = { onLoginSuccess(); isLoading = false },
                        onFailure = { errorMessage = it; isLoading = false }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) CircularProgressIndicator(Modifier.size(24.dp), color = Color.White) else Text("Entrar")
            }
            if (errorMessage.isNotEmpty()) Text(errorMessage, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageListScreen(viewModel: PageViewModel, onAddPage: () -> Unit, onEditPage: (Page) -> Unit, onViewPage: (Page) -> Unit, onLogout: () -> Unit) {
    val pages by viewModel.pages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadPages() }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Páginas", fontWeight = FontWeight.Bold) }, actions = {
                IconButton(onClick = onLogout) { Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color.Gray) }
            })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPage, shape = RoundedCornerShape(16.dp)) { Icon(Icons.Default.Add, null) }
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { Spacer(Modifier.height(16.dp)) }
                items(pages) { page ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { onViewPage(page) },
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE4E7EC))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(page.title, fontWeight = FontWeight.Bold)
                                Text(page.id, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            IconButton(onClick = { onEditPage(page) }) { Icon(Icons.Default.Edit, null, tint = Color.Gray) }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageEditorScreen(viewModel: PageViewModel, page: Page?, onBack: () -> Unit) {
    var title by remember { mutableStateOf(page?.title ?: "") }
    val id = remember { page?.id ?: (1..8).map { "abcdefghijklmnopqrstuvwxyz0123456789"[Random.nextInt(36)] }.joinToString("") }
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (page != null) "Editar" else "Novo", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(32.dp)) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    val newPage = (page ?: Page(id, title)).copy(title = title)
                    viewModel.savePage(newPage, isEditing = page != null) { onBack() }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = title.isNotBlank() && !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(Modifier.size(24.dp)) else Text("Salvar")
            }
        }
    }
}
