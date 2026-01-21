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

class Router(val viewModel: PageViewModel) {
    var currentRoute by mutableStateOf<Route>(Route.Splash)
        private set

    private val historyStack = mutableListOf<Route>()

    /**
     * Retorna o histórico de navegação atual.
     */
    fun getHistory(): List<Route> = historyStack.toList()

    /**
     * Navega para uma nova rota, adicionando a anterior ao histórico.
     */
    fun navigateTo(route: Route) {
        if (currentRoute != route) {
            // Evita salvar Splash ou duplicatas na pilha de histórico
            if (currentRoute !is Route.Splash && historyStack.lastOrNull() != currentRoute) {
                historyStack.add(currentRoute)
            }
            currentRoute = route
        }
    }

    /**
     * Volta para a tela anterior baseada no histórico interno ou fallback inteligente.
     */
    fun goBack() {
        if (historyStack.isNotEmpty()) {
            currentRoute = historyStack.removeAt(historyStack.size - 1)
        } else {
            // Fallback robusto baseado na rota atual
            when (val route = currentRoute) {
                is Route.Cart -> currentRoute = Route.PageList
                is Route.ProductDetails -> currentRoute = route.fromRoute
                is Route.WhiteLabel -> currentRoute = Route.PageList
                is Route.PageEditor -> currentRoute = Route.PageList
                is Route.PublicViewer -> currentRoute = Route.PageList
                else -> navigateBack() // Fallback de sistema
            }
        }
    }

    /**
     * Sincroniza a rota atual com a URL do navegador.
     */
    fun forceSyncUrl() {
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
            is Route.Cart -> null to null
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
            is Route.Cart -> Screen.List
        }
        
        syncUrlWithScreen(screen, pageId, productId)
    }

    /**
     * Trata Deep Links e o redirecionamento inicial.
     */
    suspend fun handleDeepLink() {
        val urlPath = getInitialUrlPath() ?: "/"
        val currentDomain = getHostname().lowercase().removePrefix("www.")
        
        // 1. Prioridade: Domínio Customizado (AWS/Produção)
        if ((urlPath == "/" || urlPath == "") && currentDomain != "localhost" && currentDomain != "127.0.0.1") {
            viewModel.loadPageByDomain(currentDomain)?.let { page ->
                currentRoute = Route.PublicViewer(page)
                return
            }
        }

        // 2. Extração de IDs da URL
        val pageId = urlPath.extractId("/p/") ?: urlPath.extractId("/view/") ?: urlPath.extractId("/editor/")
        val productId = urlPath.extractId("/product/")

        if (pageId != null && pageId != "new") {
            viewModel.loadPublicPage(pageId)?.let { page ->
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
                return
            }
        }

        // 3. Fallback para Raiz "/" ou erros
        if (urlPath == "/" || urlPath == "" || urlPath.startsWith("/login")) {
            val token = viewModel.getCurrentUserToken()
            if (token != null) {
                val userPages = viewModel.getPagesSync()
                if (userPages.isNotEmpty()) {
                    currentRoute = Route.PublicViewer(userPages.first())
                    return
                }
                currentRoute = Route.PageList
            } else {
                viewModel.loadFirstPublicPage()?.let { firstPage ->
                    currentRoute = Route.PublicViewer(firstPage)
                } ?: run {
                    currentRoute = Route.Login
                }
            }
            return
        }

        // 4. Mapeamento de rotas genéricas
        try {
            when {
                urlPath.startsWith("/list") -> currentRoute = Route.PageList
                else -> {
                    val token = viewModel.getCurrentUserToken()
                    currentRoute = if (token != null) Route.PageList else Route.Login
                }
            }
        } catch (e: Exception) {
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
