package com.itbenevides.genesys21

actual fun syncUrlWithScreen(screen: Screen, pageId: String?, productId: String?) { }

actual fun getInitialUrlPath(): String? = null

actual fun getWebBaseUrl(): String = BuildConfig.WEB_BASE_URL

actual fun onUrlChange(callback: () -> Unit) { }
