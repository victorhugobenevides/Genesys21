package com.itbenevides.genesys21

actual fun syncUrlWithScreen(screen: Screen, pageId: String?) {
    // No Android, não há barra de endereços.
}

actual fun getInitialUrlPath(): String? = null

actual fun getWebBaseUrl(): String = BuildConfig.WEB_BASE_URL
