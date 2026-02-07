package com.itbenevides.genesys21.util

import cocoapods.FirebaseAnalytics.FIRAnalytics
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * Implementação do Analytics para iOS usando Firebase Nativo (via CocoaPods).
 */
@OptIn(ExperimentalForeignApi::class)
actual val AnalyticsManager: Analytics = object : Analytics {
    override fun logEvent(name: String, params: Map<String, Any>) {
        val nsParams = params.map { (key, value) ->
            key as Any? to value as Any?
        }.toMap()
        
        FIRAnalytics.logEventWithName(name, nsParams)
    }

    override fun trackPageView(pageName: String) {
        FIRAnalytics.logEventWithName("screen_view", mapOf(
            "screen_name" to pageName,
            "screen_class" to "ComposeUIViewController"
        ))
    }

    override fun logException(throwable: Throwable, message: String?, additionalParams: Map<String, Any>) {
        // No iOS, assim como no Android, o ideal seria usar Firebase Crashlytics.
        // Simulamos via evento para observabilidade unificada.
        val params = mutableMapOf<String, Any>()
        params["exception_message"] = message ?: throwable.message ?: "Unknown"
        params["exception_type"] = throwable::class.simpleName ?: "Throwable"
        params["stack_trace"] = throwable.stackTraceToString().take(1000)
        params.putAll(additionalParams)
        
        logEvent("app_exception", params)
    }
}
