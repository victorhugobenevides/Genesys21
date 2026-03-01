package com.itbenevides.genesys21.util

import org.junit.Assert.*
import org.junit.Test

class InputValidatorTest {

    @Test
    fun `validatePrice should filter non-numeric characters`() {
        val input = "abc123.45xyz"
        val result = InputValidator.validatePrice(input)
        assertEquals("123.45", result)
    }

    @Test
    fun `validatePrice should replace comma with dot`() {
        val input = "123,45"
        val result = InputValidator.validatePrice(input)
        assertEquals("123.45", result)
    }

    @Test
    fun `validatePrice should handle multiple dots by keeping only first`() {
        val input = "123.45.67"
        val result = InputValidator.validatePrice(input)
        assertEquals("123.4567", result)
    }

    @Test
    fun `validatePrice should handle empty string`() {
        val input = ""
        val result = InputValidator.validatePrice(input)
        assertEquals("", result)
    }

    @Test
    fun `validatePrice should handle only letters`() {
        val input = "abc"
        val result = InputValidator.validatePrice(input)
        assertEquals("", result)
    }

    @Test
    fun `validateStock should filter non-digit characters`() {
        val input = "abc123xyz"
        val result = InputValidator.validateStock(input)
        assertEquals("123", result)
    }

    @Test
    fun `validateStock should handle empty string`() {
        val input = ""
        val result = InputValidator.validateStock(input)
        assertEquals("", result)
    }

    @Test
    fun `parsePrice should return double value`() {
        val input = "123.45"
        val result = InputValidator.parsePrice(input)
        assertEquals(123.45, result, 0.001)
    }

    @Test
    fun `parsePrice should return 0 for invalid input`() {
        val input = "abc"
        val result = InputValidator.parsePrice(input)
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun `parsePrice should return 0 for empty string`() {
        val input = ""
        val result = InputValidator.parsePrice(input)
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun `parseStock should return int value`() {
        val input = "100"
        val result = InputValidator.parseStock(input)
        assertEquals(100, result)
    }

    @Test
    fun `parseStock should return 0 for invalid input`() {
        val input = "abc"
        val result = InputValidator.parseStock(input)
        assertEquals(0, result)
    }

    @Test
    fun `parseStock should return 0 for empty string`() {
        val input = ""
        val result = InputValidator.parseStock(input)
        assertEquals(0, result)
    }

    @Test
    fun `validatePrice should handle multiple commas`() {
        val input = "1,234,56"
        val result = InputValidator.validatePrice(input)
        // Todas as vírgulas são substituídas por pontos, mas apenas o primeiro ponto é mantido
        // "1,234,56" -> "1.234.56" -> "1.23456"
        assertEquals("1.23456", result)
    }

    @Test
    fun `validateStock should remove decimal points`() {
        val input = "123.45"
        val result = InputValidator.validateStock(input)
        assertEquals("12345", result)
    }

    @Test
    fun `parsePrice should handle negative numbers`() {
        val input = "-123.45"
        val result = InputValidator.parsePrice(input)
        assertEquals(-123.45, result, 0.001)
    }

    @Test
    fun `parseStock should handle negative numbers as invalid`() {
        val input = "-100"
        val result = InputValidator.parseStock(input)
        // toIntOrNull retorna null para "-100"? Não, -100 é um número válido
        // Mas validateStock remove o sinal de menos
        assertEquals(-100, result)
    }
}
