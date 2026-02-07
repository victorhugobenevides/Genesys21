package com.itbenevides.genesys21.util

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

/**
 * Implementação do Analytics para Android usando Firebase Nativo.
 */
actual val AnalyticsManager: Analytics = object : Analytics {
    private val firebaseAnalytics: FirebaseAnalytics = Firebase.analytics

    override fun logEvent(name: String, params: Map<String, Any>) {
        val bundle = Bundle()
        params.forEach { (key, value) ->
            when (value) {
                is String -> bundle.putString(key, value)
                is Int -> bundle.putInt(key, value)
                is Long -> bundle.putLong(key, value)
                is Double -> bundle.putDouble(key, value)
                is Boolean -> bundle.putBoolean(key, value)
            }
        }
        firebaseAnalytics.logEvent(name, bundle)
    }

    override fun trackPageView(pageName: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, pageName)
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "ComposeActivity")
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    override fun logException(throwable: Throwable, message: String?, additionalParams: Map<String, Any>) {
        // No Firebase Android, você geralmente usa o Firebase Crashlytics para exceções.
        // Aqui simulamos via logEvent para manter a consistência do Analytics, 
        // mas idealmente integraria com Crashlytics.recordException(throwable)
        val bundle = Bundle()
        bundle.putString("exception_message", message ?: throwable.message ?: "Unknown")
        bundle.putString("exception_type", throwable::class.simpleName)
        bundle.putString("stack_trace", throwable.stackTraceToString().take(1000))
        additionalParams.forEach { (k, v) -> bundle.putString(k, v.toString()) }
        firebaseAnalytics.logEvent("app_exception", bundle)
    }
}
