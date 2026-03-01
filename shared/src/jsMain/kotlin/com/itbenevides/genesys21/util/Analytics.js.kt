package com.itbenevides.genesys21.util

actual object Analytics {
    actual fun logEvent(name: String, params: Map<String, Any>?) {
        println("JS_LOG [Event]: $name | Params: $params")
    }

    actual fun trackPageView(pageName: String) {
        println("JS_LOG [PageView]: $pageName")
    }

    actual fun logException(throwable: Throwable, message: String?, additionalParams: Map<String, Any>?) {
        println("JS_EXCEPTION: $message - ${throwable.message}")
    }
}
