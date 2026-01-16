package com.itbenevides.genesys21

actual fun syncUrlWithScreen(screen: Screen, pageId: String?) {
    // No Android, a navegação é tratada internamente
}

actual fun getInitialUrlPath(): String? = null
