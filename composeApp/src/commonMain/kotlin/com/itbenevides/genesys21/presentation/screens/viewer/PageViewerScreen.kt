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
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.navigation.Route
import com.itbenevides.genesys21.navigation.Router
import com.itbenevides.genesys21.ui.theme.AppTheme
import com.itbenevides.genesys21.domain.model.PageComponent
import org.koin.compose.koinInject

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

    LaunchedEffect(Unit) {
        isLoggedIn = router.viewModel.getCurrentUserToken() != null
    }

    // Verifica se a página tem algum componente de lista de produtos
    val hasProductList = remember(page.components) {
        page.components.any { it is PageComponent.ProductList }
    }

    AppTheme(themeConfig = page.theme) {
        var filterQuery by remember { mutableStateOf("") }

        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
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

            // Barra de Ação Superior (Admin à esquerda, Carrinho à direita)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LADO ESQUERDO: Ícone de Admin (Sutil)
                Box(modifier = Modifier.size(48.dp)) {
                    if (isLoggedIn) {
                        IconButton(
                            onClick = { router.navigateTo(Route.WhiteLabel(page)) },
                            modifier = Modifier.alpha(0.3f)
                        ) {
                            Icon(Icons.Default.Settings, "Configurações")
                        }
                    }
                }

                // LADO DIREITO: Ícone do Carrinho com Contador
                if (hasProductList || cartCount > 0) {
                    BadgedBox(
                        badge = { 
                            if (cartCount > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                ) { 
                                    Text(cartCount.toString()) 
                                } 
                            }
                        },
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        IconButton(onClick = { router.navigateTo(Route.Cart(page.whatsapp)) }) {
                            Icon(Icons.Default.ShoppingCart, "Carrinho")
                        }
                    }
                }
            }
        }
    }
}
