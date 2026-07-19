package com.itbenevides.genesys21

class WasmPlatform : Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("() => window.print()")
external fun jsPrint()

actual fun triggerPrint() {
    jsPrint()
}
