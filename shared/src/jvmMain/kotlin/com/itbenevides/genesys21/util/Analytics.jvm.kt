package com.itbenevides.genesys21.util

actual object Analytics {
    actual fun logEvent(name: String, params: Map<String, Any>?) {
        println("SERVER_LOG [Event]: $name | Params: $params")
    }

    actual fun trackPageView(pageName: String) {
        println("SERVER_LOG [PageView]: $pageName")
    }

    actual fun logException(throwable: Throwable, message: String?, additionalParams: Map<String, Any>?) {
        val fullMessage = message ?: throwable.message ?: "Unknown Server Error"
        System.err.println("SERVER_EXCEPTION: $fullMessage")
        System.err.println("Params: $additionalParams")
        throwable.printStackTrace()
    }
}
