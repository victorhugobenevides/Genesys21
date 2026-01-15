package com.itbenevides.genesys21.domain.model

import kotlinx.serialization.Serializable

@Serializable
sealed class PageComponent {
    // Adicionamos um label customizável para cada componente
    abstract val customLabel: String?

    @Serializable
    data class Text(
        val content: String, 
        val fontSize: Int = 16,
        override val customLabel: String? = null
    ) : PageComponent()

    @Serializable
    data class Header(
        val title: String,
        override val customLabel: String? = null
    ) : PageComponent()

    @Serializable
    data class Image(
        val url: String, 
        val string: String,
        override val customLabel: String? = null
    ) : PageComponent()
}

@Serializable
data class Page(
    val id: String,
    val title: String,
    val components: List<PageComponent> = emptyList()
)
