package com.itbenevides.genesys21.di

import kotlinx.browser.window

actual fun getBaseUrl(): String {
    // Detecta o IP ou Domínio atual (ex: 54.232.xx.xx) e aponta para a porta da API
    val host = window.location.hostname
    val protocol = window.location.protocol
    
    return if (host == "localhost" || host == "127.0.0.1") {
        "$protocol//localhost:8080"
    } else {
        "$protocol//$host:8080"
    }
}
