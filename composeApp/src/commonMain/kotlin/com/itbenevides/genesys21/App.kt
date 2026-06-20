package com.itbenevides.genesys21

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.domain.model.PageThemeConfig
import com.itbenevides.genesys21.navigation.Route
import com.itbenevides.genesys21.navigation.Router
import com.itbenevides.genesys21.presentation.screens.SplashScreen
import com.itbenevides.genesys21.presentation.screens.editor.PageEditorScreen
import com.itbenevides.genesys21.presentation.screens.list.PageListScreen
import com.itbenevides.genesys21.presentation.screens.login.LoginScreen
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

    var currentActivePageTheme by remember { mutableStateOf<PageThemeConfig?>(null) }

    LaunchedEffect(Unit) {
        router.handleDeepLink()
        onUrlChange { router.handleDeepLink() }
    }

    LaunchedEffect(currentRoute) {
        when (currentRoute) {
            is Route.PublicViewer -> currentActivePageTheme = currentRoute.page.theme
            is Route.WhiteLabel -> currentActivePageTheme = currentRoute.page.theme
            is Route.Splash, is Route.Login, is Route.PageList -> currentActivePageTheme = null
            else -> { }
        }
    }

    val themeToApply =
        remember(currentRoute, trackedOrder, currentActivePageTheme) {
            when (currentRoute) {
                is Route.OrderTracking -> trackedOrder?.theme ?: PageThemeConfig.ROYAL
                else -> currentActivePageTheme ?: PageThemeConfig.ROYAL
            }
        }

    AppTheme(themeConfig = themeToApply) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = androidx.compose.material3.MaterialTheme.colorScheme.background,
        ) {
            AnimatedContent(
                targetState = currentRoute,
                transitionSpec = {
                    if (targetState is Route.Splash || initialState is Route.Splash) {
                        EnterTransition.None togetherWith ExitTransition.None
                    } else {
                        fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
                    }
                },
                label = "GlobalNavigation",
            ) { route ->
                Box(Modifier.fillMaxSize()) {
                    when (route) {
                        is Route.Splash -> SplashScreen()
                        is Route.Login ->
                            LoginScreen(
                                viewModel = router.viewModel,
                                onLoginSuccess = { router.navigateTo(Route.PageList) },
                            )
                        is Route.PageList ->
                            PageListScreen(
                                viewModel = router.viewModel,
                                onAddPage = { router.navigateTo(Route.PageEditor(null)) },
                                onEditPage = { router.navigateTo(Route.WhiteLabel(it)) },
                                onViewPage = { router.navigateTo(Route.PublicViewer(it)) },
                                onLogout = {
                                    router.viewModel.signOut()
                                    router.navigateTo(Route.Login)
                                },
                            )
                        is Route.PageEditor ->
                            PageEditorScreen(
                                viewModel = router.viewModel,
                                page = route.page,
                                onBack = { router.goBack() },
                            )
                        is Route.WhiteLabel -> {
                            var editingPage by remember(route.page) { mutableStateOf(route.page) }
                            WhiteLabelScreen(
                                viewModel = router.viewModel,
                                page = editingPage,
                                onPageChange = { editingPage = it },
                                onBack = { router.goBack() },
                                onEditProduct = { product, componentIndex ->
                                    router.navigateTo(Route.ProductEditor(editingPage, product, componentIndex))
                                },
                            )
                        }
                        is Route.PublicViewer ->
                            PageViewerScreen(
                                page = route.page,
                                onBack = { router.goBack() },
                                onProductClick = { router.navigateTo(Route.ProductDetails(it, route)) },
                            )
                        is Route.ProductDetails ->
                            ProductDetailsScreen(
                                product = route.product,
                                whatsapp = ((route.fromRoute as? Route.PublicViewer)?.page ?: (route.fromRoute as? Route.WhiteLabel)?.page)?.whatsapp,
                                onBack = { router.goBack() },
                                onNavigateToCart = {
                                    val page =
                                        (route.fromRoute as? Route.PublicViewer)?.page
                                            ?: (route.fromRoute as? Route.WhiteLabel)?.page
                                    router.navigateTo(Route.Cart(page))
                                },
                            )
                        is Route.Cart ->
                            CartScreen(
                                page = route.page,
                                onBack = { router.goBack() },
                                onOrderSubmitted = { orderId ->
                                    router.navigateTo(Route.OrderTracking(orderId), replace = true)
                                },
                            )
                        is Route.OrderTracking ->
                            OrderTrackingScreen(
                                orderId = route.orderId,
                                onBack = { router.goBack() },
                            )
                        is Route.CustomerOrderHistory ->
                            CustomerOrderHistoryScreen(
                                onBack = { router.goBack() },
                                onOrderClick = { order ->
                                    router.navigateTo(Route.OrderTracking(order.id))
                                },
                            )
                        is Route.ProductEditor -> {
                            // CORREÇÃO: Usa a lista global de nomes para o dropdown
                            val categoriesNames by router.viewModel.allAvailableCategories.collectAsState()

                            com.itbenevides.genesys21.presentation.screens.editor.ProductEditorScreen(
                                viewModel = router.viewModel,
                                page = route.page,
                                product = route.product,
                                existingCategories = categoriesNames,
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

                                        // IMPORTANTE: Atualiza o rascunho local antes de voltar
                                        router.viewModel.saveDraft(updatedPage)

                                        router.goBack()
                                        // Não usamos navigateTo WhiteLabel aqui para evitar loops de estado,
                                        // o goBack + saveDraft cuidam de tudo.
                                    }
                                },
                                onBack = { router.goBack() },
                            )
                        }
                    }
                }
            }
        }
    }
}
