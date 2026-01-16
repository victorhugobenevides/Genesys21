package com.itbenevides.genesys21

actual fun syncUrlWithScreen(screen: Screen, pageId: String?) { }

actual fun getInitialUrlPath(): String? = null

actual fun getWebBaseUrl(): String {
    return "http://localhost:8081" 
}
