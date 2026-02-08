package com.itbenevides.genesys21.di

@ExperimentalWasmJsInterop
@JsFun("() => window.location.hostname")
private external fun getJsHostname(): JsString

@ExperimentalWasmJsInterop
actual fun getHostname(): String = getJsHostname().toString()
