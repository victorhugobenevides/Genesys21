package com.itbenevides.genesys21.util

expect object Analytics {
    fun logEvent(name: String, params: Map<String, Any>? = null)
    fun trackPageView(pageName: String)
    fun logException(throwable: Throwable, message: String? = null, additionalParams: Map<String, Any>? = null)
}
