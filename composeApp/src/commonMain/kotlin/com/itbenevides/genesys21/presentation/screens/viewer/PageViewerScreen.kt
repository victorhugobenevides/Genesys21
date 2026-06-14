package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.ThemeScrollbarEffectWrapper
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.navigation.Route
import com.itbenevides.genesys21.navigation.Router
import com.itbenevides.genesys21.ui.components.appbar.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.button.GenesysIconButton
import com.itbenevides.genesys21.ui.components.layout.GenesysAlignment
import com.itbenevides.genesys21.ui.components.layout.GenesysColumn
import com.itbenevides.genesys21.ui.components.layout.GenesysPage
import com.itbenevides.genesys21.ui.components.layout.GenesysSpacer
import com.itbenevides.genesys21.ui.components.layout.GenesysSpacing
import com.itbenevides.genesys21.ui.components.text.GenesysText
import com.itbenevides.genesys21.ui.components.text.GenesysTextStyle
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.AppTheme
import com.itbenevides.genesys21.ui.theme.GenesysDimens
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.util.AnalyticsManager
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
            is PageViewerScreenEvent.OnOpenAdminSettingsClicked -> router.navigateTo(Route.PageList)
            is PageViewerScreenEvent.OnBackClicked -> onBack()
        }
    }

    AppTheme(themeConfig = state.page.theme) {
        ThemeScrollbarEffectWrapper()
        PageViewerContent(state.copy(isLoggedIn = isLoggedIn), onEvent)
    }
}

@Composable
private fun PageViewerContent(
    state: PageViewerScreenState,
    onEvent: (PageViewerScreenEvent) -> Unit,
) {
    var currentFilterQuery by remember { mutableStateOf("") }

    GenesysPage(
        topBar = {
            GenesysTopAppBar(
                title = state.page.title,
                onBack = { onEvent(PageViewerScreenEvent.OnBackClicked) },
                actions = {
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
            // BOTÃO CARRINHO: Apenas se houver lista de produtos ou itens já no carrinho
            if (state.hasProductList || state.cartCount > 0) {
                BadgedBox(
                    badge = {
                        if (state.cartCount > 0) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError,
                                modifier = Modifier.offset(x = (-8).dp, y = 8.dp),
                            ) {
                                GenesysText(text = state.cartCount.toString(), style = GenesysTextStyle.Label)
                            }
                        }
                    },
                ) {
                    ExtendedFloatingActionButton(
                        onClick = { onEvent(PageViewerScreenEvent.OnOpenCartClicked) },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = androidx.compose.foundation.shape.CircleShape,
                        icon = { Icon(GenesysIcons.ShoppingBag, null) },
                        text = { GenesysText(text = GenesysStrings.ViewCart, style = GenesysTextStyle.Body) },
                    )
                }
            }
        },
    ) {
        GenesysColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = GenesysAlignment.Center,
            usePadding = false,
        ) {
            GenesysColumn(
                maxWidth = GenesysDimens.ViewerMaxWidth,
                usePadding = false,
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
                    )
                }

                GenesysSpacer(GenesysSpacing.Huge)
            }
        }
    }
}
