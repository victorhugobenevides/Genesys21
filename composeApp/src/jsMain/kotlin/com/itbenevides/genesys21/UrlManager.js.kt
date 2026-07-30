package com.itbenevides.genesys21

import com.itbenevides.genesys21.navigation.Screen
import kotlinx.browser.window

actual fun syncUrlWithScreen(
    screen: Screen,
    pageId: String?,
    productId: String?,
    title: String?,
    replace: Boolean,
) {
    val path =
        when (screen) {
            Screen.Splash -> "/"
            Screen.Login -> "/login"
            Screen.List -> "/list"
            Screen.Editor -> if (pageId != null) "/editor/$pageId" else "/editor/new"
            Screen.WhiteLabel -> if (pageId != null) "/view/$pageId" else "/view"
            Screen.PublicViewer -> if (pageId != null) "/p/$pageId" else "/p"
            Screen.ProductDetails -> "/product"
            Screen.ProductEditor -> if (pageId != null) "/product/edit/$pageId" else "/product/new"
            else -> "/"
        }

    if (replace) {
        window.history.replaceState(null, "", path)
    } else {
        window.history.pushState(null, "", path)
    }
}

actual fun getInitialUrlPath(): String? {
    return window.location.pathname
}

actual fun getUrlParams(): Map<String, String> = emptyMap()

actual fun getWebBaseUrl(): String {
    return "${window.location.protocol}//${window.location.host}"
}

actual fun onUrlChange(callback: () -> Unit) {
    window.onpopstate = { callback() }
}

actual fun navigateBack() {
    window.history.back()
}
