package com.itbenevides.genesys21.util

actual object Analytics {
    actual fun logEvent(name: String, params: Map<String, Any>?) {
        // No WasmJs, poderíamos integrar com o GA via JS bridge
        println("JS_ANALYTICS: Event $name with $params")
    }

    actual fun trackPageView(pageName: String) {
        println("JS_ANALYTICS: PageView $pageName")
    }

    actual fun logException(throwable: Throwable, message: String?, additionalParams: Map<String, Any>?) {
        println("JS_ANALYTICS: ERROR $message - ${throwable.message}")
    }
}
