package com.itbenevides.genesys21.di

import kotlinx.browser.window

actual fun getBaseUrl(): String {
    return try {
        val hostname = window.location.hostname
        val protocol = window.location.protocol
        if (hostname == "localhost" || hostname == "127.0.0.1") {
            "$protocol//localhost:8080"
        } else {
            "$protocol//$hostname"
        }
    } catch (e: Exception) {
        "http://localhost:8080"
    }
}

actual fun getCurrentUrl(): String? {
    return try {
        window.location.href
    } catch (e: Exception) {
        null
    }
}
