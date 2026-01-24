package com.itbenevides.genesys21.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.getInitialUrlPath
import com.itbenevides.genesys21.onUrlChange
import com.itbenevides.genesys21.syncUrlWithScreen
import com.itbenevides.genesys21.navigateBack
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.di.getHostname
import com.itbenevides.genesys21.util.AnalyticsManager
import kotlinx.coroutines.*

/**
 * Router re-arquitetado: O Navegador é o Boss.
 * O app apenas observa a URL e muda a tela de acordo.
 */
class Router(val viewModel: PageViewModel) {
    
    private val navigationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var navigationJob: Job? = null

    var currentRoute by mutableStateOf<Route>(Route.Splash)
        private set

    private val historyStack = mutableListOf<Route>()

    fun getHistory(): List<Route> = historyStack.toList()

    fun navigateTo(route: Route, replace: Boolean = false) {
        val current = currentRoute
        if (current == route) return
        
        // Proteção contra duplicidade: Se a nova rota for idêntica à atual, não faz nada
        if (current::class == route::class) {
             val isSameId = when {
                 current is Route.WhiteLabel && route is Route.WhiteLabel -> current.page.id == route.page.id
                 current is Route.PublicViewer && route is Route.PublicViewer -> current.page.id == route.page.id
                 else -> false
             }
             if (isSameId && !replace) return
        }

        if (!replace && current !is Route.Splash) {
            historyStack.add(current)
        }

        applyRouteState(route)
        forceSyncUrl()
    }

    fun goBack() {
        if (historyStack.isNotEmpty()) {
            val last = historyStack.removeAt(historyStack.size - 1)
            applyRouteState(last)
            forceSyncUrl()
        } else {
            navigateBack()
        }
    }

    private fun applyRouteState(route: Route) {
        if (currentRoute != route) {
            currentRoute = route
            trackRoute(route)
        }
    }

    private fun trackRoute(route: Route) {
        val pageName = when (route) {
            is Route.Splash -> "Splash"
            is Route.Login -> "Login"
            is Route.PageList -> "Minhas Páginas"
            is Route.PageEditor -> "Editor de Página"
            is Route.WhiteLabel -> "Admin - ${route.page.title}"
            is Route.PublicViewer -> "Vitrine - ${route.page.title}"
            is Route.ProductDetails -> "Detalhes - ${route.product.name}"
            is Route.ProductEditor -> "Editor de Produto"
            is Route.Cart -> "Carrinho"
            is Route.OrderTracking -> "Acompanhamento de Pedido"
            is Route.CustomerOrderHistory -> "Meus Pedidos"
        }
        AnalyticsManager.trackPageView(pageName)
    }

    fun forceSyncUrl() {
        val current = currentRoute
        if (current is Route.Splash) return

        val (pageId, productId) = when (current) {
            is Route.PageEditor -> current.page?.id to null
            is Route.WhiteLabel -> current.page.id to null
            is Route.PublicViewer -> current.page.id to null
            is Route.ProductDetails -> {
                val pId = (current.fromRoute as? Route.PublicViewer)?.page?.id 
                    ?: (current.fromRoute as? Route.WhiteLabel)?.page?.id
                pId to current.product.id
            }
            is Route.ProductEditor -> current.page.id to current.product?.id
            is Route.OrderTracking -> current.orderId to null
            is Route.CustomerOrderHistory -> current.page?.id to null
            else -> null to null
        }
        
        val screen = when (current) {
            Route.Splash -> Screen.Splash
            Route.Login -> Screen.Login
            Route.PageList -> Screen.List
            is Route.PageEditor -> Screen.Editor
            is Route.WhiteLabel -> Screen.WhiteLabel
            is Route.PublicViewer -> Screen.PublicViewer
            is Route.ProductDetails -> Screen.ProductDetails
            is Route.ProductEditor -> Screen.ProductEditor
            is Route.Cart -> Screen.Cart
            is Route.OrderTracking -> Screen.OrderTracking
            is Route.CustomerOrderHistory -> Screen.OrderHistory
        }
        
        syncUrlWithScreen(screen, pageId, productId)
    }

    fun handleDeepLink() {
        navigationJob?.cancel()
        navigationJob = navigationScope.launch {
            val urlPath = getInitialUrlPath() ?: "/"
            val currentDomain = getHostname().lowercase().removePrefix("www.")
            val token = viewModel.getCurrentUserToken()
            val isLoggedIn = token != null

            val orderId = urlPath.extractId("/track/")
            if (orderId != null) {
                applyRouteState(Route.OrderTracking(orderId))
                return@launch
            }

            if ((urlPath == "/" || urlPath == "") && currentDomain != "localhost" && currentDomain != "127.0.0.1") {
                viewModel.loadPageByDomain(currentDomain)?.let { page ->
                    applyRouteState(Route.PublicViewer(page))
                    return@launch
                }
            }

            val pageId = urlPath.extractId("/p/") ?: urlPath.extractId("/view/") ?: urlPath.extractId("/editor/")
            val productId = urlPath.extractId("/product/")

            if (pageId != null && pageId != "new" && pageId.length >= 4) {
                val page = viewModel.loadPublicPage(pageId)
                if (page != null) {
                    if (productId != null) {
                        findProductInPage(page, productId)?.let { product ->
                            applyRouteState(Route.ProductDetails(product, Route.PublicViewer(page)))
                            return@launch
                        }
                    }
                    val target = when {
                        urlPath.contains("/view/") -> if (isLoggedIn) Route.WhiteLabel(page) else Route.Login
                        urlPath.contains("/editor/") -> if (isLoggedIn) Route.PageEditor(page) else Route.Login
                        else -> Route.PublicViewer(page)
                    }
                    applyRouteState(target)
                    return@launch
                }
            }

            val finalRoute = when {
                urlPath.startsWith("/login") -> Route.Login
                urlPath.startsWith("/list") -> if (isLoggedIn) Route.PageList else Route.Login
                urlPath.startsWith("/cart") -> Route.Cart(null)
                urlPath.startsWith("/history") -> Route.CustomerOrderHistory(null)
                else -> {
                    if (isLoggedIn) Route.PageList 
                    else viewModel.loadFirstPublicPage()?.let { Route.PublicViewer(it) } ?: Route.Login
                }
            }
            applyRouteState(finalRoute)
        }
    }

    private fun String.extractId(prefix: String) = 
        if (contains(prefix)) substringAfter(prefix).split("/").firstOrNull() else null

    private fun findProductInPage(page: Page, productId: String): Product? =
        page.components
            .filterIsInstance<com.itbenevides.genesys21.domain.model.PageComponent.ProductList>()
            .flatMap { it.products }
            .find { it.id == productId }
}
