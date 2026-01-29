package com.itbenevides.genesys21.di

import kotlinx.browser.window

actual fun getHostname(): String {
    return try {
        window.location.hostname
    } catch (e: Exception) {
        "js-browser"
    }
}
