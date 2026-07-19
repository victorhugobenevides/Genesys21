package com.itbenevides.genesys21.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
sealed class PageComponent {
    abstract val customLabel: String?
    abstract val isFilterable: Boolean

    // Campos de Redirecionamento Comuns
    abstract val destinationUrl: String?
    abstract val destinationPageId: String?

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.Text")
    data class Text(
        val content: String,
        val fontSize: Int = 16,
        val textAlign: String = "START",
        val fontWeight: String = "NORMAL",
        val isUppercase: Boolean = false,
        val usePrimaryColor: Boolean = false,
        @Transient
        override val customLabel: String? = null,
        @Transient
        override val isFilterable: Boolean = false,
        override val destinationUrl: String? = null,
        override val destinationPageId: String? = null,
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.Header")
    data class Header(
        val title: String,
        val isUppercase: Boolean = false,
        val fontWeight: String = "BOLD",
        val textAlign: String = "START",
        val usePrimaryColor: Boolean = false,
        val fontSize: Int = 24,
        @Transient
        override val customLabel: String? = null,
        @Transient
        override val isFilterable: Boolean = false,
        override val destinationUrl: String? = null,
        override val destinationPageId: String? = null,
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.Image")
    data class Image(
        val url: String,
        val caption: String? = null,
        val aspectRatio: Float = 1f,
        val size: Int = 200,
        val isCircular: Boolean = false,
        val isFullWidth: Boolean = false,
        val isRounded: Boolean = false,
        @Transient
        override val customLabel: String? = null,
        @Transient
        override val isFilterable: Boolean = false,
        override val destinationUrl: String? = null,
        override val destinationPageId: String? = null,
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.Button")
    data class Button(
        val text: String,
        val url: String,
        val isPrimary: Boolean = true,
        @Transient
        override val customLabel: String? = null,
        @Transient
        override val isFilterable: Boolean = false,
        override val destinationUrl: String? = null,
        override val destinationPageId: String? = null,
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.ProductList")
    data class ProductList(
        val products: List<Product> = emptyList(),
        val title: String = "Lista de Produtos",
        val isHorizontal: Boolean = false,
        override val customLabel: String? = null,
        override val isFilterable: Boolean = true,
        override val destinationUrl: String? = null,
        override val destinationPageId: String? = null,
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.Filter")
    data class Filter(
        val placeholder: String = "O que você procura hoje?",
        override val customLabel: String? = null,
        override val isFilterable: Boolean = false,
        override val destinationUrl: String? = null,
        override val destinationPageId: String? = null,
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.CategoryComponent")
    data class CategoryComponent(
        val categoryName: String,
        val title: String? = null,
        val layout: String = "GRID",
        override val customLabel: String? = null,
        override val isFilterable: Boolean = false,
        override val destinationUrl: String? = null,
        override val destinationPageId: String? = null,
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.CategoryFilter")
    data class CategoryFilter(
        override val customLabel: String? = null,
        override val isFilterable: Boolean = false,
        override val destinationUrl: String? = null,
        override val destinationPageId: String? = null,
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.ProfileHeader")
    data class ProfileHeader(
        val imageUrl: String,
        val name: String,
        val bio: String = "",
        val imageSize: Int = 120,
        val isCircular: Boolean = true,
        @Transient
        override val customLabel: String? = null,
        @Transient
        override val isFilterable: Boolean = false,
        override val destinationUrl: String? = null,
        override val destinationPageId: String? = null,
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.SocialLinks")
    data class SocialLinks(
        val instagram: String? = null,
        val whatsapp: String? = null,
        val youtube: String? = null,
        val email: String? = null,
        @Transient
        override val customLabel: String? = null,
        @Transient
        override val isFilterable: Boolean = false,
        override val destinationUrl: String? = null,
        override val destinationPageId: String? = null,
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.ProductGrid")
    data class ProductGrid(
        val productIds: List<String>,
        val columns: Int = 2,
        val showPrice: Boolean = true,
        @Transient
        override val customLabel: String? = null,
        @Transient
        override val isFilterable: Boolean = false,
        override val destinationUrl: String? = null,
        override val destinationPageId: String? = null,
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.CartComponent")
    data class CartComponent(
        val title: String = "Carrinho",
        @Transient
        override val customLabel: String? = null,
        @Transient
        override val isFilterable: Boolean = false,
        override val destinationPageId: String? = null,
        override val destinationUrl: String? = null,
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.OrderTrackingComponent")
    data class OrderTrackingComponent(
        val title: String = "Acompanhar Pedido",
        @Transient
        override val customLabel: String? = null,
        @Transient
        override val isFilterable: Boolean = false,
        override val destinationPageId: String? = null,
        override val destinationUrl: String? = null,
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.FeaturedProductsComponent")
    data class FeaturedProductsComponent(
        val productIds: List<String>,
        val title: String = "Destaques",
        @Transient
        override val customLabel: String? = null,
        @Transient
        override val isFilterable: Boolean = false,
        override val destinationPageId: String? = null,
        override val destinationUrl: String? = null,
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.ServiceList")
    data class ServiceList(
        val services: List<BookingService> = emptyList(),
        val title: String = "Nossos Serviços",
        @Transient
        override val customLabel: String? = null,
        @Transient
        override val isFilterable: Boolean = true,
        override val destinationPageId: String? = null,
        override val destinationUrl: String? = null,
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.Row")
    data class Row(
        val components: List<PageComponent>,
        @Transient
        override val customLabel: String? = null,
        @Transient
        override val isFilterable: Boolean = false,
        override val destinationPageId: String? = null,
        override val destinationUrl: String? = null,
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.Hero")
    data class Hero(
        val title: String,
        val subtitle: String? = null,
        val imageUrl: String,
        val buttonText: String? = null,
        val buttonUrl: String? = null,
        val height: Int = 400,
        val textAlign: String = "CENTER",
        @Transient
        override val customLabel: String? = null,
        @Transient
        override val isFilterable: Boolean = false,
        override val destinationPageId: String? = null,
        override val destinationUrl: String? = null,
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.Benefits")
    data class Benefits(
        val items: List<BenefitItem>,
        val title: String? = null,
        @Transient
        override val customLabel: String? = null,
        @Transient
        override val isFilterable: Boolean = false,
        override val destinationPageId: String? = null,
        override val destinationUrl: String? = null,
    ) : PageComponent()

    @Serializable
    data class BenefitItem(
        val title: String,
        val description: String,
        val iconName: String // "Check", "Magic", "Inventory", etc.
    )

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.Testimonial")
    data class Testimonial(
        val quote: String,
        val author: String,
        val authorTitle: String? = null,
        val rating: Int = 5,
        @Transient
        override val customLabel: String? = null,
        @Transient
        override val isFilterable: Boolean = false,
        override val destinationPageId: String? = null,
        override val destinationUrl: String? = null,
    ) : PageComponent()

    companion object
}

@Serializable
data class Page(
    val id: String,
    val storeId: String,
    val title: String,
    val customDomain: String? = null,
    val whatsapp: String? = null,
    val components: List<PageComponent> = emptyList(),
    val theme: PageThemeConfig = PageThemeConfig.ROYAL,
    val customTheme: CustomThemeConfig? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val deletedAt: Long? = null
) {
    companion object {
        fun createFromTemplate(templateId: String, pageId: String, storeId: String, title: String): Page {
            return PageTemplateRegistry.createPageFromTemplate(templateId, pageId, storeId, title)
        }
    }
}
