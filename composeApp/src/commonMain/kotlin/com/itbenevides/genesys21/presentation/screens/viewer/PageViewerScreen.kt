package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.navigation.Route
import com.itbenevides.genesys21.navigation.Router
import com.itbenevides.genesys21.ui.theme.AppTheme
import com.itbenevides.genesys21.util.AnalyticsManager
import com.itbenevides.genesys21.ThemeScrollbarEffectWrapper
import com.itbenevides.genesys21.ui.components.appbar.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.layout.*
import com.itbenevides.genesys21.ui.theme.GenesysDimens
import com.itbenevides.genesys21.ui.theme.GenesysStrings
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
        AnalyticsManager.trackPageView("Vitrine Pública - ${page.title}")
    }

    LaunchedEffect(cartCount) {
        state = state.copy(cartCount = cartCount)
    }

    // 2. Event Orchestrator
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

    // 3. Render
    PageViewerContent(state, onEvent)
}

@Composable
private fun PageViewerContent(
    state: PageViewerScreenState,
    onEvent: (PageViewerScreenEvent) -> Unit
) {
    AppTheme(themeConfig = state.page.theme) {
        ThemeScrollbarEffectWrapper()

        GenesysPage(
            topBar = {
                GenesysTopAppBar(
                    title = state.page.title,
                    onBack = { onEvent(PageViewerScreenEvent.OnBackClicked) },
                    actions = {
                        IconButton(onClick = { onEvent(PageViewerScreenEvent.OnOpenHistoryClicked) }) {
                            Icon(Icons.Default.History, "Meus Pedidos", tint = MaterialTheme.colorScheme.primary)
                        }

                        if (state.isLoggedIn) {
                            IconButton(onClick = { onEvent(PageViewerScreenEvent.OnAdminSettingsClicked) }) {
                                Icon(Icons.Default.Settings, "Administração", tint = MaterialTheme.colorScheme.primary)
                            }
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
                                    Text(state.cartCount.toString(), fontSize = 12.sp)
                                }
                            }
                        }
                    ) {
                        ExtendedFloatingActionButton(
                            onClick = { onEvent(PageViewerScreenEvent.OnOpenCartClicked) },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            shape = androidx.compose.foundation.shape.CircleShape,
                            icon = { Icon(Icons.Default.ShoppingCart, "Carrinho") },
                            text = { Text("Ver Carrinho") }
                        )
                    }
                }
            }
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                GenesysColumn(
                    maxWidth = GenesysDimens.ViewerMaxWidth,
                    usePadding = false,
                    useScroll = true
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
                    
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}
