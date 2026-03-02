package com.genesys.ui.utils

enum class HapticType {
    LIGHT,
    MEDIUM,
    HEAVY,
    SUCCESS,
    ERROR,
    WARNING
}

expect object HapticFeedback {
    fun perform(type: HapticType)
    fun isAvailable(): Boolean
}