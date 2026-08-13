package com.itbenevides.genesys21.util

object PrivacyUtils {

    /**
     * Anonymizes an IP address by masking the last octet (IPv4) or last interface (IPv6).
     * e.g., 192.168.1.100 -> 192.168.1.xxx
     */
    fun anonymizeIp(ip: String?): String {
        if (ip == null || ip.isBlank()) return "unknown"

        return if (ip.contains(".")) {
            // IPv4
            val parts = ip.split(".")
            if (parts.size == 4) {
                "${parts[0]}.${parts[1]}.${parts[2]}.xxx"
            } else {
                "xxx.xxx.xxx.xxx"
            }
        } else if (ip.contains(":")) {
            // IPv6
            val parts = ip.split(":")
            if (parts.size >= 2) {
                parts.dropLast(1).joinToString(":") + ":xxxx"
            } else {
                "xxxx:xxxx"
            }
        } else {
            "xxx.xxx.xxx.xxx"
        }
    }

    /**
     * Sanitizes a string by removing potential sensitive patterns like tokens or passwords.
     */
    fun sanitizeData(text: String?): String? {
        if (text == null) return null

        var sanitized = text
        // Remove common token/secret patterns
        val patterns = listOf(
            "bearer\\s+[\\w\\-\\.]+".toRegex(RegexOption.IGNORE_CASE),
            "password\\s*[:=]\\s*[^,\\s]+".toRegex(RegexOption.IGNORE_CASE),
            "secret\\s*[:=]\\s*[^,\\s]+".toRegex(RegexOption.IGNORE_CASE),
            "token\\s*[:=]\\s*[^,\\s]+".toRegex(RegexOption.IGNORE_CASE),
            "api_key\\s*[:=]\\s*[^,\\s]+".toRegex(RegexOption.IGNORE_CASE)
        )

        patterns.forEach { regex ->
            sanitized = sanitized?.replace(regex, "[REDACTED]")
        }

        return sanitized
    }

    /**
     * Masks an email address: v...n@gmail.com
     */
    fun maskEmail(email: String?): String {
        if (email == null || !email.contains("@")) return "****"
        val parts = email.split("@")
        val name = parts[0]
        val domain = parts[1]
        if (name.length <= 2) return "${name.first()}***@$domain"
        return "${name.first()}...${name.last()}@$domain"
    }

    /**
     * Masks a phone number: (11) 9****-1234
     */
    fun maskPhone(phone: String?): String {
        if (phone == null || phone.isBlank()) return "****"
        val digits = phone.filter { it.isDigit() }
        if (digits.length < 10) return "****"
        return "(${digits.take(2)}) ${digits[2]}****-${digits.takeLast(4)}"
    }

    /**
     * Masks an address to show only the essential: Rua A****, 100
     */
    fun maskAddress(street: String?, number: String?): String {
        if (street == null) return "****"
        val firstWord = street.split(" ").firstOrNull() ?: "Rua"
        return "$firstWord ****, $number"
    }
}
