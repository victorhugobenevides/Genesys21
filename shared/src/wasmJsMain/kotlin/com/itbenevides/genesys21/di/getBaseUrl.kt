package com.itbenevides.genesys21.di

@JsFun("() => window.location.origin")
private external fun getJsOrigin(): JsString

actual fun getBaseUrl(): String {
    // Retorna a origem atual (ex: http://localhost:8081 ou https://victorbenevides.dev)
    // Isso garante que as chamadas sejam relativas ao domínio atual,
    // permitindo que o Nginx ou Proxy gerencie o redirecionamento para o backend.
    return getJsOrigin().toString()
}
