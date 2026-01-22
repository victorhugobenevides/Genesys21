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

    // O Compose observa esse estado para saber qual tela mostrar
    var currentRoute by mutableStateOf<Route>(Route.Splash)
        private set

    private val historyStack = mutableListOf<Route>()

    fun getHistory(): List<Route> = historyStack.toList()

    // Navegação Interna: Quando você clica num botão
    fun navigateTo(route: Route) {
        if (currentRoute == route) return
        println("ROUTER_LOG: [Ação UI] -> Solicitando $route")
        
        if (currentRoute !is Route.Splash) {
            historyStack.add(currentRoute)
        }

        // Em vez de mudar a tela aqui, deixamos que o forceSyncUrl mude a URL 
        // e o Navegador então nos dirá para mudar a tela (Fluxo Unidirecional)
        applyRouteState(route)
        forceSyncUrl()
    }

    fun goBack() {
        if (historyStack.isNotEmpty()) {
            val last = historyStack.removeAt(historyStack.size - 1)
            applyRouteState(last)
            forceSyncUrl()
        } else {
            navigateBack() // Apenas pede ao navegador para voltar
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
        }
        AnalyticsManager.trackPageView(pageName)
    }

    fun forceSyncUrl() {
        if (currentRoute is Route.Splash) return

        val (pageId, productId) = when (val route = currentRoute) {
            is Route.PageEditor -> route.page?.id to null
            is Route.WhiteLabel -> route.page.id to null
            is Route.PublicViewer -> route.page.id to null
            is Route.ProductDetails -> {
                val pId = (route.fromRoute as? Route.PublicViewer)?.page?.id 
                    ?: (route.fromRoute as? Route.WhiteLabel)?.page?.id
                pId to route.product.id
            }
            is Route.ProductEditor -> route.page.id to route.product?.id
            else -> null to null
        }
        
        val screen = when (currentRoute) {
            Route.Splash -> Screen.Splash
            Route.Login -> Screen.Login
            Route.PageList -> Screen.List
            is Route.PageEditor -> Screen.Editor
            is Route.WhiteLabel -> Screen.WhiteLabel
            is Route.PublicViewer -> Screen.PublicViewer
            is Route.ProductDetails -> Screen.ProductDetails
            is Route.ProductEditor -> Screen.ProductEditor
            is Route.Cart -> Screen.Cart
        }
        
        syncUrlWithScreen(screen, pageId, productId)
    }

    /**
     * O CORAÇÃO DO ROTEAMENTO:
     * Lê a URL atual do navegador e força o app a mostrar a tela certa.
     */
    fun handleDeepLink() {
        navigationJob?.cancel()
        navigationJob = navigationScope.launch {
            val urlPath = getInitialUrlPath() ?: "/"
            println("ROUTER_LOG: [O Navegador manda] Lendo URL: $urlPath")
            
            val currentDomain = getHostname().lowercase().removePrefix("www.")
            val token = viewModel.getCurrentUserToken()
            val isLoggedIn = token != null

            // 1. Prioridade absoluta: Domínio Customizado
            if ((urlPath == "/" || urlPath == "") && currentDomain != "localhost" && currentDomain != "127.0.0.1") {
                viewModel.loadPageByDomain(currentDomain)?.let { page ->
                    applyRouteState(Route.PublicViewer(page))
                    return@launch
                }
            }

            val pageId = urlPath.extractId("/p/") ?: urlPath.extractId("/view/") ?: urlPath.extractId("/editor/")
            val productId = urlPath.extractId("/product/")

            // 2. Rota por ID (Garante que compartilhamento e voltar funcionem)
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
                        urlPath.contains("/view/") -> {
                            if (isLoggedIn) Route.WhiteLabel(page) else Route.Login
                        }
                        urlPath.contains("/editor/") -> {
                            if (isLoggedIn) Route.PageEditor(page) else Route.Login
                        }
                        else -> Route.PublicViewer(page)
                    }
                    applyRouteState(target)
                    return@launch
                }
            }

            // 3. Fallback inteligente com proteção de rotas privadas
            val finalRoute = when {
                urlPath.startsWith("/login") -> Route.Login
                urlPath.startsWith("/list") -> if (isLoggedIn) Route.PageList else Route.Login
                urlPath.startsWith("/cart") -> Route.Cart(null)
                else -> {
                    if (isLoggedIn) Route.PageList 
                    else {
                        viewModel.loadFirstPublicPage()?.let { Route.PublicViewer(it) } ?: Route.Login
                    }
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
