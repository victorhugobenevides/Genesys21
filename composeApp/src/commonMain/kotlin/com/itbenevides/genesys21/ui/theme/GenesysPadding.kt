package com.itbenevides.genesys21.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Tokens de padding semânticos para o Design System.
 * 
 * Esses tokens garantem consistência visual em todo o aplicativo
 * e facilitam manutenção e adaptação para diferentes densidades de tela.
 */
object GenesysPadding {
    
    /**
     * Padding mínimo para elementos compactos.
     * Use para: Ícones pequenos, badges, indicadores.
     */
    val Minimal = 4.dp
    
    /**
     * Padding compacto para elementos densos.
     * Use para: Chips, tags, botões pequenos, listas compactas.
     */
    val Compact = 8.dp
    
    /**
     * Padding padrão para a maioria dos componentes.
     * Use para: Cards, containers, seções, listas padrão.
     * 
     * Este é o valor recomendado para 90% dos casos.
     */
    val Default = 16.dp
    
    /**
     * Padding relaxado para layouts espaçados.
     * Use para: Cards destacados, modais, bottom sheets, formulários.
     */
    val Relaxed = 24.dp
    
    /**
     * Padding grande para seções importantes.
     * Use para: Cards principais, hero sections, onboarding.
     */
    val Large = 32.dp
}

/**
 * Tokens de padding específicos para cards.
 */
object CardPadding {
    /** Card pequeno: stats, badges, indicadores. */
    val Small = GenesysPadding.Compact // 8.dp
    
    /** Card padrão: lista de produtos, informações. */
    val Medium = GenesysPadding.Default // 16.dp
    
    /** Card grande: destaques, hero cards, modais. */
    val Large = GenesysPadding.Relaxed // 24.dp
}

/**
 * Tokens de padding para formulários.
 */
object FormPadding {
    /** Espaçamento entre campos de formulário. */
    val FieldSpacing = GenesysPadding.Default // 16.dp
    
    /** Padding interno de campos de input. */
    val InputPadding = GenesysPadding.Compact // 8.dp
    
    /** Padding da seção de formulário completo. */
    val SectionPadding = GenesysPadding.Relaxed // 24.dp
}

/**
 * Tokens de padding para listas.
 */
object ListPadding {
    /** Espaçamento entre itens de lista compacta. */
    val ItemSpacingCompact = GenesysPadding.Minimal // 4.dp
    
    /** Espaçamento padrão entre itens de lista. */
    val ItemSpacingDefault = GenesysPadding.Compact // 8.dp
    
    /** Espaçamento relaxado entre itens de lista. */
    val ItemSpacingRelaxed = GenesysPadding.Default // 16.dp
    
    /** Padding da lista completa nas bordas. */
    val ContentPadding = GenesysPadding.Default // 16.dp
}

/**
 * Tokens de padding para botões.
 */
object ButtonPadding {
    /** Padding horizontal padrão de botões. */
    val Horizontal = 24.dp
    
    /** Padding vertical padrão de botões. */
    val Vertical = 12.dp
    
    /** Padding para botões compactos. */
    val CompactHorizontal = 16.dp
    
    /** Padding para botões largos (fillMaxWidth). */
    val FillWidthHorizontal = GenesysPadding.Default // 16.dp
}