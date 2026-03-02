package com.genesys.ui.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ErrorHandlerTest {

    @Test
    fun testGetErrorMessage_networkError() {
        val error = UiError.Network()
        val message = ErrorHandler.getErrorMessage(error)
        assertTrue(message.contains("connection"))
    }

    @Test
    fun testGetErrorMessage_validationError() {
        val error = UiError.Validation("Email", "invalid format")
        val message = ErrorHandler.getErrorMessage(error)
        assertTrue(message.contains("Email"))
        assertTrue(message.contains("invalid format"))
    }

    @Test
    fun testGetErrorMessage_notFoundError() {
        val error = UiError.NotFound("User")
        val message = ErrorHandler.getErrorMessage(error)
        assertTrue(message.contains("User"))
        assertTrue(message.contains("not found"))
    }

    @Test
    fun testGetErrorMessage_unauthorizedError() {
        val error = UiError.Unauthorized()
        val message = ErrorHandler.getErrorMessage(error)
        assertTrue(message.contains("permission"))
    }

    @Test
    fun testGetErrorMessage_serverError() {
        val error = UiError.ServerError()
        val message = ErrorHandler.getErrorMessage(error)
        assertTrue(message.contains("Server error") || message.contains("our end"))
    }

    @Test
    fun testGetErrorMessage_timeoutError() {
        val error = UiError.Timeout()
        val message = ErrorHandler.getErrorMessage(error)
        assertTrue(message.contains("too long") || message.contains("timeout"))
    }

    @Test
    fun testGetErrorMessage_genericError() {
        val error = UiError.Generic("Something went wrong")
        val message = ErrorHandler.getErrorMessage(error)
        assertEquals("Something went wrong", message)
    }

    @Test
    fun testGetErrorTitle_allTypes() {
        assertEquals("Connection Error", ErrorHandler.getErrorTitle(UiError.Network()))
        assertEquals("Validation Error", ErrorHandler.getErrorTitle(UiError.Validation("Field", "msg")))
        assertEquals("Not Found", ErrorHandler.getErrorTitle(UiError.NotFound()))
        assertEquals("Access Denied", ErrorHandler.getErrorTitle(UiError.Unauthorized()))
        assertEquals("Server Error", ErrorHandler.getErrorTitle(UiError.ServerError()))
        assertEquals("Timeout", ErrorHandler.getErrorTitle(UiError.Timeout()))
        assertEquals("Error", ErrorHandler.getErrorTitle(UiError.Generic("msg")))
    }

    @Test
    fun testFromException_networkException() {
        val exception = Exception("Network connection failed")
        val error = ErrorHandler.fromException(exception)
        assertTrue(error is UiError.Network)
    }

    @Test
    fun testFromException_timeoutException() {
        val exception = Exception("Request timeout after 30s")
        val error = ErrorHandler.fromException(exception)
        assertTrue(error is UiError.Timeout)
    }

    @Test
    fun testFromException_404Exception() {
        val exception = Exception("HTTP 404 Not Found")
        val error = ErrorHandler.fromException(exception)
        assertTrue(error is UiError.NotFound)
    }

    @Test
    fun testFromException_401Exception() {
        val exception = Exception("HTTP 401 Unauthorized")
        val error = ErrorHandler.fromException(exception)
        assertTrue(error is UiError.Unauthorized)
    }

    @Test
    fun testFromException_500Exception() {
        val exception = Exception("HTTP 500 Internal Server Error")
        val error = ErrorHandler.fromException(exception)
        assertTrue(error is UiError.ServerError)
    }

    @Test
    fun testFromException_genericException() {
        val exception = Exception("Unknown error")
        val error = ErrorHandler.fromException(exception)
        assertTrue(error is UiError.Generic)
        assertEquals("Unknown error", (error as UiError.Generic).message)
    }
}