package com.itbenevides.genesys21.domain.model

import kotlinx.serialization.Serializable

/**
 * DTO (Data Transfer Object) para exposição pública de perfis de usuário.
 * Contém apenas o estritamente necessário para identificação visual,
 * protegendo dados sensíveis como e-mail, telefone e permissões internas.
 */
@Serializable
data class PublicUserProfile(
    val id: String,
    val name: String,
    val avatarUrl: String? = null
)

fun UserProfile.toPublic() = PublicUserProfile(
    id = id,
    name = name,
    avatarUrl = avatarUrl
)
