package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.ThemeScrollbarEffectWrapper
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.getWebBaseUrl
import com.itbenevides.genesys21.navigation.Route
import com.itbenevides.genesys21.navigation.Router
import com.itbenevides.genesys21.ui.components.appbar.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.button.GenesysIconButton
import com.itbenevides.genesys21.ui.components.layout.*
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.AppTheme
import com.itbenevides.genesys21.ui.theme.GenesysDimens
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.ui.util.pulse
import com.itbenevides.genesys21.util.AnalyticsManager
import com.itbenevides.genesys21.util.ShareManagerInstance
import org.koin.compose.koinInject

@Composable
fun PageViewerScreen(
    page: Page,
    onBack: () -> Unit,
    onProductClick: (Product) -> Unit,
) {
    val router: Router = koinInject()
    val cartCount by router.viewModel.cartCount.collectAsState()
    val storeCategories by router.viewModel.allAvailableCategories.collectAsState()
    val allProducts by router.viewModel.allAvailableProducts.collectAsState()

    val state =
        remember(page, cartCount, storeCategories) {
            PageViewerScreenState(
                page = page,
                cartCount = cartCount,
                allStoreCategories = storeCategories,
            )
        }

    var isLoggedIn by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoggedIn = router.viewModel.getCurrentUserToken() != null
        AnalyticsManager.trackPageView("${GenesysStrings.AppName} - ${page.title}")
    }

    val onEvent: (PageViewerScreenEvent) -> Unit = { event ->
        when (event) {
            is PageViewerScreenEvent.OnFilterQueryChanged -> { }
            is PageViewerScreenEvent.OnProductClicked -> onProductClick(event.product)
            is PageViewerScreenEvent.OnOpenCartClicked -> {
                AnalyticsManager.logEvent("open_cart")
                router.navigateTo(Route.Cart(state.page))
            }
            is PageViewerScreenEvent.OnOpenHistoryClicked -> {
                AnalyticsManager.logEvent("open_order_history")
                router.navigateTo(Route.CustomerOrderHistory(state.page))
            }
            is PageViewerScreenEvent.OnShareClicked -> {
                val url = "${getWebBaseUrl()}/p/${state.page.id}"
                AnalyticsManager.logEvent("share_page", mapOf("page_id" to state.page.id))
                ShareManagerInstance.shareLink(
                    title = state.page.title,
                    text = "Confira esta vitrine incrível!",
                    url = url,
                )
            }
            is PageViewerScreenEvent.OnOpenAdminSettingsClicked -> router.navigateTo(Route.PageList)
            is PageViewerScreenEvent.OnBackClicked -> onBack()
        }
    }

    AppTheme(themeConfig = state.page.theme) {
        ThemeScrollbarEffectWrapper()
        PageViewerContent(
            state = state.copy(isLoggedIn = isLoggedIn),
            allProducts = allProducts,
            onEvent = onEvent,
        )
    }
}

@Composable
private fun PageViewerContent(
    state: PageViewerScreenState,
    allProducts: List<Product>,
    onEvent: (PageViewerScreenEvent) -> Unit,
) {
    var currentFilterQuery by remember { mutableStateOf("") }

    GenesysPage(
        topBar = {
            GenesysTopAppBar(
                title = state.page.title,
                onBack = { onEvent(PageViewerScreenEvent.OnBackClicked) },
                actions = {
                    // T024: Botão de Compartilhamento Nativo
                    GenesysIconButton(
                        icon = GenesysIcons.Share,
                        contentDescription = "Compartilhar",
                        onClick = { onEvent(PageViewerScreenEvent.OnShareClicked) },
                    )

                    // BOTÃO CARRINHO: Sempre visível no topo direito
                    BadgedBox(
                        badge = {
                            if (state.cartCount > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.offset(x = (-4).dp, y = 4.dp),
                                ) {
                                    Text(text = state.cartCount.toString())
                                }
                            }
                        },
                        modifier =
                            Modifier
                                .padding(end = 8.dp)
                                .then(if (state.cartCount > 0) Modifier.pulse() else Modifier),
                    ) {
                        GenesysIconButton(
                            icon = GenesysIcons.ShoppingBag,
                            contentDescription = GenesysStrings.ViewCart,
                            onClick = { onEvent(PageViewerScreenEvent.OnOpenCartClicked) },
                        )
                    }

                    // BOTÃO MEUS PEDIDOS: Apenas se houver lista de produtos na página
                    if (state.hasProductList) {
                        GenesysIconButton(
                            icon = GenesysIcons.List,
                            contentDescription = GenesysStrings.OrderHistoryTitle,
                            onClick = { onEvent(PageViewerScreenEvent.OnOpenHistoryClicked) },
                        )
                    }

                    if (state.isLoggedIn) {
                        GenesysIconButton(
                            icon = GenesysIcons.Settings,
                            contentDescription = GenesysStrings.AdminTitle,
                            onClick = { onEvent(PageViewerScreenEvent.OnOpenAdminSettingsClicked) },
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            // Removido do FAB, agora reside na TopBar
        },
    ) {
        GenesysColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = GenesysAlignment.Center,
            usePadding = false,
        ) {
            GenesysColumn(
                maxWidth = GenesysDimens.ViewerMaxWidth,
                usePadding = true,
                useScroll = true,
                weightValue = 1f,
            ) {
                state.page.components.forEach { component ->
                    PageComponentRenderer(
                        component = component,
                        onProductClick = { onEvent(PageViewerScreenEvent.OnProductClicked(it)) },
                        filterQuery = currentFilterQuery,
                        onFilterQueryChange = { currentFilterQuery = it },
                        allAvailableCategories = state.allStoreCategories,
                        allProducts = allProducts,
                    )
                }

                GenesysSpacer(GenesysSpacing.Huge)
            }
        }
    }
}
