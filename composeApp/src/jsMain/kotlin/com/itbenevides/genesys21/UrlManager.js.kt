package com.itbenevides.genesys21

import kotlinx.browser.window

actual fun syncUrlWithScreen(screen: Screen, pageId: String?) {
    val path = when (screen) {
        Screen.Login -> "/login"
        Screen.List -> "/list"
        Screen.Editor -> if (pageId != null) "/editor/$pageId" else "/editor/new"
    }
    window.history.pushState(null, "", path)
}
