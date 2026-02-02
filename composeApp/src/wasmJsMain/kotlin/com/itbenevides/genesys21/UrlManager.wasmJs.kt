package com.itbenevides.genesys21

import com.itbenevides.genesys21.navigation.Screen
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.events.Event

/**
 * Sincroniza o estado do Compose COM o Navegador.
 * Na nova lógica, o Navegador manda. Esta função apenas empurra a URL se necessário.
 */
actual fun syncUrlWithScreen(screen: Screen, pageId: String?, productId: String?, title: String?) {
    // Splash não gera histórico para evitar que o botão voltar caia numa tela branca
    if (screen == Screen.Splash) return

    val path = when (screen) {
        Screen.Splash -> "/"
        Screen.Login -> "/login"
        Screen.List -> "/list"
        Screen.Editor -> if (pageId != null) "/editor/$pageId" else "/editor/new"
        Screen.WhiteLabel -> if (pageId != null) "/view/$pageId" else "/view"
        Screen.PublicViewer -> if (pageId != null) "/p/$pageId" else "/p"
        Screen.ProductDetails -> {
            if (pageId != null && productId != null) "/p/$pageId/product/$productId"
            else if (productId != null) "/product/$productId"
            else "/product"
        }
        Screen.ProductEditor -> "/product/edit"
        Screen.Cart -> "/cart"
        Screen.OrderTracking -> if (pageId != null) "/track/$pageId" else "/track"
        Screen.OrderHistory -> "/history"
        else -> "/"
    }
    
    // ATUALIZA O TÍTULO DA ABA DINAMICAMENTE
    val displayTitle = if (!title.isNullOrBlank()) title else "Genesys21"
    document.title = displayTitle

    val currentPath = window.location.pathname.removeSuffix("/")
    val targetPath = path.removeSuffix("/")
    
    // Se a UI mudou e a URL ainda não reflete isso, atualizamos a URL sem disparar eventos circulares
    if (currentPath != targetPath) {
        println("WASM_LOG: [UI -> Browser] URL atualizada para: $path")
        window.history.pushState(null, "", path)
    }
}

actual fun getInitialUrlPath(): String? = window.location.pathname

actual fun getWebBaseUrl(): String = "${window.location.protocol}//${window.location.host}"

actual fun onUrlChange(callback: () -> Unit) {
    // O navegador avisa aqui quando o usuário clica em Voltar ou Avançar
    window.onpopstate = {
        println("WASM_LOG: [Browser -> UI] O Navegador mudou a URL para: ${window.location.pathname}")
        callback()
    }
}

actual fun navigateBack() {
    window.history.back()
}
