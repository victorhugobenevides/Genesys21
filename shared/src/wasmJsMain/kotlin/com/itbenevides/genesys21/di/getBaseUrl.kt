package com.itbenevides.genesys21.di

@JsFun("() => window.location.hostname")
private external fun getJsHostname(): JsString

@JsFun("() => window.location.origin")
private external fun getJsOrigin(): JsString

actual fun getBaseUrl(): String {
    val hostname = getJsHostname().toString()
    
    // Se estiver rodando localmente no navegador, aponta para a porta 8080 do backend.
    // Em produção (quando não é localhost), assume que o frontend e o backend 
    // estão sob o mesmo domínio gerenciado por um proxy (Nginx).
    return if (hostname == "localhost" || hostname == "127.0.0.1") {
        "http://localhost:8080"
    } else {
        getJsOrigin().toString()
    }
}
