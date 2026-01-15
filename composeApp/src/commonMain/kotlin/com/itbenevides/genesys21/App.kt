package com.itbenevides.genesys21

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.presentation.screens.editor.PageEditorScreen
import com.itbenevides.genesys21.presentation.screens.list.PageListScreen
import com.itbenevides.genesys21.presentation.screens.login.LoginScreen
import com.itbenevides.genesys21.presentation.screens.viewer.WhiteLabelScreen
import com.itbenevides.genesys21.ui.theme.AppTheme
import kotlinx.coroutines.delay
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel

enum class Screen { Splash, Login, List, Editor, WhiteLabel }

@Composable
@Preview
fun App() {
    KoinContext {
        AppTheme {
            val viewModel: PageViewModel = koinViewModel()
            var currentScreen by remember { mutableStateOf(Screen.Splash) }
            var selectedPage by remember { mutableStateOf<Page?>(null) }

            // Lógica de verificação de login automático
            LaunchedEffect(Unit) {
                val token = viewModel.getCurrentUserToken()
                delay(500) // Pequeno delay para evitar flickering
                if (token != null) {
                    currentScreen = Screen.List
                } else {
                    currentScreen = Screen.Login
                }
            }

            LaunchedEffect(currentScreen, selectedPage) {
                syncUrlWithScreen(currentScreen, selectedPage?.id)
            }

            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                AnimatedContent(targetState = currentScreen) { screen ->
                    when (screen) {
                        Screen.Splash -> SplashScreen()
                        Screen.Login -> LoginScreen(
                            viewModel = viewModel, 
                            onLoginSuccess = { currentScreen = Screen.List }
                        )
                        Screen.List -> PageListScreen(
                            viewModel = viewModel,
                            onAddPage = { selectedPage = null; currentScreen = Screen.Editor },
                            onEditPage = { page -> selectedPage = page; currentScreen = Screen.Editor },
                            onViewPage = { page -> selectedPage = page; currentScreen = Screen.WhiteLabel },
                            onLogout = { 
                                viewModel.signOut()
                                currentScreen = Screen.Login 
                            }
                        )
                        Screen.Editor -> PageEditorScreen(
                            viewModel = viewModel, 
                            page = selectedPage, 
                            onBack = { currentScreen = Screen.List }
                        )
                        Screen.WhiteLabel -> WhiteLabelScreen(
                            viewModel = viewModel, 
                            page = selectedPage!!, 
                            onBack = { currentScreen = Screen.List }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
