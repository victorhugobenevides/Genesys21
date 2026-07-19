package com.itbenevides.genesys21

import kotlinx.browser.window

class JsPlatform : Platform {
    override val name: String = "Web with Kotlin/JS"
}

actual fun getPlatform(): Platform = JsPlatform()

actual fun triggerPrint() {
    window.print()
}
