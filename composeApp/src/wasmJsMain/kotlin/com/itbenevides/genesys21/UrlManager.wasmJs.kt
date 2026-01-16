package com.itbenevides.genesys21

import kotlinx.browser.window

actual fun syncUrlWithScreen(screen: Screen, pageId: String?) {
    val path = when (screen) {
        Screen.Splash -> "/"
        Screen.Login -> "/login"
        Screen.List -> "/list"
        Screen.Editor -> if (pageId != null) "/editor/$pageId" else "/editor/new"
        Screen.WhiteLabel -> if (pageId != null) "/view/$pageId" else "/view"
        Screen.PublicViewer -> if (pageId != null) "/p/$pageId" else "/p"
        Screen.ProductDetails -> "/product"
        Screen.ProductEditor -> "/product/edit"
    }
    window.history.pushState(null, "", path)
}
