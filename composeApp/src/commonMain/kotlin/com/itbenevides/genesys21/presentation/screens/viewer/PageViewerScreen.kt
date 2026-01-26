package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.navigation.Route
import com.itbenevides.genesys21.navigation.Router
import com.itbenevides.genesys21.ui.theme.AppTheme
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.util.AnalyticsManager
import com.itbenevides.genesys21.ThemeScrollbarEffectWrapper
import com.itbenevides.genesys21.ui.components.appbar.GenesysTopAppBar
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageViewerScreen(
    page: Page, 
    onBack: () -> Unit,
    onProductClick: (Product) -> Unit,
    allAvailableCategories: List<String> = emptyList()
) {
    val router: Router = koinInject()
    var isLoggedIn by remember { mutableStateOf(false) }
    val cartCount by router.viewModel.cartCount.collectAsState()
    
    val hasHistory = remember(router.currentRoute) { router.getHistory().isNotEmpty() }

    // CORREÇÃO: Extrair as categorias dos produtos presentes na página se a lista externa estiver vazia
    val categories = remember(page.components, allAvailableCategories) {
        if (allAvailableCategories.isNotEmpty()) {
            allAvailableCategories
        } else {
            page.components
                .filterIsInstance<PageComponent.ProductList>()
                .flatMap { it.products }
                .map { it.category }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()
        }
    }

    LaunchedEffect(Unit) {
        isLoggedIn = router.viewModel.getCurrentUserToken() != null
        AnalyticsManager.trackPageView("Vitrine Pública - ${page.title}")
    }

    val hasProductList = remember(page.components) {
        page.components.any { it is PageComponent.ProductList }
    }

    AppTheme(themeConfig = page.theme) {
        ThemeScrollbarEffectWrapper()

        var filterQuery by remember { mutableStateOf("") }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                GenesysTopAppBar(
                    title = page.title,
                    onBack = if (hasHistory) { { router.goBack() } } else null,
                    actions = {
                        IconButton(onClick = { 
                            AnalyticsManager.logEvent("open_order_history")
                            router.navigateTo(Route.CustomerOrderHistory(page)) 
                        }) {
                            Icon(Icons.Default.History, "Meus Pedidos", tint = MaterialTheme.colorScheme.primary)
                        }

                        if (isLoggedIn) {
                            IconButton(onClick = { router.navigateTo(Route.PageList) }) {
                                Icon(Icons.Default.Settings, "Administração", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                )
            },
            floatingActionButton = {
                if (hasProductList || cartCount > 0) {
                    BadgedBox(
                        badge = {
                            if (cartCount > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError,
                                    modifier = Modifier.offset(x = (-8).dp, y = 8.dp)
                                ) {
                                    Text(cartCount.toString(), fontSize = 12.sp)
                                }
                            }
                        }
                    ) {
                        ExtendedFloatingActionButton(
                            onClick = { 
                                AnalyticsManager.logEvent("open_cart")
                                router.navigateTo(Route.Cart(page)) 
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            shape = CircleShape,
                            icon = { Icon(Icons.Default.ShoppingCart, "Carrinho") },
                            text = { Text("Ver Carrinho") }
                        )
                    }
                }
            }
        ) { padding ->
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.TopCenter
            ) {
                val maxWidthContent = 1300.dp
                val horizontalPadding = if (maxWidth > maxWidthContent) (maxWidth - maxWidthContent) / 2 else 12.dp

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = horizontalPadding,
                        end = horizontalPadding,
                        top = 8.dp,
                        bottom = 100.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(page.components) { component ->
                        PageComponentRenderer(
                            component = component,
                            onProductClick = onProductClick,
                            filterQuery = filterQuery,
                            onFilterQueryChange = { filterQuery = it },
                            allAvailableCategories = categories // Passando a lista de categorias extraída/corrigida
                        )
                    }
                }
            }
        }
    }
}
