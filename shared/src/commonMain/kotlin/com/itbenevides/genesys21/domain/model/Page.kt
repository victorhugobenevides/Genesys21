package com.itbenevides.genesys21.domain.model

import kotlinx.serialization.Serializable

@Serializable
sealed class PageComponent {
    @Serializable
    data class Text(val content: String, val fontSize: Int = 16) : PageComponent()
    @Serializable
    data class Header(val title: String) : PageComponent()
    @Serializable
    data class Image(val url: String, val string: String) : PageComponent()
}

@Serializable
data class Page(
    val id: String,
    val title: String,
    val components: List<PageComponent> = emptyList()
)
