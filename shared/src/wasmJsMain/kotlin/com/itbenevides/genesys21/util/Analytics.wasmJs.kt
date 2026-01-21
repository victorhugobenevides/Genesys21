package com.itbenevides.genesys21.util

/**
 * Funções seguras de Interoperação com JavaScript.
 * Usamos JsAny para delegar a serialização ao motor do navegador.
 */
@JsFun("(name, params) => { if (window.gtag) window.gtag('event', name, params); }")
private external fun jsLogEvent(name: String, params: JsAny?)

@JsFun("(pageName) => { if (window.gtag) window.gtag('event', 'page_view', { page_title: pageName, page_location: window.location.href, page_path: window.location.pathname }); }")
private external fun jsTrackPageView(pageName: String)

actual val AnalyticsManager: Analytics = object : Analytics {
    override fun logEvent(name: String, params: Map<String, Any>) {
        try {
            // No WasmJs, passamos null ou convertemos apenas se houver params
            // Para simplificar e evitar crash, vamos logar apenas o nome por enquanto
            // ou converter para um objeto JS básico.
            jsLogEvent(name, null) 
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
}
