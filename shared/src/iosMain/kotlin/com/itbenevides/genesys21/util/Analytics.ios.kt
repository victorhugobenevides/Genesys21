package com.itbenevides.genesys21.util

import cocoapods.FirebaseAnalytics.FIRAnalytics
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * Implementação do Analytics para iOS usando Firebase Nativo (via CocoaPods).
 */
@OptIn(ExperimentalForeignApi::class)
actual object Analytics {
    actual fun logEvent(name: String, params: Map<String, Any>?) {
        val nsParams = params?.map { (key, value) ->
            key as Any? to value as Any?
        }?.toMap()
        
        FIRAnalytics.logEventWithName(name, parameters = nsParams)
    }

    actual fun trackPageView(pageName: String) {
        FIRAnalytics.logEventWithName("screen_view", parameters = mapOf(
            "screen_name" to pageName,
            "screen_class" to "ComposeUIViewController"
        ))
    }

    actual fun logException(throwable: Throwable, message: String?, additionalParams: Map<String, Any>?) {
        val params = mutableMapOf<String, Any>()
        params["exception_message"] = message ?: throwable.message ?: "Unknown"
        params["exception_type"] = throwable::class.simpleName ?: "Throwable"
        params["stack_trace"] = throwable.stackTraceToString().take(1000)
        additionalParams?.let { params.putAll(it) }
        
        logEvent("app_exception", params)
    }
}
