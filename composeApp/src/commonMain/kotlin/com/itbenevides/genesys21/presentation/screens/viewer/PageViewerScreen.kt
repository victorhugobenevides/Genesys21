package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.BrandingEffects
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.getWebBaseUrl
import com.itbenevides.genesys21.navigation.Route
import com.itbenevides.genesys21.navigation.Router
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysIconButton
import com.itbenevides.genesys21.ui.components.atoms.primitives.*
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.organisms.navigation.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.templates.pages.GenesysPage
import com.itbenevides.genesys21.ui.theme.AppTheme
import com.itbenevides.genesys21.ui.theme.GenesysDimens
import com.itbenevides.genesys21.ui.util.GenesysWindowSizeClass
import com.itbenevides.genesys21.ui.util.LocalWindowSizeClass
import com.itbenevides.genesys21.util.AnalyticsManager
import com.itbenevides.genesys21.util.ShareManagerInstance
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PageViewerScreen(
    page: Page,
    router: Router,
    onOpenDashboard: () -> Unit,
) {
    val viewModel: PageViewModel = koinViewModel()
    val allProducts by viewModel.allAvailableProducts.collectAsState()
    val allServices by viewModel.services.collectAsState()
    val allCategories by viewModel.allAvailableCategories.collectAsState()
    val cartCount by viewModel.cartCount.collectAsState()

    var state by remember { mutableStateOf(PageViewerScreenState(page)) }
    var currentFilterQuery by remember { mutableStateOf("") }
    val windowSizeClass = LocalWindowSizeClass.current
    val isCompact = windowSizeClass == GenesysWindowSizeClass.COMPACT

    LaunchedEffect(page) {
        state = state.copy(page = page)
    }

    val onEvent: (PageViewerScreenEvent) -> Unit = { event ->
        when (event) {
            is PageViewerScreenEvent.OnBackClicked -> router.goBack()
            is PageViewerScreenEvent.OnProductClicked -> {
                AnalyticsManager.logEvent("product_view", mapOf("product_id" to event.product.id))
                router.navigateTo(Route.ProductDetails(event.product, fromRoute = Route.PublicViewer(page)))
            }
            is PageViewerScreenEvent.OnServiceClicked -> {
                AnalyticsManager.logEvent("service_view", mapOf("service_id" to event.service.id))
                router.navigateTo(Route.ServiceBooking(event.service, page))
            }
            is PageViewerScreenEvent.OnShareClicked -> {
                val url = "${getWebBaseUrl()}/page/${state.page.id}"
                ShareManagerInstance.shareLink(
                    title = state.page.title,
                    text = "Confira minha vitrine: ${state.page.title}",
                    url = url,
                )
            }
            is PageViewerScreenEvent.OnOpenAdminSettingsClicked -> onOpenDashboard()
            is PageViewerScreenEvent.OnOpenCartClicked -> router.navigateTo(Route.Cart(state.page))
            is PageViewerScreenEvent.OnOpenHistoryClicked -> router.navigateTo(Route.CustomerOrderHistory(state.page))
            is PageViewerScreenEvent.OnOpenProfileClicked -> router.navigateTo(Route.Profile)
            is PageViewerScreenEvent.OnFilterQueryChanged -> {
                currentFilterQuery = event.query
            }
        }
    }

    BrandingEffects(state.page)

    AppTheme(themeConfig = state.page.theme, customTheme = state.page.customTheme) {
        PageViewerContent(
            state = state,
            currentFilterQuery = currentFilterQuery,
            isCompact = isCompact,
            cartCount = cartCount,
            allProducts = allProducts,
            allServices = allServices,
            allCategories = allCategories,
            onEvent = onEvent
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageViewerContent(
    state: PageViewerScreenState,
    currentFilterQuery: String,
    isCompact: Boolean,
    cartCount: Int = 0,
    allProducts: List<com.itbenevides.genesys21.domain.model.Product> = emptyList(),
    allServices: List<com.itbenevides.genesys21.domain.model.BookingService> = emptyList(),
    allCategories: List<String> = emptyList(),
    onEvent: (PageViewerScreenEvent) -> Unit,
) {
    GenesysPage(
        topBar = {
             GenesysTopAppBar(
                title = if (isCompact) state.page.title.take(15).let { if (it.length < state.page.title.length) "$it..." else it } else state.page.title,
                onBack = { onEvent(PageViewerScreenEvent.OnBackClicked) },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        // COMPARTILHAR
                        FilledTonalIconButton(
                            onClick = { onEvent(PageViewerScreenEvent.OnShareClicked) },
                            modifier = Modifier.size(40.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Icon(GenesysIcons.Share, null, modifier = Modifier.size(20.dp))
                        }

                        // HISTÓRICO / MEUS PEDIDOS
                        FilledTonalIconButton(
                            onClick = { onEvent(PageViewerScreenEvent.OnOpenHistoryClicked) },
                            modifier = Modifier.size(40.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Icon(GenesysIcons.History, null, modifier = Modifier.size(20.dp))
                        }

                        // PERFIL
                        FilledTonalIconButton(
                            onClick = { onEvent(PageViewerScreenEvent.OnOpenProfileClicked) },
                            modifier = Modifier.size(40.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Icon(GenesysIcons.Person, null, modifier = Modifier.size(20.dp))
                        }

                        // CARRINHO (Sempre visível e sugestivo)
                        BadgedBox(
                            badge = {
                                if (cartCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.offset(x = (-4).dp, y = 4.dp)
                                    ) { Text(cartCount.toString()) }
                                }
                            }
                        ) {
                            Button(
                                onClick = { onEvent(PageViewerScreenEvent.OnOpenCartClicked) },
                                shape = androidx.compose.foundation.shape.CircleShape,
                                contentPadding = PaddingValues(
                                    horizontal = if (isCompact && cartCount == 0) 12.dp else 16.dp,
                                    vertical = 8.dp
                                ),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.height(40.dp)
                            ) {
                                Icon(GenesysIcons.ShoppingCart, null, modifier = Modifier.size(20.dp))
                                if (!isCompact || cartCount > 0) {
                                    Spacer(Modifier.width(8.dp))
                                    Text("Carrinho", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            if (isCompact && cartCount > 0) {
                ExtendedFloatingActionButton(
                    onClick = { onEvent(PageViewerScreenEvent.OnOpenCartClicked) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = {
                        BadgedBox(
                            badge = {
                                Badge { Text(cartCount.toString()) }
                            }
                        ) {
                            Icon(GenesysIcons.ShoppingCart, "Ver Carrinho")
                        }
                    },
                    text = { Text("Ver Carrinho") }
                )
            }
        }
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
                        onServiceClick = { onEvent(PageViewerScreenEvent.OnServiceClicked(it)) },
                        filterQuery = currentFilterQuery,
                        onFilterQueryChange = { onEvent(PageViewerScreenEvent.OnFilterQueryChanged(it)) },
                        allProducts = allProducts,
                        allServices = allServices,
                        allAvailableCategories = allCategories
                    )
                }
                GenesysSpacer(GenesysSpacing.Huge)
            }
        }
    }
}
