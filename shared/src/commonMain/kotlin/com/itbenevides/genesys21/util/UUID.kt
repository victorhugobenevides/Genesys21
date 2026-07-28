package com.itbenevides.genesys21.util

import kotlinx.datetime.Clock

object GenesysUUID {
    /**
     * Gera um ID pseudo-aleatório compatível com KMP.
     * Em produção, recomenda-se usar uma biblioteca real de UUID.
     */
    fun randomUUID(): String {
        val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val timestamp = Clock.System.now().toEpochMilliseconds().toString(36)
        val random = (1..8).map { chars.random() }.joinToString("")
        return "${timestamp}-${random}"
    }
}
