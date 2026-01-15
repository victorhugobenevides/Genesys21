package com.itbenevides.genesys21.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val imageUrl: String
)

@Serializable
sealed class PageComponent {
    abstract val customLabel: String?
    abstract val isTransparent: Boolean
    abstract val isRounded: Boolean

    @Serializable
    data class Text(
        val content: String, 
        val fontSize: Int = 16,
        override val customLabel: String? = null,
        override val isTransparent: Boolean = false,
        override val isRounded: Boolean = false
    ) : PageComponent()

    @Serializable
    data class Header(
        val title: String,
        override val customLabel: String? = null,
        override val isTransparent: Boolean = false,
        override val isRounded: Boolean = false
    ) : PageComponent()

    @Serializable
    data class Image(
        val url: String, 
        val string: String,
        override val customLabel: String? = null,
        override val isTransparent: Boolean = false,
        override val isRounded: Boolean = false
    ) : PageComponent()

    @Serializable
    data class Logo(
        val url: String,
        val size: Int = 64,
        override val customLabel: String? = null,
        override val isTransparent: Boolean = true,
        override val isRounded: Boolean = true
    ) : PageComponent()

    @Serializable
    data class ProductList(
        val products: List<Product>,
        override val customLabel: String? = null,
        override val isTransparent: Boolean = false,
        override val isRounded: Boolean = false
    ) : PageComponent()
}

@Serializable
data class Page(
    val id: String,
    val title: String,
    val components: List<PageComponent> = emptyList()
)
