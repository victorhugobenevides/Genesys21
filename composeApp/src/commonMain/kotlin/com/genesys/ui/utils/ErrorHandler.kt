package com.genesys.ui.utils

sealed class UiError {
    data class Network(val message: String = "Network connection failed") : UiError()
    data class Validation(val field: String, val message: String) : UiError()
    data class NotFound(val resource: String = "Resource not found") : UiError()
    data class Unauthorized(val message: String = "Unauthorized access") : UiError()
    data class ServerError(val message: String = "Server error occurred") : UiError()
    data class Generic(val message: String) : UiError()
    data class Timeout(val message: String = "Request timed out") : UiError()
}

object ErrorHandler {
    fun getErrorMessage(error: UiError): String {
        return when (error) {
            is UiError.Network -> 
                "Unable to connect. Please check your internet connection."
            is UiError.Validation -> 
                "${error.field}: ${error.message}"
            is UiError.NotFound -> 
                "The requested ${error.resource} could not be found."
            is UiError.Unauthorized -> 
                "You don't have permission to access this resource."
            is UiError.ServerError -> 
                "Something went wrong on our end. Please try again later."
            is UiError.Generic -> 
                error.message
            is UiError.Timeout -> 
                "The request took too long. Please try again."
        }
    }
    
    fun getErrorTitle(error: UiError): String {
        return when (error) {
            is UiError.Network -> "Connection Error"
            is UiError.Validation -> "Validation Error"
            is UiError.NotFound -> "Not Found"
            is UiError.Unauthorized -> "Access Denied"
            is UiError.ServerError -> "Server Error"
            is UiError.Timeout -> "Timeout"
            is UiError.Generic -> "Error"
        }
    }
    
    fun fromException(exception: Exception): UiError {
        return when {
            exception.message?.contains("network", ignoreCase = true) == true ->
                UiError.Network()
            exception.message?.contains("timeout", ignoreCase = true) == true ->
                UiError.Timeout()
            exception.message?.contains("404", ignoreCase = true) == true ->
                UiError.NotFound()
            exception.message?.contains("401", ignoreCase = true) == true ||
            exception.message?.contains("403", ignoreCase = true) == true ->
                UiError.Unauthorized()
            exception.message?.contains("500", ignoreCase = true) == true ||
            exception.message?.contains("502", ignoreCase = true) == true ||
            exception.message?.contains("503", ignoreCase = true) == true ->
                UiError.ServerError()
            else ->
                UiError.Generic(exception.message ?: "An error occurred")
        }
    }
}