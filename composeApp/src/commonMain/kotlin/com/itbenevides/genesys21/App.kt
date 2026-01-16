package com.itbenevides.genesys21

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.presentation.screens.editor.PageEditorScreen
import com.itbenevides.genesys21.presentation.screens.list.PageListScreen
import com.itbenevides.genesys21.presentation.screens.login.LoginScreen
import com.itbenevides.genesys21.presentation.screens.viewer.WhiteLabelScreen
import com.itbenevides.genesys21.presentation.screens.viewer.PageViewerScreen
import com.itbenevides.genesys21.presentation.screens.viewer.ProductDetailsScreen
import com.itbenevides.genesys21.presentation.screens.editor.ProductEditorScreen
import com.itbenevides.genesys21.ui.theme.AppTheme
import kotlinx.coroutines.delay
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel

enum class Screen { Splash, Login, List, Editor, WhiteLabel, PublicViewer, ProductDetails, ProductEditor }

@Composable
@Preview
fun App() {
    KoinContext {
        AppTheme {
            val viewModel: PageViewModel = koinViewModel()
            var currentScreen by remember { mutableStateOf(Screen.Splash) }
            var selectedPage by remember { mutableStateOf<Page?>(null) }
            var selectedProduct by remember { mutableStateOf<Product?>(null) }
            var productToEdit by remember { mutableStateOf<Product?>(null) }
            
            var activeComponentIndex by remember { mutableStateOf<Int?>(null) }
            var previousScreen by remember { mutableStateOf<Screen?>(null) }

            LaunchedEffect(Unit) {
                val token = viewModel.getCurrentUserToken()
                delay(500)
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
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    val maxWidth = if (currentScreen == Screen.Login || currentScreen == Screen.Splash) 600.dp else 900.dp
                    
                    Box(modifier = Modifier.fillMaxHeight().widthIn(max = maxWidth)) {
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
                                    onSharePage = { page -> selectedPage = page; currentScreen = Screen.PublicViewer },
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
                                    onPageChange = { selectedPage = it }, // SINCRONIZA MUDANÇAS
                                    onBack = { currentScreen = Screen.List },
                                    onEditProduct = { product, compIndex ->
                                        productToEdit = product
                                        activeComponentIndex = compIndex
                                        currentScreen = Screen.ProductEditor
                                    }
                                )
                                Screen.PublicViewer -> PageViewerScreen(
                                    page = selectedPage!!, 
                                    onBack = { currentScreen = Screen.List },
                                    onProductClick = { product ->
                                        selectedProduct = product
                                        previousScreen = Screen.PublicViewer
                                        currentScreen = Screen.ProductDetails
                                    }
                                )
                                Screen.ProductDetails -> ProductDetailsScreen(
                                    product = selectedProduct!!,
                                    onBack = { currentScreen = previousScreen ?: Screen.PublicViewer }
                                )
                                Screen.ProductEditor -> ProductEditorScreen(
                                    product = productToEdit,
                                    onSave = { updatedProduct ->
                                        selectedPage = selectedPage?.let { page ->
                                            val newComponents = page.components.toMutableList()
                                            activeComponentIndex?.let { idx ->
                                                if (idx < newComponents.size) {
                                                    val component = newComponents[idx]
                                                    if (component is com.itbenevides.genesys21.domain.model.PageComponent.ProductList) {
                                                        val newProducts = if (productToEdit == null) {
                                                            component.products + updatedProduct
                                                        } else {
                                                            component.products.map { if (it.id == updatedProduct.id) updatedProduct else it }
                                                        }
                                                        newComponents[idx] = component.copy(products = newProducts)
                                                    }
                                                }
                                            }
                                            page.copy(components = newComponents)
                                        }
                                        currentScreen = Screen.WhiteLabel
                                        productToEdit = null
                                        activeComponentIndex = null
                                    },
                                    onBack = { 
                                        currentScreen = Screen.WhiteLabel
                                        productToEdit = null
                                        activeComponentIndex = null
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(strokeWidth = 3.dp)
    }
}
