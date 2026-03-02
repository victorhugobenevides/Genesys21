package com.genesys.ui.utils

/**
 * Input validation utilities
 * 
 * Usage:
 * ```kotlin
 * val emailError = ValidationHelper.validateEmail("test@example.com")
 * if (emailError != null) {
 *     // Show error
 * }
 * ```
 */
object ValidationHelper {
    
    /**
     * Validates email format
     * @return Error message if invalid, null if valid
     */
    fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email is required"
            !email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) ->
                "Invalid email format"
            else -> null
        }
    }
    
    /**
     * Validates phone number (Brazilian format)
     * @return Error message if invalid, null if valid
     */
    fun validatePhone(phone: String): String? {
        val digitsOnly = phone.replace(Regex("[^0-9]"), "")
        return when {
            phone.isBlank() -> "Phone is required"
            digitsOnly.length < 10 -> "Phone must have at least 10 digits"
            digitsOnly.length > 11 -> "Phone cannot exceed 11 digits"
            else -> null
        }
    }
    
    /**
     * Validates CPF (Brazilian tax ID)
     * @return Error message if invalid, null if valid
     */
    fun validateCPF(cpf: String): String? {
        val digitsOnly = cpf.replace(Regex("[^0-9]"), "")
        
        return when {
            cpf.isBlank() -> "CPF is required"
            digitsOnly.length != 11 -> "CPF must have 11 digits"
            digitsOnly.all { it == digitsOnly[0] } -> "Invalid CPF"
            !isValidCPF(digitsOnly) -> "Invalid CPF"
            else -> null
        }
    }
    
    /**
     * Validates password strength
     * @return Error message if weak, null if strong
     */
    fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Password is required"
            password.length < 8 -> "Password must be at least 8 characters"
            !password.any { it.isUpperCase() } -> "Password must contain uppercase letter"
            !password.any { it.isLowerCase() } -> "Password must contain lowercase letter"
            !password.any { it.isDigit() } -> "Password must contain a number"
            else -> null
        }
    }
    
    /**
     * Validates required field
     */
    fun validateRequired(value: String, fieldName: String = "Field"): String? {
        return if (value.isBlank()) {
            "$fieldName is required"
        } else null
    }
    
    /**
     * Validates minimum length
     */
    fun validateMinLength(value: String, minLength: Int, fieldName: String = "Field"): String? {
        return if (value.length < minLength) {
            "$fieldName must be at least $minLength characters"
        } else null
    }
    
    /**
     * Validates maximum length
     */
    fun validateMaxLength(value: String, maxLength: Int, fieldName: String = "Field"): String? {
        return if (value.length > maxLength) {
            "$fieldName cannot exceed $maxLength characters"
        } else null
    }
    
    /**
     * CPF validation algorithm
     */
    private fun isValidCPF(cpf: String): Boolean {
        if (cpf.length != 11) return false
        
        var sum = 0
        for (i in 0..8) {
            sum += cpf[i].toString().toInt() * (10 - i)
        }
        var checkDigit = 11 - (sum % 11)
        if (checkDigit >= 10) checkDigit = 0
        
        if (checkDigit != cpf[9].toString().toInt()) return false
        
        sum = 0
        for (i in 0..9) {
            sum += cpf[i].toString().toInt() * (11 - i)
        }
        checkDigit = 11 - (sum % 11)
        if (checkDigit >= 10) checkDigit = 0
        
        return checkDigit == cpf[10].toString().toInt()
    }
}