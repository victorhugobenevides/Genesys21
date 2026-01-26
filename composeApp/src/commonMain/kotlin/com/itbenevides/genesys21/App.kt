package com.itbenevides.genesys21

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.PageThemeConfig
import com.itbenevides.genesys21.domain.model.Order
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.navigation.Route
import com.itbenevides.genesys21.navigation.Router
import com.itbenevides.genesys21.presentation.screens.SplashScreen
import com.itbenevides.genesys21.presentation.screens.login.LoginScreen
import com.itbenevides.genesys21.presentation.screens.editor.PageEditorScreen
import com.itbenevides.genesys21.presentation.screens.list.PageListScreen
import com.itbenevides.genesys21.presentation.screens.viewer.CartScreen
import com.itbenevides.genesys21.presentation.screens.viewer.CustomerOrderHistoryScreen
import com.itbenevides.genesys21.presentation.screens.viewer.OrderTrackingScreen
import com.itbenevides.genesys21.presentation.screens.viewer.PageViewerScreen
import com.itbenevides.genesys21.presentation.screens.viewer.ProductDetailsScreen
import com.itbenevides.genesys21.presentation.screens.viewer.WhiteLabelScreen
import com.itbenevides.genesys21.ui.theme.AppTheme
import org.koin.compose.koinInject

@Composable
fun App() {
    val router: Router = koinInject()
    val currentRoute = router.currentRoute
    val trackedOrder by router.viewModel.trackedOrder.collectAsState()
    
    var historySelectedOrderTheme by remember { mutableStateOf<PageThemeConfig?>(null) }

    LaunchedEffect(Unit) {
        router.handleDeepLink()
        onUrlChange { router.handleDeepLink() }
    }

    LaunchedEffect(currentRoute) {
        if (currentRoute !is Route.OrderTracking) {
            historySelectedOrderTheme = null
        }
    }

    val currentTheme: PageThemeConfig = remember(currentRoute, trackedOrder, historySelectedOrderTheme) {
        val theme = when (currentRoute) {
            is Route.PublicViewer -> currentRoute.page.theme
            is Route.WhiteLabel -> currentRoute.page.theme
            is Route.ProductDetails -> {
                (currentRoute.fromRoute as? Route.PublicViewer)?.page?.theme 
                    ?: (currentRoute.fromRoute as? Route.WhiteLabel)?.page?.theme 
            }
            is Route.Cart -> currentRoute.page?.theme
            is Route.OrderTracking -> {
                val isCorrectOrder = trackedOrder?.id == currentRoute.orderId
                if (isCorrectOrder) trackedOrder?.theme else historySelectedOrderTheme
            }
            is Route.CustomerOrderHistory -> {
                val lastViewer = router.getHistory().filterIsInstance<Route.PublicViewer>().lastOrNull()
                lastViewer?.page?.theme
            }
            else -> PageThemeConfig.ROYAL
        }
        theme ?: PageThemeConfig.ROYAL
    }

    AppTheme(themeConfig = currentTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize()) {
                AnimatedContent(
                    targetState = currentRoute,
                    transitionSpec = {
                        if (targetState is Route.Splash || initialState is Route.Splash) {
                            EnterTransition.None togetherWith ExitTransition.None
                        } else {
                            fadeIn() togetherWith fadeOut()
                        }
                    },
                    label = "AppNavigation"
                ) { route ->
                    when (route) {
                        is Route.Splash -> SplashScreen()
                        is Route.Login -> LoginScreen(
                            viewModel = router.viewModel,
                            onLoginSuccess = { router.navigateTo(Route.PageList) }
                        )
                        is Route.PageList -> PageListScreen(
                            viewModel = router.viewModel,
                            onAddPage = { router.navigateTo(Route.PageEditor(null)) },
                            onEditPage = { router.navigateTo(Route.WhiteLabel(it)) },
                            onViewPage = { router.navigateTo(Route.PublicViewer(it)) },
                            onLogout = { 
                                router.viewModel.signOut()
                                router.navigateTo(Route.Login) 
                            }
                        )
                        is Route.PageEditor -> PageEditorScreen(
                            viewModel = router.viewModel,
                            page = route.page,
                            onBack = { router.goBack() }
                        )
                        is Route.WhiteLabel -> {
                            var editingPage by remember(route.page) { mutableStateOf(route.page) }
                            
                            AppTheme(themeConfig = editingPage.theme) {
                                WhiteLabelScreen(
                                    viewModel = router.viewModel,
                                    page = editingPage,
                                    onPageChange = { editingPage = it }, 
                                    onBack = { router.goBack() },
                                    onEditProduct = { product, componentIndex -> 
                                        router.navigateTo(Route.ProductEditor(editingPage, product, componentIndex))
                                    }
                                )
                            }
                        }
                        is Route.PublicViewer -> PageViewerScreen(
                            page = route.page,
                            onBack = { router.goBack() },
                            onProductClick = { router.navigateTo(Route.ProductDetails(it, route)) }
                        )
                        is Route.ProductDetails -> ProductDetailsScreen(
                            product = route.product,
                            onBack = { router.goBack() },
                            onNavigateToCart = { 
                                val page = (route.fromRoute as? Route.PublicViewer)?.page 
                                    ?: (route.fromRoute as? Route.WhiteLabel)?.page
                                router.navigateTo(Route.Cart(page)) 
                            }
                        )
                        is Route.Cart -> CartScreen(
                            page = route.page,
                            onBack = { router.goBack() },
                            onOrderSubmitted = { orderId ->
                                router.navigateTo(Route.OrderTracking(orderId), replace = true)
                            }
                        )
                        is Route.OrderTracking -> OrderTrackingScreen(
                            orderId = route.orderId,
                            onBack = { router.goBack() }
                        )
                        is Route.CustomerOrderHistory -> CustomerOrderHistoryScreen(
                            onBack = { router.goBack() },
                            onOrderClick = { order ->
                                historySelectedOrderTheme = order.theme
                                router.navigateTo(Route.OrderTracking(order.id))
                            }
                        )
                        is Route.ProductEditor -> {
                            val categories by router.viewModel.allAvailableCategories.collectAsState()
                            com.itbenevides.genesys21.presentation.screens.editor.ProductEditorScreen(
                                viewModel = router.viewModel,
                                page = route.page,
                                product = route.product,
                                existingCategories = categories,
                                onSave = { updatedProduct ->
                                    val updatedComponents = route.page.components.toMutableList()
                                    val index = route.componentIndex ?: 0
                                    val comp = updatedComponents.getOrNull(index) as? PageComponent.ProductList
                                    if (comp != null) {
                                        val updatedProducts = comp.products.toMutableList()
                                        val pIndex = updatedProducts.indexOfFirst { it.id == updatedProduct.id }
                                        if (pIndex != -1) {
                                            updatedProducts[pIndex] = updatedProduct
                                        } else {
                                            updatedProducts.add(0, updatedProduct) 
                                        }
                                        
                                        updatedComponents[index] = comp.copy(products = updatedProducts)
                                        val updatedPage = route.page.copy(components = updatedComponents)
                                        
                                        // CORREÇÃO: Primeiro voltamos para a WhiteLabel original e depois a substituímos
                                        // Isso remove o editor do histórico e evita duplicidade da WhiteLabel.
                                        router.goBack() 
                                        router.navigateTo(Route.WhiteLabel(updatedPage), replace = true)
                                    }
                                },
                                onBack = { router.goBack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
