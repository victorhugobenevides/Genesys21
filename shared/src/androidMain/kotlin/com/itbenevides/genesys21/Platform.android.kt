package com.itbenevides.genesys21

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun triggerPrint() {
    // Not implemented for Android
}

actual fun openUrlInCurrentTab(url: String) {
    // No Android sempre abre em navegador externo ou WebView
}

actual fun openUrlInNewTab(url: String) {
    // Mesma lógica no Mobile
}

actual fun getUrlSearchParameters(): String = ""
