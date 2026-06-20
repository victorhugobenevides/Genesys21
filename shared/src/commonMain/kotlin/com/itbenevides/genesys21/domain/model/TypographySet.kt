package com.itbenevides.genesys21.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class TypographySet {
    DEFAULT,
    MODERN_SANS,
    CLASSIC_SERIF,
    MINIMAL_MONO,
    PLAYFUL_ROUNDED
}
