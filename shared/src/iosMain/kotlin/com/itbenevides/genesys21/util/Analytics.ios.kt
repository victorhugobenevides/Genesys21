package com.itbenevides.genesys21.util

/**
 * Implementação do Analytics para iOS (No-op temporário devido a limitações do ambiente).
 */
actual val AnalyticsManager: Analytics =
    object : Analytics {
        override fun logEvent(
            name: String,
            params: Map<String, Any>,
        ) {
            println("iOS Analytics: $name - $params")
        }

        override fun trackPageView(pageName: String) {
            println("iOS Analytics: PageView -> $pageName")
        }

        override fun recordError(
            title: String,
            throwable: Throwable?,
            extraParams: Map<String, String>,
        ) {
            val errorParams =
                mutableMapOf<String, Any>(
                    "error_title" to title,
                    "error_message" to (throwable?.message ?: "Unknown"),
                )
            errorParams.putAll(extraParams)
            logEvent("app_error", errorParams)
        }
    }
