package com.itbenevides.genesys21

import com.itbenevides.genesys21.navigation.Screen
import kotlinx.browser.window

// Evita que o app tente gravar na URL algo que acabou de vir do navegador
private var isInternalNav = true 

actual fun syncUrlWithScreen(screen: Screen, pageId: String?, productId: String?) {
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
    }
    
    val browserPath = window.location.pathname.removeSuffix("/")
    val targetPath = path.removeSuffix("/")
    
    if (browserPath != targetPath && isInternalNav) {
        println("WASM: [UI -> Browser] pushState: $path")
        window.history.pushState(null, "", path)
    }
    isInternalNav = true
}

actual fun getInitialUrlPath(): String? = window.location.pathname

actual fun getWebBaseUrl(): String = "${window.location.protocol}//${window.location.host}"

actual fun onUrlChange(callback: () -> Unit) {
    window.addEventListener("popstate", {
        println("WASM: [Browser -> UI] Popstate detectado: ${window.location.pathname}")
        isInternalNav = false
        callback()
    })
}

actual fun navigateBack() {
    window.history.back()
}
