package com.itbenevides.genesys21

class JVMPlatform : Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun triggerPrint() {
    // Not implemented for JVM
}
