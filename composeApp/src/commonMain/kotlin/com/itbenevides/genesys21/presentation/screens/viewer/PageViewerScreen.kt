package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    onProductClick: (Product) -> Unit
) {
    val router: Router = koinInject()
    val cartCount by router.viewModel.cartCount.collectAsState()
    
    // 1. State Holder
    var state by remember { mutableStateOf(PageViewerScreenState(page = page)) }
    
    LaunchedEffect(Unit) {
        val isLoggedIn = router.viewModel.getCurrentUserToken() != null
        state = state.copy(
            isLoggedIn = isLoggedIn,
            cartCount = cartCount
        )
        AnalyticsManager.trackPageView("${GenesysStrings.AppName} - ${page.title}")
    }

    LaunchedEffect(cartCount) {
        state = state.copy(cartCount = cartCount)
    }

    // 2. Orquestrador de Eventos
    val onEvent: (PageViewerScreenEvent) -> Unit = { event ->
        when (event) {
            is PageViewerScreenEvent.OnFilterQueryChanged -> state = state.copy(filterQuery = event.query)
            is PageViewerScreenEvent.OnProductClicked -> onProductClick(event.product)
            is PageViewerScreenEvent.OnOpenCartClicked -> {
                AnalyticsManager.logEvent("open_cart")
                router.navigateTo(Route.Cart(state.page))
            }
            is PageViewerScreenEvent.OnOpenHistoryClicked -> {
                AnalyticsManager.logEvent("open_order_history")
                router.navigateTo(Route.CustomerOrderHistory(state.page))
            }
            is PageViewerScreenEvent.OnAdminSettingsClicked -> router.navigateTo(Route.PageList)
            is PageViewerScreenEvent.OnBackClicked -> router.goBack()
        }
    }

    // 3. Renderização sob o tema da página
    AppTheme(themeConfig = state.page.theme) {
        ThemeScrollbarEffectWrapper()
        PageViewerContent(state, onEvent)
    }
}

@Composable
private fun PageViewerContent(
    state: PageViewerScreenState,
    onEvent: (PageViewerScreenEvent) -> Unit
) {
    GenesysPage(
        topBar = {
             GenesysTopAppBar(
                title = state.page.title,
                onBack = { onEvent(PageViewerScreenEvent.OnBackClicked) },
                actions = {
                    GenesysIconButton(
                        icon = GenesysIcons.List, 
                        contentDescription = GenesysStrings.OrderHistoryTitle,
                        onClick = { onEvent(PageViewerScreenEvent.OnOpenHistoryClicked) }
                    )

                    if (state.isLoggedIn) {
                        GenesysIconButton(
                            icon = GenesysIcons.Settings, 
                            contentDescription = GenesysStrings.AdminTitle,
                            onClick = { onEvent(PageViewerScreenEvent.OnAdminSettingsClicked) }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.hasProductList || state.cartCount > 0) {
                BadgedBox(
                    badge = {
                        if (state.cartCount > 0) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError,
                                modifier = Modifier.offset(x = (-8).dp, y = 8.dp)
                            ) {
                                GenesysText(text = state.cartCount.toString(), style = GenesysTextStyle.Label)
                            }
                        }
                    }
                ) {
                    ExtendedFloatingActionButton(
                        onClick = { onEvent(PageViewerScreenEvent.OnOpenCartClicked) },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = androidx.compose.foundation.shape.CircleShape,
                        icon = { Icon(GenesysIcons.ShoppingBag, null) },
                        text = { GenesysText(text = GenesysStrings.ViewCart, style = GenesysTextStyle.Body) }
                    )
                }
            }
        }
    ) {
        // Container Root centralizado
        GenesysColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = GenesysAlignment.Center,
            usePadding = false
        ) {
            // Conteúdo da Vitrine com scroll e largura máxima
            GenesysColumn(
                maxWidth = GenesysDimens.ViewerMaxWidth,
                usePadding = false,
                useScroll = true,
                weightValue = 1f
            ) {
                state.page.components.forEach { component ->
                    PageComponentRenderer(
                        component = component,
                        onProductClick = { onEvent(PageViewerScreenEvent.OnProductClicked(it)) },
                        filterQuery = state.filterQuery,
                        onFilterQueryChange = { onEvent(PageViewerScreenEvent.OnFilterQueryChanged(it)) },
                        allAvailableCategories = state.categories
                    )
                }
                
                // Espaço extra no final para não ficar colado no FAB
                GenesysSpacer(GenesysSpacing.Huge)
            }
        }
    }
}
