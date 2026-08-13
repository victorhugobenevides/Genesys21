package com.itbenevides.genesys21.domain.model

import kotlinx.serialization.Serializable

/**
 * Definição dos Temas Elite do Genesys21 (Versão 2026).
 * Foco em alta performance visual, contraste e estética Premium.
 */
@Serializable
enum class PageThemeConfig {
    ELEGANCE,   // Slate & Gold (Luxo)
    VIBRANT,    // Electric Blue (Tech)
    NATURE,     // Deep Forest (Bem-estar)
    MONO,       // Architectural B&W (Moda)
    MIDNIGHT,   // OLED Black (Moderno)
    CANDY,      // Pastel Pop (Criativo)
    DEFAULT     // Fallback para ELEGANCE
}
