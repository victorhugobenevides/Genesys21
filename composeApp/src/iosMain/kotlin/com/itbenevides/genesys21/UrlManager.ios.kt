package com.itbenevides.genesys21

actual fun syncUrlWithScreen(screen: Screen, pageId: String?, productId: String?) { }

actual fun getInitialUrlPath(): String? = null

actual fun getWebBaseUrl(): String = "http://localhost:8081"

actual fun onUrlChange(callback: () -> Unit) { }
