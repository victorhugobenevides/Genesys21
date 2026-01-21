package com.itbenevides.genesys21.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val imageUrls: List<String> = emptyList(),
    val description: String = "",
    val category: String = "",
    val stock: Int = 0
) {
    val imageUrl: String get() = imageUrls.firstOrNull() ?: ""
}

@Serializable
enum class PageThemeConfig {
    ROYAL, OCEAN, FOREST, CANDY, SUNSET, 
    BERRY, MINIMAL, VINTAGE, NORDIC, COFFEE,
    SOFT_LAVENDER, SKY_BLUE, MINT_GREEN, PEACH, LEMON,
    DARK_MODE, MIDNIGHT, NEON, DEEP_SPACE, LUXURY_GOLD,
    DEFAULT 
}

@Serializable
sealed class PageComponent {
    abstract val customLabel: String?
    abstract val isFilterable: Boolean

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.Text")
    data class Text(
        val content: String, 
        val fontSize: Int = 16,
        val textAlign: String = "LEFT", // "LEFT", "CENTER", "RIGHT"
        override val customLabel: String? = null,
        override val isFilterable: Boolean = false
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.Header")
    data class Header(
        val title: String,
        val fontSize: Int = 28,
        val textAlign: String = "LEFT", // "LEFT", "CENTER", "RIGHT"
        override val customLabel: String? = null,
        override val isFilterable: Boolean = false
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.Image")
    data class Image(
        val url: String, 
        val string: String,
        val size: Int = 200,
        val destinationPageId: String? = null,
        val isFullWidth: Boolean = false,
        override val customLabel: String? = null,
        override val isFilterable: Boolean = false
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.Button")
    data class Button(
        val text: String,
        val url: String,
        val iconName: String? = null,
        override val customLabel: String? = null,
        override val isFilterable: Boolean = false
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.ProductList")
    data class ProductList(
        val products: List<Product>,
        val isHorizontal: Boolean = false,
        override val customLabel: String? = null,
        override val isFilterable: Boolean = true
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.Filter")
    data class Filter(
        val placeholder: String = "Filtrar conteúdo...",
        override val customLabel: String? = null,
        override val isFilterable: Boolean = false
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.CategoryFilter")
    data class CategoryFilter(
        override val customLabel: String? = null,
        override val isFilterable: Boolean = false
    ) : PageComponent()

    @Serializable
    @SerialName("unknown")
    data class Unknown(
        override val customLabel: String? = "Componente Antigo",
        override val isFilterable: Boolean = false
    ) : PageComponent()
}

@Serializable
data class Page(
    val id: String,
    val title: String,
    val ownerId: String? = null,
    val customDomain: String? = null,
    val whatsapp: String? = null,
    val components: List<PageComponent> = emptyList(),
    val theme: PageThemeConfig = PageThemeConfig.ROYAL
)
