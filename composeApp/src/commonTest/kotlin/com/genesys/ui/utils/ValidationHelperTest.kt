package com.genesys.ui.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ValidationHelperTest {

    @Test
    fun testValidateEmail_validEmail_returnsNull() {
        assertNull(ValidationHelper.validateEmail("test@example.com"))
        assertNull(ValidationHelper.validateEmail("user.name+tag@domain.co"))
    }

    @Test
    fun testValidateEmail_emptyEmail_returnsError() {
        assertEquals("Email is required", ValidationHelper.validateEmail(""))
        assertEquals("Email is required", ValidationHelper.validateEmail("   "))
    }

    @Test
    fun testValidateEmail_invalidFormat_returnsError() {
        assertEquals("Invalid email format", ValidationHelper.validateEmail("invalid"))
        assertEquals("Invalid email format", ValidationHelper.validateEmail("@domain.com"))
        assertEquals("Invalid email format", ValidationHelper.validateEmail("user@"))
    }

    @Test
    fun testValidatePhone_validPhone_returnsNull() {
        assertNull(ValidationHelper.validatePhone("1234567890"))
        assertNull(ValidationHelper.validatePhone("12345678901"))
        assertNull(ValidationHelper.validatePhone("(11) 98765-4321"))
    }

    @Test
    fun testValidatePhone_emptyPhone_returnsError() {
        assertEquals("Phone is required", ValidationHelper.validatePhone(""))
    }

    @Test
    fun testValidatePhone_tooShort_returnsError() {
        assertEquals("Phone must have at least 10 digits", ValidationHelper.validatePhone("123"))
    }

    @Test
    fun testValidatePhone_tooLong_returnsError() {
        assertEquals("Phone cannot exceed 11 digits", ValidationHelper.validatePhone("123456789012"))
    }

    @Test
    fun testValidateCPF_validCPF_returnsNull() {
        assertNull(ValidationHelper.validateCPF("11144477735"))
        assertNull(ValidationHelper.validateCPF("111.444.777-35"))
    }

    @Test
    fun testValidateCPF_emptyCPF_returnsError() {
        assertEquals("CPF is required", ValidationHelper.validateCPF(""))
    }

    @Test
    fun testValidateCPF_invalidLength_returnsError() {
        assertEquals("CPF must have 11 digits", ValidationHelper.validateCPF("123"))
    }

    @Test
    fun testValidateCPF_allSameDigits_returnsError() {
        assertEquals("Invalid CPF", ValidationHelper.validateCPF("11111111111"))
    }

    @Test
    fun testValidateCPF_invalidChecksum_returnsError() {
        assertEquals("Invalid CPF", ValidationHelper.validateCPF("12345678901"))
    }

    @Test
    fun testValidatePassword_validPassword_returnsNull() {
        assertNull(ValidationHelper.validatePassword("Password123"))
        assertNull(ValidationHelper.validatePassword("MySecure1Pass"))
    }

    @Test
    fun testValidatePassword_emptyPassword_returnsError() {
        assertEquals("Password is required", ValidationHelper.validatePassword(""))
    }

    @Test
    fun testValidatePassword_tooShort_returnsError() {
        assertEquals("Password must be at least 8 characters", ValidationHelper.validatePassword("Pass1"))
    }

    @Test
    fun testValidatePassword_noUppercase_returnsError() {
        assertEquals("Password must contain uppercase letter", ValidationHelper.validatePassword("password123"))
    }

    @Test
    fun testValidatePassword_noLowercase_returnsError() {
        assertEquals("Password must contain lowercase letter", ValidationHelper.validatePassword("PASSWORD123"))
    }

    @Test
    fun testValidatePassword_noDigit_returnsError() {
        assertEquals("Password must contain a number", ValidationHelper.validatePassword("Password"))
    }

    @Test
    fun testValidateRequired_nonEmpty_returnsNull() {
        assertNull(ValidationHelper.validateRequired("Value"))
    }

    @Test
    fun testValidateRequired_empty_returnsError() {
        assertEquals("Field is required", ValidationHelper.validateRequired(""))
        assertEquals("Name is required", ValidationHelper.validateRequired("", "Name"))
    }

    @Test
    fun testValidateMinLength_sufficientLength_returnsNull() {
        assertNull(ValidationHelper.validateMinLength("Hello", 3))
    }

    @Test
    fun testValidateMinLength_insufficientLength_returnsError() {
        assertEquals("Field must be at least 5 characters", ValidationHelper.validateMinLength("Hi", 5))
    }

    @Test
    fun testValidateMaxLength_withinLimit_returnsNull() {
        assertNull(ValidationHelper.validateMaxLength("Hello", 10))
    }

    @Test
    fun testValidateMaxLength_exceedsLimit_returnsError() {
        assertEquals("Field cannot exceed 3 characters", ValidationHelper.validateMaxLength("Hello", 3))
    }
}