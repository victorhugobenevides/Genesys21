package com.itbenevides.genesys21.util

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import android.os.Bundle

actual object Analytics {
    private val analytics: FirebaseAnalytics by lazy { Firebase.analytics }

    actual fun logEvent(name: String, params: Map<String, Any>?) {
        val bundle = Bundle()
        params?.forEach { (key, value) ->
            when (value) {
                is String -> bundle.putString(key, value)
                is Int -> bundle.putInt(key, value)
                is Double -> bundle.putDouble(key, value)
                is Boolean -> bundle.putBoolean(key, value)
            }
        }
        analytics.logEvent(name, bundle)
    }

    actual fun trackPageView(pageName: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, pageName)
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity")
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    actual fun logException(throwable: Throwable, message: String?, additionalParams: Map<String, Any>?) {
        logEvent("app_exception", (additionalParams ?: emptyMap()) + mapOf(
            "message" to (message ?: throwable.message ?: "unknown"),
            "type" to throwable::class.simpleName.toString()
        ))
    }
}
