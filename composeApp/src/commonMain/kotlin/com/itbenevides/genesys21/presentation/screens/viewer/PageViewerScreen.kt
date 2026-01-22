package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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

    LaunchedEffect(Unit) {
        isLoggedIn = router.viewModel.getCurrentUserToken() != null
    }

    val hasProductList = remember(page.components) {
        page.components.any { it is PageComponent.ProductList }
    }

    AppTheme(themeConfig = page.theme) {
        var filterQuery by remember { mutableStateOf("") }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(page.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                    navigationIcon = {
                        if (hasHistory) {
                            TextButton(onClick = { router.goBack() }) {
                                Text("Voltar", color = MaterialTheme.colorScheme.primary, fontSize = 17.sp)
                            }
                        }
                    },
                    actions = {
                        if (isLoggedIn) {
                            IconButton(onClick = { router.navigateTo(Route.WhiteLabel(page)) }) {
                                Icon(Icons.Default.Settings, "Admin", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
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
                            onClick = { router.navigateTo(Route.Cart(page)) }, // CORREÇÃO: Passando a página
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
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(page.components) { component ->
                        PageComponentRenderer(
                            component = component,
                            onProductClick = onProductClick,
                            filterQuery = filterQuery,
                            onFilterQueryChange = { filterQuery = it },
                            allAvailableCategories = allAvailableCategories
                        )
                    }
                }
            }
        }
    }
}
