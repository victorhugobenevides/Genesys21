package com.itbenevides.genesys21

class WasmPlatform : Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

@JsFun("() => window.print()")
external fun jsPrint()

actual fun triggerPrint() {
    jsPrint()
}
