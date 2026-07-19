package com.itbenevides.genesys21.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.itbenevides.genesys21.di.getHostname
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.getInitialUrlPath
import com.itbenevides.genesys21.getUrlParams
import com.itbenevides.genesys21.navigateBack
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.syncUrlWithScreen
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

    fun navigateTo(
        route: Route,
        replace: Boolean = false,
    ) {
        val current = currentRoute
        if (current == route) return

        if (current::class == route::class) {
            val isSameId =
                when {
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
        val pageName =
            when (route) {
                is Route.Splash -> "Splash"
                is Route.Login -> "Login"
                is Route.PageList -> "Administração"
                is Route.PageEditor -> "Editor de Página"
                is Route.WhiteLabel -> "Admin: ${route.page.title}"
                is Route.PublicViewer -> route.page.title
                is Route.ProductDetails -> "Produto: ${route.product.name}"
                is Route.ProductEditor -> "Editando Produto"
                is Route.ServiceEditor -> "Editando Serviço"
                is Route.ServiceSelection -> "Selecionar Serviços"
                is Route.Cart -> "Meu Carrinho"
                is Route.OrderTracking -> "Rastreio de Pedido"
                is Route.CustomerOrderHistory -> "Meus Pedidos"
                is Route.Profile -> "Meu Perfil"
                is Route.DesignSystemShowcase -> "Design System Showcase"
                is Route.EditorShowcase -> "Editor Showcase"
                is Route.TemplateShowcase -> "Catálogo de Templates"
                is Route.ServiceBooking -> "Agendamento: ${route.service.name}"
            }
        AnalyticsManager.trackPageView(pageName)
    }

    fun forceSyncUrl() {
        val current = currentRoute
        if (current is Route.Splash) return

        val (pageId, productId, title) =
            when (current) {
                is Route.Splash -> Triple(null, null, "Genesys21")
                is Route.PageEditor -> Triple(current.page?.id, null, "Editor: ${current.page?.title ?: "Nova Página"}")
                is Route.WhiteLabel -> Triple(current.page.id, null, "Gerenciar: ${current.page.title}")
                is Route.PublicViewer -> Triple(current.page.id, null, current.page.title)
                is Route.ProductDetails -> {
                    val pId =
                        (current.fromRoute as? Route.PublicViewer)?.page?.id
                            ?: (current.fromRoute as? Route.WhiteLabel)?.page?.id
                    Triple(pId, current.product.id, current.product.name)
                }
                is Route.ProductEditor -> Triple(current.page.id, current.product?.id, "Produto: ${current.product?.name ?: "Novo"}")
                is Route.ServiceEditor -> Triple(current.page?.id, current.service?.id, "Serviço: ${current.service?.name ?: "Novo"}")
                is Route.ServiceSelection -> Triple(current.page.id, null, "Selecionar Serviços")
                is Route.OrderTracking -> Triple(null, null, "Pedido: ${current.orderId}")
                is Route.CustomerOrderHistory -> Triple(current.page?.id, null, "Meus Pedidos")
                is Route.Cart -> Triple(null, null, "Meu Carrinho")
                is Route.Login -> Triple(null, null, "Entrar - Genesys21")
                is Route.PageList -> Triple(null, null, "Administração")
                is Route.DesignSystemShowcase -> Triple(null, null, "Design System Showcase")
                is Route.Profile -> Triple(null, null, "Meu Perfil")
                is Route.EditorShowcase -> Triple(null, null, "Editor Showcase")
                is Route.TemplateShowcase -> Triple(null, null, "Catálogo de Templates")
                is Route.ServiceBooking -> Triple(current.page.id, current.service.id, "Agendar: ${current.service.name}")
            }

        val screen =
            when (current) {
                is Route.Splash -> Screen.Splash
                is Route.Login -> Screen.Login
                is Route.PageList -> Screen.List
                is Route.PageEditor -> Screen.Editor
                is Route.WhiteLabel -> Screen.WhiteLabel
                is Route.PublicViewer -> Screen.PublicViewer
                is Route.ProductDetails -> Screen.ProductDetails
                is Route.ProductEditor -> Screen.ProductEditor
                is Route.ServiceEditor -> Screen.ServiceBooking // Map to a general booking/service screen
                is Route.ServiceSelection -> Screen.ServiceBooking
                is Route.Cart -> Screen.Cart
                is Route.OrderTracking -> Screen.OrderTracking
                is Route.CustomerOrderHistory -> Screen.OrderHistory
                is Route.Profile -> Screen.Profile
                is Route.DesignSystemShowcase -> Screen.DesignSystemShowcase
                is Route.EditorShowcase -> Screen.EditorShowcase
                is Route.TemplateShowcase -> Screen.TemplateShowcase
                is Route.ServiceBooking -> Screen.ServiceBooking
            }

        syncUrlWithScreen(screen, pageId, productId, title ?: "Genesys21")
    }

    fun handleDeepLink() {
        navigationJob?.cancel()
        navigationJob =
            navigationScope.launch {
                val urlPath = getInitialUrlPath() ?: "/"
                val params = getUrlParams()

                if (params.containsKey("utm_source")) {
                    AnalyticsManager.logEvent("traffic_source", params.mapValues { it.value as Any })
                }

                val currentDomain = getHostname().lowercase().removePrefix("www.")
                val token = viewModel.getCurrentUserToken()
                val isLoggedIn = token != null

                val orderId = urlPath.extractId("/track/")
                if (orderId != null) {
                    applyRouteState(Route.OrderTracking(orderId))
                    forceSyncUrl()
                    return@launch
                }

                if ((urlPath == "/" || urlPath == "") && currentDomain != "localhost" && currentDomain != "127.0.0.1") {
                    viewModel.loadPageByDomain(currentDomain)?.let { page ->
                        applyRouteState(Route.PublicViewer(page))
                        forceSyncUrl()
                        return@launch
                    }
                }

                val pageIdFromParams = params["pageId"]
                val pageId = urlPath.extractId("/p/") ?: urlPath.extractId("/view/") ?: urlPath.extractId("/editor/") ?: pageIdFromParams
                val productId = urlPath.extractId("/product/")

                if (pageId != null && pageId != "new" && pageId.length >= 4) {
                    val page = viewModel.loadPublicPage(pageId)
                    if (page != null) {
                        if (productId != null) {
                            findProductInPage(page, productId)?.let { product ->
                                applyRouteState(Route.ProductDetails(product, Route.PublicViewer(page)))
                                forceSyncUrl()
                                return@launch
                            }
                        }
                        val target =
                            when {
                                urlPath.contains("/view/") -> if (isLoggedIn) Route.WhiteLabel(page) else Route.Login
                                urlPath.contains("/editor/") -> if (isLoggedIn) Route.PageEditor(page) else Route.Login
                                else -> Route.PublicViewer(page)
                            }
                        applyRouteState(target)
                        forceSyncUrl()
                        return@launch
                    }
                }

                val finalRoute =
                    when {
                        urlPath.startsWith("/login") -> Route.Login
                        urlPath.startsWith("/list") -> if (isLoggedIn) Route.PageList else Route.Login
                        urlPath.startsWith("/cart") -> Route.Cart(null)
                        urlPath.startsWith("/history") -> Route.CustomerOrderHistory(null)
                        urlPath.startsWith("/about") || urlPath.startsWith("/showcase") -> Route.DesignSystemShowcase
                        urlPath.startsWith("/editor-showcase") -> Route.EditorShowcase
                        urlPath.startsWith("/templates") -> Route.TemplateShowcase
                        else -> {
                            if (isLoggedIn) {
                                Route.PageList
                            } else {
                                viewModel.loadFirstPublicPage()?.let { Route.PublicViewer(it) } ?: Route.Login
                            }
                        }
                    }
                applyRouteState(finalRoute)
                forceSyncUrl()
            }
    }

    private fun String.extractId(prefix: String) = if (contains(prefix)) substringAfter(prefix).split("/").firstOrNull() else null

    private fun findProductInPage(
        page: Page,
        productId: String,
    ): Product? =
        page.components
            .filterIsInstance<com.itbenevides.genesys21.domain.model.PageComponent.ProductList>()
            .flatMap { it.products }
            .find { it.id == productId }
}
