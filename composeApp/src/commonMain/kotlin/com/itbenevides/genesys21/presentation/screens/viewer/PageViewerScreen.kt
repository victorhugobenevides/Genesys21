package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.navigation.Route
import com.itbenevides.genesys21.navigation.Router
import com.itbenevides.genesys21.ui.components.appbar.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.button.GenesysIconButton
import com.itbenevides.genesys21.ui.theme.AppTheme
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.util.Analytics
import org.koin.compose.koinInject

@Composable
fun PageViewerScreen(
    page: Page,
    onBack: () -> Unit,
    onProductClick: (Product) -> Unit
) {
    val router: Router = koinInject()
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(page.id) {
        Analytics.trackPageView("${GenesysStrings.AppName} - ${page.title}")
    }

    AppTheme(themeConfig = page.theme) {
        Scaffold(
            topBar = {
                GenesysTopAppBar(
                    title = page.title,
                    onBack = onBack,
                    actions = {
                        GenesysIconButton(
                            icon = GenesysIcons.List,
                            onClick = { 
                                Analytics.logEvent("open_order_history")
                                router.navigateTo(Route.CustomerOrderHistory) 
                            }
                        )
                        GenesysIconButton(
                            icon = GenesysIcons.ShoppingBag,
                            onClick = { 
                                Analytics.logEvent("open_cart")
                                router.navigateTo(Route.Cart(page)) 
                            }
                        )
                    }
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                itemsIndexed(page.components) { _, component ->
                    PageComponentRenderer(
                        component = component,
                        onProductClick = onProductClick,
                        selectedCategory = selectedCategory,
                        onCategorySelect = { selectedCategory = it }
                    )
                }
            }
        }
    }
}
