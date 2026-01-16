package com.itbenevides.genesys21

expect fun syncUrlWithScreen(screen: Screen, pageId: String? = null)

expect fun getInitialUrlPath(): String?

expect fun getWebBaseUrl(): String
