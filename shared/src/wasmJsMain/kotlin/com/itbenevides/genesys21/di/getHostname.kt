package com.itbenevides.genesys21.di

@JsFun("() => window.location.hostname")
private external fun getJsHostname(): JsString

actual fun getHostname(): String = getJsHostname().toString()
