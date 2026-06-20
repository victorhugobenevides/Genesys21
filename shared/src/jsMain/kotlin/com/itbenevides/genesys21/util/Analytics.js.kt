package com.itbenevides.genesys21.util

actual val AnalyticsManager: Analytics =
    object : Analytics {
        override fun logEvent(
            name: String,
            params: Map<String, Any>,
        ) {
            println("JS Analytics: $name - $params")
        }

        override fun trackPageView(pageName: String) {
            println("JS Analytics: PageView -> $pageName")
        }

        override fun recordError(
            title: String,
            throwable: Throwable?,
            extraParams: Map<String, String>,
        ) {
            println("JS Analytics Error: $title - ${throwable?.message} - $extraParams")
        }
    }
