package com.itbenevides.genesys21

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.domain.model.PageThemeConfig
import com.itbenevides.genesys21.navigation.Route
import com.itbenevides.genesys21.navigation.Router
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.presentation.screens.editor.PageEditorScreen
import com.itbenevides.genesys21.presentation.screens.editor.ProductEditorScreen
import com.itbenevides.genesys21.presentation.screens.list.PageListScreen
import com.itbenevides.genesys21.presentation.screens.login.LoginScreen
import com.itbenevides.genesys21.presentation.screens.viewer.PageViewerScreen
import com.itbenevides.genesys21.presentation.screens.viewer.ProductDetailsScreen
import com.itbenevides.genesys21.presentation.screens.viewer.WhiteLabelScreen
import com.itbenevides.genesys21.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App() {
    KoinContext {
        val viewModel: PageViewModel = koinViewModel()
        val router = remember { Router(viewModel) }
        val currentRoute = router.currentRoute

        val savedCategories by viewModel.allAvailableCategories.collectAsState()
        
        val allCategories by remember(savedCategories, currentRoute) {
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
                
                (savedCategories + currentSessionCategories).distinct().filterNotNull().sorted()
            }
        }

        val themeConfig = when (currentRoute) {
            is Route.WhiteLabel -> currentRoute.page.theme
            is Route.PublicViewer -> currentRoute.page.theme
            is Route.ProductDetails -> {
                val from = currentRoute.fromRoute
                if (from is Route.PublicViewer) from.page.theme else PageThemeConfig.DEFAULT
            }
            else -> PageThemeConfig.DEFAULT
        }

        AppTheme(themeConfig = themeConfig) {
            LaunchedEffect(Unit) {
                router.handleDeepLink()
                onUrlChange {
                    launch { router.handleDeepLink() }
                }
            }

            LaunchedEffect(currentRoute) {
                router.forceSyncUrl()
            }

            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    val maxWidth = if (currentRoute is Route.Login || currentRoute is Route.Splash) 400.dp else 1200.dp
                    
                    Box(modifier = Modifier.fillMaxHeight().widthIn(max = maxWidth)) {
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
                                    onBack = { router.goBack() }
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
