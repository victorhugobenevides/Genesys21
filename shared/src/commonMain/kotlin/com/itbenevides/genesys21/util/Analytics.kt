package com.itbenevides.genesys21.util

interface Analytics {
    fun logEvent(name: String, params: Map<String, Any> = emptyMap())
    fun trackPageView(pageName: String)
    fun logException(throwable: Throwable, message: String? = null, additionalParams: Map<String, Any> = emptyMap())
}

// Singleton global para acesso simplificado
expect val AnalyticsManager: Analytics
