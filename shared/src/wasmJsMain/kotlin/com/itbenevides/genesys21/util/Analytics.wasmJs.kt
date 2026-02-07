package com.itbenevides.genesys21.util

/**
 * Implementação Mock para WasmJs (Pode ser estendida para usar JS Interop com GA4/Amplitude)
 */
actual val AnalyticsManager: Analytics = object : Analytics {
    override fun logEvent(name: String, params: Map<String, Any>) {
        println("ANALYTICS [Event]: $name | Params: $params")
    }

    override fun trackPageView(pageName: String) {
        println("ANALYTICS [PageView]: $pageName")
    }

    override fun logException(throwable: Throwable, message: String?, additionalParams: Map<String, Any>) {
        val fullMessage = message ?: throwable.message ?: "No message"
        println("ANALYTICS [EXCEPTION]: $fullMessage")
        println(throwable.stackTraceToString())
    }
}
