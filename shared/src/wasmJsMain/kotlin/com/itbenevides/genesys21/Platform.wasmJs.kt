package com.itbenevides.genesys21

class WasmPlatform : Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

@JsFun("() => window.print()")
external fun jsPrint()

@JsFun("(url) => { window.location.href = url; }")
external fun jsOpenUrl(url: String)

@JsFun("() => window.location.search")
external fun jsGetSearch(): JsString

actual fun triggerPrint() {
    jsPrint()
}

actual fun openUrlInCurrentTab(url: String) {
    jsOpenUrl(url)
}

actual fun getUrlSearchParameters(): String {
    return jsGetSearch().toString()
}
