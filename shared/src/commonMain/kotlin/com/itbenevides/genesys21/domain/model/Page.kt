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
sealed class PageComponent {
    abstract val customLabel: String?
    abstract val isTransparent: Boolean
    abstract val isRounded: Boolean

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.Text")
    data class Text(
        val content: String, 
        val fontSize: Int = 16,
        override val customLabel: String? = null,
        override val isTransparent: Boolean = false,
        override val isRounded: Boolean = false
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.Header")
    data class Header(
        val title: String,
        override val customLabel: String? = null,
        override val isTransparent: Boolean = false,
        override val isRounded: Boolean = false
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.Image")
    data class Image(
        val url: String, 
        val string: String,
        val size: Int = 200,
        override val customLabel: String? = null,
        override val isTransparent: Boolean = false,
        override val isRounded: Boolean = false
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.ProductList")
    data class ProductList(
        val products: List<Product>,
        val isHorizontal: Boolean = false,
        override val customLabel: String? = null,
        override val isTransparent: Boolean = false,
        override val isRounded: Boolean = false
    ) : PageComponent()

    // Fallback para evitar crash com componentes antigos (como Logo)
    @Serializable
    @SerialName("unknown")
    data class Unknown(
        override val customLabel: String? = "Componente Antigo",
        override val isTransparent: Boolean = true,
        override val isRounded: Boolean = false
    ) : PageComponent()
}

@Serializable
data class Page(
    val id: String,
    val title: String,
    val ownerId: String? = null,
    val components: List<PageComponent> = emptyList()
)
