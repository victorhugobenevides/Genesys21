package com.genesys.ui.utils

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult

/**
 * Centralized feedback manager for Toast/Snackbar messages
 * 
 * Usage:
 * ```kotlin
 * val snackbarHostState = remember { SnackbarHostState() }
 * val feedbackManager = remember { FeedbackManager(snackbarHostState) }
 * 
 * feedbackManager.showSuccess("Item saved!")
 * ```
 */
class FeedbackManager(
    private val snackbarHostState: SnackbarHostState
) {
    suspend fun showSuccess(
        message: String,
        actionLabel: String? = null,
        duration: SnackbarDuration = SnackbarDuration.Short
    ): SnackbarResult {
        return snackbarHostState.showSnackbar(
            message = "✓ $message",
            actionLabel = actionLabel,
            duration = duration
        )
    }
    
    suspend fun showError(
        message: String,
        actionLabel: String? = "Retry",
        duration: SnackbarDuration = SnackbarDuration.Long
    ): SnackbarResult {
        return snackbarHostState.showSnackbar(
            message = "✕ $message",
            actionLabel = actionLabel,
            duration = duration
        )
    }
    
    suspend fun showInfo(
        message: String,
        actionLabel: String? = null,
        duration: SnackbarDuration = SnackbarDuration.Short
    ): SnackbarResult {
        return snackbarHostState.showSnackbar(
            message = message,
            actionLabel = actionLabel,
            duration = duration
        )
    }
    
    suspend fun showWarning(
        message: String,
        actionLabel: String? = null,
        duration: SnackbarDuration = SnackbarDuration.Long
    ): SnackbarResult {
        return snackbarHostState.showSnackbar(
            message = "⚠ $message",
            actionLabel = actionLabel,
            duration = duration
        )
    }
}