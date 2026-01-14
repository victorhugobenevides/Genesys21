package com.itbenevides.genesys21

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

val client = HttpClient {
    install(ContentNegotiation) { json() }
    install(HttpTimeout) { requestTimeoutMillis = 15000 }
}
const val API_URL = "http://localhost:8080" 

fun generateRandomId(length: Int = 8): String {
    val charPool = "abcdefghijklmnopqrstuvwxyz0123456789"
    return (1..length).map { charPool[Random.nextInt(0, charPool.length)] }.joinToString("")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
    val pureWhite = Color(0xFFFFFFFF)
    val softGray = Color(0xFFF2F4F7)
    val textPrimary = Color(0xFF101828)
    val textSecondary = Color(0xFF667085)
    val brandColor = Color(0xFF101828) // Preto minimalista

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = brandColor,
            onPrimary = pureWhite,
            background = softGray,
            surface = pureWhite,
            secondary = textSecondary
        ),
        typography = Typography(
            headlineLarge = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1).sp,
                color = textPrimary
            )
        )
    ) {
        var currentScreen by remember { mutableStateOf(Screen.Login) }
        var selectedPage by remember { mutableStateOf<Page?>(null) }

        LaunchedEffect(currentScreen, selectedPage) {
            syncUrlWithScreen(currentScreen, selectedPage?.id)
        }

        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() }
            ) { screen ->
                when (screen) {
                    Screen.Login -> LoginScreen(onLoginSuccess = { currentScreen = Screen.List })
                    Screen.List -> PageListScreen(
                        onAddPage = { selectedPage = null; currentScreen = Screen.Editor },
                        onEditPage = { page -> selectedPage = page; currentScreen = Screen.Editor },
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
}

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val authRepository = remember { getAuthRepository() }
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.fillMaxWidth().widthIn(max = 360.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text("Login", style = MaterialTheme.typography.headlineLarge)
            Text("Acesse sua conta para continuar", color = MaterialTheme.colorScheme.secondary)
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = email, 
                    onValueChange = { email = it }, 
                    placeholder = { Text("E-mail") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.LightGray)
                )
                OutlinedTextField(
                    value = password, 
                    onValueChange = { password = it }, 
                    placeholder = { Text("Senha") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.LightGray)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        authRepository.signIn(email, password).onSuccess { onLoginSuccess() }
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading,
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                if (isLoading) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White)
                else Text("Entrar", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageListScreen(onAddPage: () -> Unit, onEditPage: (Page) -> Unit, onLogout: () -> Unit) {
    var pages by remember { mutableStateOf(emptyList<Page>()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            pages = client.get("$API_URL/pages").body()
        } catch (e: Exception) { } finally { isLoading = false }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Páginas", fontWeight = FontWeight.Bold) },
                actions = { IconButton(onClick = onLogout) { Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color.Gray) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddPage, 
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(0.dp)
            ) { Icon(Icons.Default.Add, null) }
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(strokeWidth = 2.dp) }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(Modifier.height(16.dp)) }
                items(pages) { page ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { onEditPage(page) },
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE4E7EC))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(page.title, fontWeight = FontWeight.Bold, color = Color(0xFF101828))
                                Text(page.id, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            Icon(Icons.Default.KeyboardArrowRight, null, tint = Color.LightGray)
                        }
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
    val isEditing = page != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar" else "Novo", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(32.dp).fillMaxSize()) {
            Text(if (isEditing) "Altere os detalhes" else "Crie um novo item", color = Color.Gray)
            Spacer(modifier = Modifier.height(48.dp))
            
            Text("Título", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            TextField(
                value = title, 
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.LightGray
                ),
                textStyle = MaterialTheme.typography.headlineSmall
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("ID: $id", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
            
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = {
                    scope.launch {
                        try {
                            isSaving = true
                            val token = authRepository.getCurrentUserToken()
                            val newPage = Page(id, title)
                            val response = if (isEditing) client.put("$API_URL/pages") {
                                header(HttpHeaders.Authorization, "Bearer $token")
                                contentType(ContentType.Application.Json); setBody(newPage)
                            } else client.post("$API_URL/pages") {
                                header(HttpHeaders.Authorization, "Bearer $token")
                                contentType(ContentType.Application.Json); setBody(newPage)
                            }
                            if (response.status.isSuccess()) onBack()
                        } catch (e: Exception) { } finally { isSaving = false }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = title.isNotBlank() && !isSaving,
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                if (isSaving) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White) 
                else Text("Salvar Alterações", fontWeight = FontWeight.Bold)
            }
        }
    }
}
