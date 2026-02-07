package com.itbenevides.genesys21.util

/**
 * Implementação para JVM (Servidor).
 * No servidor, logs de exceção são direcionados para o console standard (stdout/stderr).
 */
actual val AnalyticsManager: Analytics = object : Analytics {
    override fun logEvent(name: String, params: Map<String, Any>) {
        println("SERVER_LOG [Event]: $name | Params: $params")
    }

    override fun trackPageView(pageName: String) {
        println("SERVER_LOG [PageView]: $pageName")
    }

    override fun logException(throwable: Throwable, message: String?, additionalParams: Map<String, Any>) {
        val fullMessage = message ?: throwable.message ?: "Unknown Server Error"
        System.err.println("SERVER_EXCEPTION: $fullMessage")
        System.err.println("Params: $additionalParams")
        throwable.printStackTrace()
    }
}
