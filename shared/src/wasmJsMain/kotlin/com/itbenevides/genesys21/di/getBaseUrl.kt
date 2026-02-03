package com.itbenevides.genesys21.di

@JsFun("() => window.location.origin")
private external fun getJsOrigin(): JsString

actual fun getBaseUrl(): String {
    // Retorna a origem atual (ex: http://localhost:8081 ou http://radarani.site).
    // Isso garante que as chamadas /api sejam interceptadas pelo Proxy (Webpack ou Nginx).
    return getJsOrigin().toString()
}
