package com.itbenevides.genesys21

import com.itbenevides.genesys21.navigation.Screen

expect fun syncUrlWithScreen(
    screen: Screen,
    pageId: String? = null,
    productId: String? = null,
    title: String? = null,
)

expect fun getInitialUrlPath(): String?

expect fun getUrlParams(): Map<String, String>

expect fun getWebBaseUrl(): String

expect fun onUrlChange(callback: () -> Unit)

expect fun navigateBack()
