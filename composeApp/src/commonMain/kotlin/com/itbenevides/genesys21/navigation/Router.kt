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

class Router(private val viewModel: PageViewModel) {
    var currentRoute by mutableStateOf<Route>(Route.Splash)
        private set

    fun navigateTo(route: Route) {
        if (currentRoute != route) {
            println("WASM: Router.navigateTo -> $route")
            currentRoute = route
        }
    }

    fun goBack() {
        navigateBack()
    }

    // Sincroniza o estado interno com a URL (Escrita)
    fun forceSyncUrl() {
        val (pageId, productId) = when (val route = currentRoute) {
            is Route.PageEditor -> route.page?.id to null
            is Route.WhiteLabel -> route.page.id to null
            is Route.PublicViewer -> route.page.id to null
            is Route.ProductDetails -> {
                val pId = if (route.fromRoute is Route.PublicViewer) route.fromRoute.page.id else null
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
        }
        
        syncUrlWithScreen(screen, pageId, productId)
    }

    // Reconstrói o estado a partir da URL (Leitura)
    suspend fun handleDeepLink() {
        val urlPath = getInitialUrlPath() ?: "/"
        println("WASM: Router.handleDeepLink processando -> $urlPath")
        
        val pageId = urlPath.extractId("/p/") ?: urlPath.extractId("/view/") ?: urlPath.extractId("/editor/")
        val productId = urlPath.extractId("/product/")

        try {
            when {
                pageId != null && pageId != "new" -> {
                    val page = viewModel.loadPublicPage(pageId)
                    if (page != null) {
                        println("WASM: DeepLink -> Página carregada: ${page.id}")
                        if (productId != null) {
                            val product = findProductInPage(page, productId)
                            if (product != null) {
                                currentRoute = Route.ProductDetails(product, Route.PublicViewer(page))
                                return
                            }
                        }
                        currentRoute = when {
                            urlPath.contains("/p/") -> Route.PublicViewer(page)
                            urlPath.contains("/view/") -> Route.WhiteLabel(page)
                            urlPath.contains("/editor/") -> Route.PageEditor(page)
                            else -> Route.PublicViewer(page)
                        }
                    } else {
                        println("WASM: DeepLink -> Página não encontrada, voltando para login")
                        currentRoute = Route.Login
                    }
                }
                urlPath.startsWith("/login") -> currentRoute = Route.Login
                urlPath.startsWith("/list") -> currentRoute = Route.PageList
                else -> {
                    val token = viewModel.getCurrentUserToken()
                    currentRoute = if (token != null) Route.PageList else Route.Login
                }
            }
            println("WASM: Router.handleDeepLink -> Rota definida para $currentRoute")
        } catch (e: Exception) {
            println("WASM: Router.handleDeepLink ERROR -> ${e.message}")
            currentRoute = Route.Login
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
