package com.itbenevides.genesys21.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.itbenevides.genesys21.di.getCurrentUrl
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.presentation.PageViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Router(val viewModel: PageViewModel) {
    var currentRoute by mutableStateOf<Route>(Route.Splash)
        private set

    private val scope = CoroutineScope(Dispatchers.Main)
    private val navigationStack = mutableListOf<Route>()

    fun navigateTo(route: Route, replace: Boolean = false) {
        if (replace && navigationStack.isNotEmpty()) {
            navigationStack.removeAt(navigationStack.size - 1)
        }
        navigationStack.add(route)
        currentRoute = route
    }

    fun goBack() {
        if (navigationStack.size > 1) {
            navigationStack.removeAt(navigationStack.size - 1)
            currentRoute = navigationStack.last()
        }
    }

    fun handleDeepLink(url: String? = getCurrentUrl()) {
        scope.launch {
            try {
                // Pequeno delay para a Splash aparecer e o sistema estabilizar
                if (currentRoute == Route.Splash) delay(800)

                url?.let {
                    if (it.contains("genesys21://order/")) {
                        val orderId = it.substringAfterLast("/")
                        navigateTo(Route.OrderTracking(orderId), replace = true)
                        return@launch
                    }
                    
                    val params = it.substringAfter("?", "").split("&").mapNotNull { pair ->
                        val parts = pair.split("=")
                        if (parts.size == 2) parts[0] to parts[1] else null
                    }.toMap()

                    params["pageId"]?.let { pageId ->
                        viewModel.loadPublicPage(pageId)?.let { page ->
                            navigateTo(Route.PublicViewer(page), replace = true)
                            return@launch
                        }
                    }
                }

                // NAVEGAÇÃO PADRÃO: Resiliente a falhas
                if (currentRoute == Route.Splash) {
                    val token = viewModel.getCurrentUserToken()
                    if (token != null) {
                        navigateTo(Route.PageList, replace = true)
                    } else {
                        navigateTo(Route.Login, replace = true)
                    }
                }
            } catch (e: Exception) {
                println("ROUTER ERROR: Falha na navegação inicial: ${e.message}")
                // Em caso de erro crítico, força ida para o Login para não travar o App
                if (currentRoute == Route.Splash) {
                    navigateTo(Route.Login, replace = true)
                }
            }
        }
    }
}
