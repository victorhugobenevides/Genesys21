package com.itbenevides.genesys21

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import com.itbenevides.genesys21.domain.model.CustomThemeConfig
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.domain.model.PageThemeConfig
import com.itbenevides.genesys21.domain.model.PageTemplateRegistry
import com.itbenevides.genesys21.navigation.Route
import com.itbenevides.genesys21.navigation.Router
import com.itbenevides.genesys21.presentation.screens.SplashScreen
import com.itbenevides.genesys21.presentation.screens.editor.*
import com.itbenevides.genesys21.presentation.screens.list.PageListScreen
import com.itbenevides.genesys21.presentation.screens.login.LoginScreen
import com.itbenevides.genesys21.presentation.screens.profile.ProfileScreen
import com.itbenevides.genesys21.presentation.screens.viewer.*
import com.itbenevides.genesys21.ui.theme.AppTheme
import com.itbenevides.genesys21.ui.util.ProvideWindowSizeClass
import org.koin.compose.koinInject

@Composable
fun App() {
    val router: Router = koinInject()
    val currentRoute = router.currentRoute
    val trackedOrder by router.viewModel.trackedOrder.collectAsState()

    var currentActivePageTheme by remember { mutableStateOf<PageThemeConfig?>(null) }
    var currentActiveCustomTheme by remember { mutableStateOf<CustomThemeConfig?>(null) }

    LaunchedEffect(Unit) {
        router.handleDeepLink()
        onUrlChange { router.handleDeepLink() }
    }

    LaunchedEffect(currentRoute) {
        when (currentRoute) {
            is Route.PublicViewer -> {
                currentActivePageTheme = currentRoute.page.theme
                currentActiveCustomTheme = currentRoute.page.customTheme
            }
            is Route.WhiteLabel -> {
                currentActivePageTheme = currentRoute.page.theme
                currentActiveCustomTheme = currentRoute.page.customTheme
            }
            is Route.ServiceBooking -> {
                currentActivePageTheme = currentRoute.page.theme
                currentActiveCustomTheme = currentRoute.page.customTheme
            }
            is Route.Splash, is Route.Login, is Route.PageList, is Route.Profile -> {
                currentActivePageTheme = null
                currentActiveCustomTheme = null
            }
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

    val customThemeToApply =
        remember(currentRoute, currentActiveCustomTheme) {
            when (currentRoute) {
                is Route.OrderTracking -> null // Por enquanto pedidos não salvam custom colors
                else -> currentActiveCustomTheme
            }
        }

    BoxWithConstraints {
        ProvideWindowSizeClass(maxWidth) {
            AppTheme(themeConfig = themeToApply, customTheme = customThemeToApply) {
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
                                        onShowcase = { router.navigateTo(Route.DesignSystemShowcase) },
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
                                        onEditService = { service, componentIndex ->
                                            router.navigateTo(Route.ServiceEditor(editingPage, service, componentIndex))
                                        }
                                    )
                                }
                                is Route.PublicViewer ->
                                    PageViewerScreen(
                                        page = route.page,
                                        router = router,
                                        onOpenDashboard = { router.navigateTo(Route.Login) },
                                    )
                                is Route.ServiceBooking ->
                                    ServiceBookingScreen(
                                        service = route.service,
                                        page = route.page,
                                        router = router,
                                        viewModel = router.viewModel,
                                    )
                                is Route.ServiceEditor ->
                                    ServiceEditorScreen(
                                        viewModel = router.viewModel,
                                        service = route.service,
                                        onSave = { updatedService ->
                                            router.viewModel.saveBookingService(updatedService) {
                                                val page = route.page
                                                if (page != null) {
                                                    val updatedComponents = page.components.toMutableList()
                                                    val index = route.componentIndex ?: 0
                                                    val comp = updatedComponents.getOrNull(index) as? PageComponent.ServiceList
                                                    if (comp != null) {
                                                        val updatedServices = comp.services.toMutableList()
                                                        val sIndex = updatedServices.indexOfFirst { it.id == updatedService.id }
                                                        if (sIndex != -1) {
                                                            updatedServices[sIndex] = updatedService
                                                        } else {
                                                            updatedServices.add(0, updatedService)
                                                        }

                                                        updatedComponents[index] = comp.copy(services = updatedServices)
                                                        val updatedPage = page.copy(components = updatedComponents)
                                                        router.viewModel.saveDraft(updatedPage)
                                                        router.goBack()
                                                    } else {
                                                        router.goBack()
                                                    }
                                                } else {
                                                    router.goBack()
                                                }
                                            }
                                        },
                                        onBack = { router.goBack() },
                                    )
                                is Route.ServiceSelection ->
                                    ServiceSelectionScreen(
                                        viewModel = router.viewModel,
                                        selectedIds = route.selectedIds,
                                        onConfirm = { selectedIds ->
                                            // Fetch objects for the selected IDs and update component
                                            val allServices = router.viewModel.services.value
                                            val selectedServices = allServices.filter { it.id in selectedIds }

                                            val updatedComponents = route.page.components.toMutableList()
                                            val comp = updatedComponents.getOrNull(route.componentIndex) as? PageComponent.ServiceList
                                            if (comp != null) {
                                                updatedComponents[route.componentIndex] = comp.copy(services = selectedServices)
                                                val updatedPage = route.page.copy(components = updatedComponents)
                                                router.viewModel.saveDraft(updatedPage)
                                                router.goBack()
                                            }
                                        },
                                        onBack = { router.goBack() },
                                        onAddNewService = {
                                            router.navigateTo(Route.ServiceEditor(route.page, null, route.componentIndex))
                                        }
                                    )
                                is Route.ProductDetails ->
                                    ProductDetailsScreen(
                                        product = route.product,
                                        pageId = ((route.fromRoute as? Route.PublicViewer)?.page ?: (route.fromRoute as? Route.WhiteLabel)?.page)?.id,
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
                                    val categoriesNames by router.viewModel.allAvailableCategories.collectAsState()

                                    ProductEditorScreen(
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
                                                router.viewModel.saveDraft(updatedPage)
                                                router.goBack()
                                            }
                                        },
                                        onBack = { router.goBack() },
                                    )
                                }
                                is Route.Profile ->
                                    ProfileScreen(
                                        viewModel = router.viewModel,
                                        router = router
                                    )
                                is Route.DesignSystemShowcase ->
                                    DesignSystemShowcaseScreen(
                                        onBack = { router.goBack() },
                                        onOpenEditorShowcase = { router.navigateTo(Route.EditorShowcase) },
                                        onOpenTemplateShowcase = { router.navigateTo(Route.TemplateShowcase) },
                                    )
                                is Route.EditorShowcase ->
                                    EditorShowcaseScreen(
                                        onBack = { router.goBack() },
                                    )
                                is Route.TemplateShowcase ->
                                    TemplateCatalogScreen(
                                        viewModel = router.viewModel,
                                        onBack = { router.goBack() },
                                        onTemplateSelected = { template ->
                                            val id = (1..8).map { "abcdefghijklmnopqrstuvwxyz0123456789".random() }.joinToString("")
                                            val storeId = "genesys-official-store" // Should come from user context
                                            val newPage = PageTemplateRegistry.createPageFromTemplate(template.id, id, storeId)

                                            router.viewModel.savePage(newPage, false) {
                                                router.navigateTo(Route.WhiteLabel(newPage), replace = true)
                                            }
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
