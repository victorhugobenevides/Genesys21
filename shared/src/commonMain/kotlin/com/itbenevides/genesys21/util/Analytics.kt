package com.itbenevides.genesys21.util

interface Analytics {
    fun logEvent(
        name: String,
        params: Map<String, Any> = emptyMap(),
    )

    fun trackPageView(pageName: String)
}

// Singleton global para acesso simplificado
expect val AnalyticsManager: Analytics
