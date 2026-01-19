package com.itbenevides.genesys21.di

@JsFun("() => window.location.hostname")
private external fun getJsHostname(): JsString

@JsFun("() => window.location.origin")
private external fun getJsOrigin(): JsString

actual fun getBaseUrl(): String {
    val host = getJsHostname().toString()
    val origin = getJsOrigin().toString()
    
    // Em produção (na AWS), usamos o próprio domínio (HTTPS via Proxy do Nginx)
    // Em local, continuamos usando a porta 8080
    return if (host == "localhost" || host == "127.0.0.1") {
        "http://localhost:8080"
    } else {
        // Na AWS, todas as chamadas para /pages, /api etc serão tratadas pelo Nginx
        origin
    }
}
