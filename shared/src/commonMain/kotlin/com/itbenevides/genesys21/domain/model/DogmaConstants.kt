package com.itbenevides.genesys21.domain.model

/**
 * Constantes de segurança e regras de negócio "Dogma".
 * Nota: O OWNER_EMAIL aqui é um valor de fallback.
 * O servidor usa a variável de ambiente OWNER_EMAIL como fonte da verdade.
 */
object DogmaConstants {
    const val OWNER_EMAIL = "victorkoto@gmail.com"
    const val OWNER_UID = "mKQ9MZqG6bYhy3JqvngGpv49ZZs1"
}

fun UserProfile.isGodMode(): Boolean =
    id == DogmaConstants.OWNER_UID || email.lowercase().trim() == DogmaConstants.OWNER_EMAIL
