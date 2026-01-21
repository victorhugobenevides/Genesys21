package com.itbenevides.genesys21.util

/**
 * Implementação No-op para JVM (Servidor).
 */
actual val AnalyticsManager: Analytics = object : Analytics {
    override fun logEvent(name: String, params: Map<String, Any>) {
        // No-op no servidor por enquanto
    }

    override fun trackPageView(pageName: String) {
        // No-op no servidor por enquanto
    }
}
