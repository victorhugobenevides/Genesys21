package com.itbenevides.genesys21

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.domain.model.PageThemeConfig
import com.itbenevides.genesys21.domain.model.PageTemplateRegistry
import com.itbenevides.genesys21.navigation.Route
import com.itbenevides.genesys21.navigation.Router
import com.itbenevides.genesys21.presentation.UiEvent
import com.itbenevides.genesys21.presentation.components.auth.AccountLinkingDialog
import com.itbenevides.genesys21.presentation.screens.SplashScreen
import com.itbenevides.genesys21.presentation.screens.editor.*
import com.itbenevides.genesys21.presentation.screens.list.PageListScreen
import com.itbenevides.genesys21.presentation.screens.login.LoginScreen
import com.itbenevides.genesys21.presentation.screens.profile.ProfileScreen
import com.itbenevides.genesys21.presentation.screens.viewer.*
import com.itbenevides.genesys21.ui.theme.AppTheme
import com.itbenevides.genesys21.ui.theme.GenesysTheme
import com.itbenevides.genesys21.ui.util.ProvideWindowSizeClass
import org.koin.compose.koinInject

@OptIn(ExperimentalAnimationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun App() {
    val router: Router = koinInject()
    val currentRoute = router.currentRoute

    val snackbarHostState = remember { SnackbarHostState() }
    var accountLinkingEmail by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(router.viewModel) {
        launch {
            router.viewModel.errorEvents.collect { error ->
                snackbarHostState.showSnackbar(
                    message = "${error.title}: ${error.message}",
                    actionLabel = "OK",
                    duration = SnackbarDuration.Long
                )
            }
        }
        launch {
            router.viewModel.uiMessages.collect { message ->
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
            }
        }
        launch {
            router.viewModel.uiEvent.collect { event ->
                when (event) {
                    is UiEvent.ShowAccountLinkingDialog -> {
                        accountLinkingEmail = event.email
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        router.handleDeepLink()
        onUrlChange { router.handleDeepLink() }
    }

    val useDynamicColor =
        remember(currentRoute) {
            when (currentRoute) {
                is Route.PageList, is Route.Profile, is Route.EditorShowcase, is Route.DesignSystemShowcase, is Route.Receipts -> true
                else -> false
            }
        }

    val globalAppTheme by router.viewModel.appTheme.collectAsState()

    BoxWithConstraints {
        ProvideWindowSizeClass(maxWidth) {
            // UNIFICAÇÃO DE TEMA: O appTheme soberano controla toda a experiência.
            // CustomTheme é nulo aqui pois o portal administrativo usa os tokens oficiais.
            AppTheme(themeConfig = globalAppTheme, customTheme = null, useDynamicColor = useDynamicColor) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    content = { padding ->
                        Surface(
                            modifier = Modifier.fillMaxSize().padding(padding),
                            color = GenesysTheme.colors.background,
                        ) {
                            SharedTransitionLayout {
                                AnimatedContent(
                                    targetState = currentRoute,
                                    transitionSpec = {
                                        if (targetState is Route.Splash || initialState is Route.Splash) {
                                            EnterTransition.None togetherWith ExitTransition.None
                                        } else {
                                            val duration = com.itbenevides.genesys21.ui.theme.GenesysMotion.DurationMedium4
                                            val easing = com.itbenevides.genesys21.ui.theme.GenesysMotion.Emphasized

                                            (slideInHorizontally(
                                                animationSpec = tween(duration, easing = easing),
                                                initialOffsetX = { it / 10 }
                                            ) + fadeIn(animationSpec = tween(duration))).togetherWith(
                                                slideOutHorizontally(
                                                    animationSpec = tween(duration, easing = easing),
                                                    targetOffsetX = { -it / 10 }
                                                ) + fadeOut(animationSpec = tween(duration))
                                            )
                                        }
                                    },
                                    label = "GlobalNavigation",
                                ) { route ->
                                    CompositionLocalProvider(
                                        LocalSharedTransitionScope provides this@SharedTransitionLayout,
                                        LocalAnimatedContentScope provides this@AnimatedContent
                                    ) {
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
                                                        status = route.status,
                                                        onBack = { router.goBack() },
                                                    )
                                                is Route.CustomerOrderHistory ->
                                                    CustomerOrderHistoryScreen(
                                                        status = router.currentUrlParameters.split("status=").getOrNull(1)?.split("&")?.getOrNull(0),
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
                                                is Route.Receipts -> {
                                                    com.itbenevides.genesys21.presentation.receipt.ReceiptListScreen(
                                                        viewModel = koinInject(),
                                                        onOpenUrl = { url -> com.itbenevides.genesys21.openUrlInNewTab(url) }
                                                    )
                                                }
                                            }
                                        }

                                        accountLinkingEmail?.let { email ->
                                            AccountLinkingDialog(
                                                email = email,
                                                onDismiss = { accountLinkingEmail = null },
                                                onLoginClick = {
                                                    accountLinkingEmail = null
                                                    router.navigateTo(Route.Login)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }
val LocalAnimatedContentScope = staticCompositionLocalOf<AnimatedContentScope?> { null }
