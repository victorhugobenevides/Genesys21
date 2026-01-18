package com.itbenevides.genesys21.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val imageUrl: String,
    val description: String = "",
    val category: String = "",
    val stock: Int = 0
)

@Serializable
enum class PageThemeConfig {
    DEFAULT, OCEAN, FOREST, CANDY, DARK,
    SUNSET, BERRY, MINIMAL, VINTAGE, NEON
}

@Serializable
sealed class PageComponent {
    abstract val customLabel: String?
    abstract val isTransparent: Boolean
    abstract val isRounded: Boolean
    abstract val isFilterable: Boolean

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.Text")
    data class Text(
        val content: String, 
        val fontSize: Int = 16,
        override val customLabel: String? = null,
        override val isTransparent: Boolean = false,
        override val isRounded: Boolean = false,
        override val isFilterable: Boolean = false
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.Header")
    data class Header(
        val title: String,
        override val customLabel: String? = null,
        override val isTransparent: Boolean = false,
        override val isRounded: Boolean = false,
        override val isFilterable: Boolean = false
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.Image")
    data class Image(
        val url: String, 
        val string: String,
        val size: Int = 200,
        override val customLabel: String? = null,
        override val isTransparent: Boolean = false,
        override val isRounded: Boolean = false,
        override val isFilterable: Boolean = false
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.Button")
    data class Button(
        val text: String,
        val url: String,
        val iconName: String? = null,
        override val customLabel: String? = null,
        override val isTransparent: Boolean = false,
        override val isRounded: Boolean = true,
        override val isFilterable: Boolean = false
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.ProductList")
    data class ProductList(
        val products: List<Product>,
        val isHorizontal: Boolean = false,
        override val customLabel: String? = null,
        override val isTransparent: Boolean = false,
        override val isRounded: Boolean = false,
        override val isFilterable: Boolean = true
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.Filter")
    data class Filter(
        val placeholder: String = "Filtrar conteúdo...",
        override val customLabel: String? = null,
        override val isTransparent: Boolean = false,
        override val isRounded: Boolean = false,
        override val isFilterable: Boolean = false
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.CategoryFilter")
    data class CategoryFilter(
        override val customLabel: String? = null,
        override val isTransparent: Boolean = false,
        override val isRounded: Boolean = false,
        override val isFilterable: Boolean = false
    ) : PageComponent()

    @Serializable
    @SerialName("unknown")
    data class Unknown(
        override val customLabel: String? = "Componente Antigo",
        override val isTransparent: Boolean = true,
        override val isRounded: Boolean = false,
        override val isFilterable: Boolean = false
    ) : PageComponent()
}

@Serializable
data class Page(
    val id: String,
    val title: String,
    val ownerId: String? = null,
    val components: List<PageComponent> = emptyList(),
    val theme: PageThemeConfig = PageThemeConfig.DEFAULT
)
