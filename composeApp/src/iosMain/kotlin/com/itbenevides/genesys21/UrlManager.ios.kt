package com.itbenevides.genesys21

actual fun syncUrlWithScreen(screen: Screen, pageId: String?) {
    // No iOS, não há uma barra de endereços para sincronizar.
}

actual fun getInitialUrlPath(): String? = null
