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
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.random.Random

enum class Screen {
    Login, List, Editor
}

// Configuração do Cliente Ktor
val client = HttpClient {
    install(ContentNegotiation) { json() }
    install(HttpTimeout) {
        requestTimeoutMillis = 15000
    }
}

// Em produção/Docker, o host costuma ser localhost se acessado do mesmo PC.
// Se falhar, tente usar o IP real da sua máquina (ex: 192.168.x.x)
const val API_URL = "http://localhost:8080" 

fun generateRandomId(length: Int = 8): String {
    val charPool = "abcdefghijklmnopqrstuvwxyz0123456789"
    return (1..length)
        .map { Random.nextInt(0, charPool.length).let { charPool[it] } }
        .joinToString("")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF2D3250),
            secondary = Color(0xFF7077A1),
            tertiary = Color(0xFFF6B17A)
        )
    ) {
        var currentScreen by remember { mutableStateOf(Screen.Login) }
        var selectedPage by remember { mutableStateOf<Page?>(null) }

        LaunchedEffect(currentScreen, selectedPage) {
            syncUrlWithScreen(currentScreen, selectedPage?.id)
        }

        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            when (currentScreen) {
                Screen.Login -> LoginScreen(onLoginSuccess = { currentScreen = Screen.List })
                Screen.List -> PageListScreen(
                    onAddPage = { 
                        selectedPage = null
                        currentScreen = Screen.Editor 
                    },
                    onEditPage = { page ->
                        selectedPage = page
                        currentScreen = Screen.Editor
                    },
                    onLogout = { currentScreen = Screen.Login }
                )
                Screen.Editor -> PageEditorScreen(
                    page = selectedPage,
                    onBack = { currentScreen = Screen.List }
                )
            }
        }
    }
}

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val authRepository = remember { getAuthRepository() }
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(modifier = Modifier.fillMaxWidth(0.9f).widthIn(max = 400.dp), shape = RoundedCornerShape(24.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Genesys21", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Senha") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            errorMessage = ""
                            println("Tentando login...")
                            authRepository.signIn(email, password)
                                .onSuccess { 
                                    println("Login Firebase sucesso!")
                                    onLoginSuccess() 
                                }
                                .onFailure { 
                                    println("Login Firebase erro: ${it.message}")
                                    errorMessage = it.message ?: "Erro no login" 
                                }
                            isLoading = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp)) else Text("Entrar")
                }
                if (errorMessage.isNotEmpty()) {
                    Text(errorMessage, color = Color.Red, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageListScreen(onAddPage: () -> Unit, onEditPage: (Page) -> Unit, onLogout: () -> Unit) {
    var pages by remember { mutableStateOf(emptyList<Page>()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            isLoading = true
            println("Buscando páginas em $API_URL/pages...")
            val response = client.get("$API_URL/pages")
            pages = response.body()
            println("Páginas carregadas: ${pages.size}")
        } catch (e: Exception) {
            errorMessage = "Erro ao conectar com o servidor: ${e.message}"
            println("Erro Ktor: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Minhas Páginas") }, actions = {
                IconButton(onClick = onLogout) { Icon(Icons.AutoMirrored.Filled.Logout, null) }
            })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPage) { Icon(Icons.Default.Add, null) }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = Color.Red, modifier = Modifier.padding(16.dp))
            }
            
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(pages) { page ->
                        ListItem(
                            headlineContent = { Text(page.title) },
                            supportingContent = { Text("ID: ${page.id}") },
                            trailingContent = { Icon(Icons.Default.Edit, null) },
                            modifier = Modifier.clickable { onEditPage(page) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageEditorScreen(page: Page?, onBack: () -> Unit) {
    val authRepository = remember { getAuthRepository() }
    var id by remember { mutableStateOf(page?.id ?: generateRandomId()) }
    var title by remember { mutableStateOf(page?.title ?: "") }
    val scope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    val isEditing = page != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Página" else "Nova Página") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = id, 
                onValueChange = { }, 
                label = { Text("ID (Automático)") }, 
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                readOnly = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
            
            if (errorMsg.isNotEmpty()) {
                Text(errorMsg, color = Color.Red, modifier = Modifier.padding(vertical = 8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    scope.launch {
                        try {
                            isSaving = true
                            errorMsg = ""
                            val token = authRepository.getCurrentUserToken()
                            val newPage = Page(id, title)
                            
                            val response = if (isEditing) {
                                client.put("$API_URL/pages") {
                                    header(HttpHeaders.Authorization, "Bearer $token")
                                    contentType(ContentType.Application.Json)
                                    setBody(newPage)
                                }
                            } else {
                                client.post("$API_URL/pages") {
                                    header(HttpHeaders.Authorization, "Bearer $token")
                                    contentType(ContentType.Application.Json)
                                    setBody(newPage)
                                }
                            }

                            if (response.status.isSuccess()) {
                                onBack()
                            } else {
                                errorMsg = "Erro no Servidor: ${response.status}"
                            }
                        } catch (e: Exception) {
                            errorMsg = "Falha de conexão: ${e.message}"
                        } finally {
                            isSaving = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            ) {
                if (isSaving) CircularProgressIndicator(Modifier.size(24.dp)) else Text("Salvar")
            }
        }
    }
}
