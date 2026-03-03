package com.itbenevides.genesys21.di

/**
 * Funções externas para interagir com o objeto 'window' no Navegador (WasmJs).
 */
@JsFun("() => window.location.hostname")
external fun getWindowLocationHostname(): String

@JsFun("() => window.location.protocol")
external fun getWindowLocationProtocol(): String

@JsFun("() => window.location.href")
external fun getWindowLocationHref(): String

actual fun getBaseUrl(): String {
    val protocol = getWindowLocationProtocol()
    val hostname = getWindowLocationHostname()
    // Lógica para alternar entre localhost (desenvolvimento) e produção
    return if (hostname == "localhost" || hostname == "127.0.0.1") {
        "$protocol//localhost:8080"
    } else {
        "$protocol//$hostname"
    }
}

actual fun getHostname(): String {
    return getWindowLocationHostname()
}

actual fun getCurrentUrl(): String? {
    return getWindowLocationHref()
}
