package com.itbenevides.genesys21.util

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

/**
 * Implementação do Analytics para Android usando Firebase Nativo.
 * Resiliente para falhas na inicialização do Firebase (útil para testes de UI e Paparazzi).
 */
actual val AnalyticsManager: Analytics =
    object : Analytics {
        private val firebaseAnalytics: FirebaseAnalytics? by lazy {
            try {
                Firebase.analytics
            } catch (e: Throwable) {
                null
            }
        }

        override fun logEvent(
            name: String,
            params: Map<String, Any>,
        ) {
            val fa = firebaseAnalytics ?: return
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
            fa.logEvent(name, bundle)
        }

        override fun trackPageView(pageName: String) {
            val fa = firebaseAnalytics ?: return
            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, pageName)
            bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "ComposeActivity")
            fa.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
        }

        override fun recordError(
            title: String,
            throwable: Throwable?,
            extraParams: Map<String, String>,
        ) {
            try {
                val crashlytics = com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance()
                crashlytics.setCustomKey("error_title", title)
                extraParams.forEach { (k, v) -> crashlytics.setCustomKey(k, v) }
                if (throwable != null) {
                    crashlytics.recordException(throwable)
                } else {
                    crashlytics.log("Non-fatal error: $title")
                }
            } catch (e: Throwable) {
                // Silencioso em caso de erro na inicialização do Crashlytics
            }
        }
    }
