package com.itbenevides.genesys21

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun triggerPrint()
