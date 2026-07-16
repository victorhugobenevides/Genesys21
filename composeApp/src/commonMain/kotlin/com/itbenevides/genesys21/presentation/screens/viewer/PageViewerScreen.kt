package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.itbenevides.genesys21.BrandingEffects
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.getWebBaseUrl
import com.itbenevides.genesys21.navigation.Route
import com.itbenevides.genesys21.navigation.Router
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysIconButton
import com.itbenevides.genesys21.ui.components.atoms.primitives.*
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.organisms.navigation.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.templates.pages.GenesysPage
import com.itbenevides.genesys21.ui.theme.AppTheme
import com.itbenevides.genesys21.ui.theme.GenesysDimens
import com.itbenevides.genesys21.util.AnalyticsManager
import com.itbenevides.genesys21.util.ShareManagerInstance

@Composable
fun PageViewerScreen(
    page: Page,
    router: Router,
    onOpenDashboard: () -> Unit,
) {
    var state by remember { mutableStateOf(PageViewerScreenState(page)) }
    var currentFilterQuery by remember { mutableStateOf("") }

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
            is PageViewerScreenEvent.OnFilterQueryChanged -> {
                currentFilterQuery = event.query
            }
        }
    }

    BrandingEffects(state.page)

    AppTheme(themeConfig = state.page.theme, customTheme = state.page.customTheme) {
        PageViewerContent(state, currentFilterQuery, onEvent)
    }
}

@Composable
fun PageViewerContent(
    state: PageViewerScreenState,
    currentFilterQuery: String,
    onEvent: (PageViewerScreenEvent) -> Unit,
) {
    GenesysPage(
        topBar = {
            GenesysTopAppBar(
                title = state.page.title,
                onBack = { onEvent(PageViewerScreenEvent.OnBackClicked) },
                actions = {
                    GenesysIconButton(
                        icon = GenesysIcons.ShoppingBag,
                        onClick = { onEvent(PageViewerScreenEvent.OnOpenCartClicked) },
                    )
                    GenesysIconButton(
                        icon = GenesysIcons.List,
                        onClick = { onEvent(PageViewerScreenEvent.OnOpenHistoryClicked) },
                    )
                    GenesysIconButton(
                        icon = GenesysIcons.Share,
                        onClick = { onEvent(PageViewerScreenEvent.OnShareClicked) },
                    )
                    if (false) { // Apenas se autenticado como admin
                        GenesysIconButton(
                            icon = GenesysIcons.Settings,
                            onClick = { onEvent(PageViewerScreenEvent.OnOpenAdminSettingsClicked) },
                        )
                    }
                },
            )
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
                        onServiceClick = { onEvent(PageViewerScreenEvent.OnServiceClicked(it)) },
                        filterQuery = currentFilterQuery,
                        onFilterQueryChange = { onEvent(PageViewerScreenEvent.OnFilterQueryChanged(it)) },
                    )
                }
                GenesysSpacer(GenesysSpacing.Huge)
            }
        }
    }
}
