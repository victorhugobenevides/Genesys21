package com.itbenevides.genesys21.util

/**
 * Funções de Interoperação com JavaScript para Analytics no Wasm.
 */
@JsFun("(name, params) => { if (window.gtag) window.gtag('event', name, params); }")
private external fun jsLogEvent(
    name: String,
    params: JsAny?,
)

@JsFun("(pageName) => { if (window.gtag) window.gtag('event', 'page_view', { page_title: pageName, page_location: window.location.href, page_path: window.location.pathname }); }")
private external fun jsTrackPageView(pageName: String)

@JsFun("(title, message, extra) => { if (window.console) window.console.error('Analytics Error:', title, message, extra); if (window.gtag) window.gtag('event', 'exception', { 'description': title + ': ' + message, 'fatal': false }); }")
private external fun jsRecordError(
    title: String,
    message: String,
    extra: JsAny?,
)

@JsFun("() => ({})")
private external fun createJsObject(): JsAny

@JsFun("(obj, key, value) => { obj[key] = value; }")
private external fun setJsProperty(
    obj: JsAny,
    key: String,
    value: String,
)

@JsFun("(obj, key, value) => { obj[key] = value; }")
private external fun setJsProperty(
    obj: JsAny,
    key: String,
    value: Int,
)

@JsFun("(obj, key, value) => { obj[key] = value; }")
private external fun setJsProperty(
    obj: JsAny,
    key: String,
    value: Double,
)

@JsFun("(obj, key, value) => { obj[key] = value; }")
private external fun setJsProperty(
    obj: JsAny,
    key: String,
    value: Boolean,
)

/**
 * Converte um Map do Kotlin para um objeto JavaScript básico (JsAny).
 */
private fun Map<String, Any>.toJsObject(): JsAny {
    val obj = createJsObject()
    this.forEach { (key, value) ->
        when (value) {
            is String -> setJsProperty(obj, key, value)
            is Int -> setJsProperty(obj, key, value)
            is Double -> setJsProperty(obj, key, value)
            is Boolean -> setJsProperty(obj, key, value)
            else -> setJsProperty(obj, key, value.toString())
        }
    }
    return obj
}

actual val AnalyticsManager: Analytics =
    object : Analytics {
        override fun logEvent(
            name: String,
            params: Map<String, Any>,
        ) {
            try {
                val jsParams = if (params.isNotEmpty()) params.toJsObject() else null
                jsLogEvent(name, jsParams)
            } catch (e: Exception) {
                // Silencioso: Analytics não deve travar o App
            }
        }

        override fun trackPageView(pageName: String) {
            try {
                jsTrackPageView(pageName)
            } catch (e: Exception) {
                // Silencioso
            }
        }

        override fun recordError(
            title: String,
            throwable: Throwable?,
            extraParams: Map<String, String>,
        ) {
            try {
                val message = throwable?.message ?: "No message"
                val extra = if (extraParams.isNotEmpty()) extraParams.mapValues { it.value as Any }.toJsObject() else null
                jsRecordError(title, message, extra)
            } catch (e: Exception) {
                // Silencioso
            }
        }
    }
