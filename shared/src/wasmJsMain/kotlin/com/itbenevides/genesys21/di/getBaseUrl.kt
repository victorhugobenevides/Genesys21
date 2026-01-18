package com.itbenevides.genesys21.di

/**
 * Obtém a URL base de forma dinâmica no WasmJS sem depender de bibliotecas externas (como kotlinx.browser).
 * Isso resolve o erro de 'Unresolved reference browser' no módulo shared.
 */
@JsFun("() => window.location.hostname")
private external fun getJsHostname(): JsString

@JsFun("() => window.location.protocol")
private external fun getJsProtocol(): JsString

actual fun getBaseUrl(): String {
    val host = getJsHostname().toString()
    val protocol = getJsProtocol().toString()
    
    return if (host == "localhost" || host == "127.0.0.1") {
        "$protocol//localhost:8080"
    } else {
        "$protocol//$host:8080"
    }
}
