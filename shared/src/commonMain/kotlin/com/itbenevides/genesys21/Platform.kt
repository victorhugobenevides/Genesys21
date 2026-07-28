package com.itbenevides.genesys21

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun triggerPrint()

expect fun openUrlInCurrentTab(url: String)

expect fun getUrlSearchParameters(): String
