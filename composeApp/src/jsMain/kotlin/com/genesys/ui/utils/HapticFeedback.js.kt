package com.genesys.ui.utils

actual object HapticFeedback {
    actual fun perform(type: HapticType) {
        // Not implemented for JS
    }

    actual fun isAvailable(): Boolean = false
}
