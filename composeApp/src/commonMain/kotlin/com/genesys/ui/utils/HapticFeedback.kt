package com.genesys.ui.utils

/**
 * Haptic feedback types
 */
enum class HapticType {
    LIGHT,
    MEDIUM,
    HEAVY,
    SUCCESS,
    ERROR,
    WARNING
}

/**
 * Haptic Feedback Manager
 * Platform-specific implementations should be provided
 * 
 * Note: Actual implementation requires platform-specific code
 * This is the common interface
 * 
 * Usage:
 * ```kotlin
 * HapticFeedback.perform(HapticType.SUCCESS)
 * ```
 */
expect object HapticFeedback {
    /**
     * Performs haptic feedback
     * @param type Type of haptic feedback
     */
    fun perform(type: HapticType)
    
    /**
     * Checks if haptic feedback is available on this device
     */
    fun isAvailable(): Boolean
}