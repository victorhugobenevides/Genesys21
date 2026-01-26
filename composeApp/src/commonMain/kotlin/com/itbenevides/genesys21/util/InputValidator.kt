package com.itbenevides.genesys21.util

object InputValidator {
    
    fun validatePrice(input: String): String {
        // Permite apenas números, uma vírgula ou um ponto
        // Remove espaços e caracteres inválidos
        return input.filter { it.isDigit() || it == '.' || it == ',' }
            .replace(",", ".")
            .let { 
                // Garante apenas um ponto decimal
                val parts = it.split(".")
                if (parts.size > 2) {
                    parts[0] + "." + parts.drop(1).joinToString("")
                } else it
            }
    }

    fun validateStock(input: String): String {
        // Permite apenas dígitos
        return input.filter { it.isDigit() }
    }

    fun parsePrice(input: String): Double {
        return input.toDoubleOrNull() ?: 0.0
    }

    fun parseStock(input: String): Int {
        return input.toIntOrNull() ?: 0
    }
}
