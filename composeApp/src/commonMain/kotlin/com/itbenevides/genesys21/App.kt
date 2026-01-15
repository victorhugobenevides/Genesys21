package com.itbenevides.genesys21

import androidx.compose.animation.*
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
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.presentation.PageViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel
import kotlin.random.Random

enum class Screen { Login, List, Editor, Viewer }

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
                            onViewPage = { page -> selectedPage = page; currentScreen = Screen.Viewer },
                            onLogout = { currentScreen = Screen.Login }
                        )
                        Screen.Editor -> PageEditorScreen(viewModel = viewModel, page = selectedPage, onBack = { currentScreen = Screen.List })
                        Screen.Viewer -> PageViewerScreen(viewModel = viewModel, page = selectedPage!!, onBack = { currentScreen = Screen.List })
                    }
                }
            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageViewerScreen(viewModel: PageViewModel, page: Page, onBack: () -> Unit) {
    var currentPage by remember { mutableStateOf(page) }
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentPage.title, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = { viewModel.savePage(currentPage, true) {} }) { Icon(Icons.Default.Save, null) }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                IconButton(onClick = { currentPage = currentPage.copy(components = currentPage.components + PageComponent.Header("Novo Título")) }) {
                    Icon(Icons.Default.Title, null)
                }
                IconButton(onClick = { currentPage = currentPage.copy(components = currentPage.components + PageComponent.Text("Novo Texto")) }) {
                    Icon(Icons.AutoMirrored.Filled.Notes, null)
                }
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(currentPage.components) { component ->
                when (component) {
                    is PageComponent.Header -> Text(component.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    is PageComponent.Text -> Text(component.content)
                    is PageComponent.Image -> Text("[Imagem]")
                }
            }
        }
    }
}
