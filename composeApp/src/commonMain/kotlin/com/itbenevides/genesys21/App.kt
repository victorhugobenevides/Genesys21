package com.itbenevides.genesys21

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.domain.model.PageThemeConfig
import com.itbenevides.genesys21.navigation.Route
import com.itbenevides.genesys21.navigation.Router
import com.itbenevides.genesys21.presentation.AppError
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.presentation.screens.editor.PageEditorScreen
import com.itbenevides.genesys21.presentation.screens.editor.ProductEditorScreen
import com.itbenevides.genesys21.presentation.screens.list.PageListScreen
import com.itbenevides.genesys21.presentation.screens.login.LoginScreen
import com.itbenevides.genesys21.presentation.screens.viewer.*
import com.itbenevides.genesys21.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App() {
    KoinContext {
        val viewModel: PageViewModel = koinViewModel()
        val router: Router = koinInject()
        val currentRoute = router.currentRoute
        
        val currentError by viewModel.currentError.collectAsState()

        val savedCategories: List<String> by viewModel.allAvailableCategories.collectAsState(initial = emptyList())
        
        val allCategories: List<String> by remember(savedCategories, currentRoute) {
            derivedStateOf {
                val page = when (currentRoute) {
                    is Route.WhiteLabel -> currentRoute.page
                    is Route.PublicViewer -> currentRoute.page
                    is Route.ProductEditor -> currentRoute.page
                    else -> null
                }
                val currentSessionCategories = page?.components
                    ?.filterIsInstance<PageComponent.ProductList>()
                    ?.flatMap { it.products }
                    ?.map { it.category }
                    ?.filter { it.isNotBlank() } ?: emptyList()
                
                (savedCategories + currentSessionCategories).distinct().sorted()
            }
        }

        val themeConfig by remember(currentRoute) {
            derivedStateOf {
                when (currentRoute) {
                    is Route.WhiteLabel -> currentRoute.page.theme
                    is Route.PublicViewer -> currentRoute.page.theme
                    is Route.ProductEditor -> currentRoute.page.theme
                    is Route.ProductDetails -> {
                        val from = currentRoute.fromRoute
                        when (from) {
                            is Route.PublicViewer -> from.page.theme
                            is Route.WhiteLabel -> from.page.theme
                            else -> PageThemeConfig.DEFAULT
                        }
                    }
                    is Route.Cart -> {
                        val lastPage = router.getHistory().reversed().firstNotNullOfOrNull { 
                            when (it) {
                                is Route.PublicViewer -> it.page
                                is Route.WhiteLabel -> it.page
                                is Route.ProductDetails -> {
                                    (it.fromRoute as? Route.PublicViewer)?.page ?: (it.fromRoute as? Route.WhiteLabel)?.page
                                }
                                else -> null
                            }
                        }
                        lastPage?.theme ?: PageThemeConfig.DEFAULT
                    }
                    else -> PageThemeConfig.DEFAULT
                }
            }
        }

        AppTheme(themeConfig = themeConfig) {
            LaunchedEffect(Unit) {
                router.handleDeepLink()
                onUrlChange { launch { router.handleDeepLink() } }
            }

            LaunchedEffect(currentRoute) { router.forceSyncUrl() }

            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                
                currentError?.let { error ->
                    GlobalErrorDialog(error = error, onDismiss = { viewModel.clearError() })
                }

                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    val maxWidth = if (currentRoute is Route.Login || currentRoute is Route.Splash) 400.dp else 1200.dp
                    
                    Box(modifier = Modifier.fillMaxHeight().widthIn(max = maxWidth).padding(horizontal = 16.dp)) {
                        AnimatedContent(targetState = currentRoute) { route ->
                            when (route) {
                                is Route.Splash -> SplashScreen()
                                is Route.Login -> LoginScreen(viewModel, onLoginSuccess = { router.navigateTo(Route.PageList) })
                                is Route.PageList -> PageListScreen(
                                    viewModel = viewModel,
                                    onAddPage = { router.navigateTo(Route.PageEditor()) },
                                    onEditPage = { router.navigateTo(Route.WhiteLabel(it)) },
                                    onViewPage = { router.navigateTo(Route.WhiteLabel(it)) },
                                    onLogout = { viewModel.signOut(); router.navigateTo(Route.Login) }
                                )
                                is Route.PageEditor -> PageEditorScreen(viewModel, route.page, onBack = { router.navigateTo(Route.PageList) })
                                is Route.WhiteLabel -> WhiteLabelScreen(
                                    viewModel = viewModel,
                                    page = route.page,
                                    onPageChange = { router.navigateTo(Route.WhiteLabel(it)) },
                                    onBack = { router.navigateTo(Route.PageList) },
                                    onEditProduct = { prod, idx -> router.navigateTo(Route.ProductEditor(route.page, prod, idx)) }
                                )
                                is Route.PublicViewer -> PageViewerScreen(
                                    page = route.page,
                                    onBack = { router.navigateTo(Route.PageList) },
                                    onProductClick = { router.navigateTo(Route.ProductDetails(it, route)) },
                                    allAvailableCategories = allCategories
                                )
                                is Route.ProductDetails -> ProductDetailsScreen(
                                    product = route.product,
                                    onBack = { router.goBack() },
                                    onNavigateToCart = { 
                                        val whatsapp = (route.fromRoute as? Route.PublicViewer)?.page?.whatsapp 
                                            ?: (route.fromRoute as? Route.WhiteLabel)?.page?.whatsapp
                                        router.navigateTo(Route.Cart(whatsapp))
                                    }
                                )
                                is Route.ProductEditor -> ProductEditorScreen(
                                    viewModel = viewModel,
                                    product = route.product,
                                    existingCategories = allCategories,
                                    onSave = { updatedProduct ->
                                        val newComponents = route.page.components.toMutableList()
                                        route.componentIndex?.let { idx ->
                                            if (idx >= 0 && idx < newComponents.size) {
                                                val component = newComponents[idx] as? PageComponent.ProductList
                                                component?.let {
                                                    val newProducts = if (route.product == null) it.products + updatedProduct
                                                    else it.products.map { p -> if (p.id == updatedProduct.id) updatedProduct else p }
                                                    newComponents[idx] = it.copy(products = newProducts)
                                                }
                                            }
                                        }
                                        router.navigateTo(Route.WhiteLabel(route.page.copy(components = newComponents)))
                                    },
                                    onBack = { router.navigateTo(Route.WhiteLabel(route.page)) }
                                )
                                is Route.Cart -> CartScreen(whatsappNumber = route.whatsapp, onBack = { router.goBack() })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GlobalErrorDialog(error: AppError, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp)) },
        title = { Text(error.title, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(error.message, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                
                error.stackTrace?.let { trace ->
                    Spacer(Modifier.height(16.dp))
                    Surface(
                        color = Color.Black.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = trace,
                            modifier = Modifier.padding(12.dp),
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Entendi")
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun SplashScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(strokeWidth = 3.dp)
    }
}
