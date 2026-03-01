package com.itbenevides.genesys21.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.random.Random

@Serializable
data class Product(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val imageUrls: List<String> = emptyList(),
    val description: String = "",
    val categoryId: Int? = null,
    val categoryName: String? = null,
    val stock: Int = 0
) {
    val imageUrl: String get() = imageUrls.firstOrNull() ?: ""
}

@Serializable
data class StepItem(val title: String = "", val description: String = "")

@Serializable
enum class PageThemeConfig {
    ROYAL, OCEAN, FOREST, CANDY, SUNSET, 
    BERRY, MINIMAL, VINTAGE, NORDIC, COFFEE,
    SOFT_LAVENDER, SKY_BLUE, MINT_GREEN, PEACH, LEMON,
    DARK_MODE, MIDNIGHT, NEON, DEEP_SPACE, LUXURY_GOLD,
    RADARANI, MARKETING_RED,
    DEFAULT
}

enum class PageTemplateType {
    EMPTY, STORE, BIO, LANDING
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("component_class")
sealed class PageComponent {
    abstract val customLabel: String?
    abstract val isFilterable: Boolean
    abstract val destinationUrl: String?
    abstract val destinationPageId: String?
    abstract val backgroundColor: String?

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.Typography")
    data class Typography(
        val text: String = "",
        val style: String = "BODY", 
        val fontSize: Int = 16,
        val textAlign: String = "LEFT",
        val fontWeight: String = "NORMAL",
        val isUppercase: Boolean = false,
        val usePrimaryColor: Boolean = false,
        override val customLabel: String? = "Texto",
        override val isFilterable: Boolean = false,
        override val destinationUrl: String? = null,
        override val destinationPageId: String? = null,
        override val backgroundColor: String? = null
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.Header")
    data class Header(
        val title: String = "",
        val textAlign: String = "CENTER",
        val isUppercase: Boolean = false,
        val fontSize: Int = 24,
        override val customLabel: String? = "Cabeçalho",
        override val isFilterable: Boolean = false,
        override val destinationUrl: String? = null,
        override val destinationPageId: String? = null,
        override val backgroundColor: String? = null
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.Media")
    data class Media(
        val url: String = "",
        val title: String? = null,
        val description: String? = null,
        val layout: String = "FULL_WIDTH",
        val imageOnRight: Boolean = false,
        val size: Int = 300,
        val isRounded: Boolean = true,
        val hasBottomArc: Boolean = false,
        override val customLabel: String? = "Mídia",
        override val isFilterable: Boolean = false,
        override val destinationUrl: String? = null,
        override val destinationPageId: String? = null,
        override val backgroundColor: String? = null
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.Image")
    data class Image(
        val url: String = "",
        val size: Int = 200,
        val isFullWidth: Boolean = true,
        val isCircular: Boolean = false,
        override val customLabel: String? = "Imagem",
        override val isFilterable: Boolean = false,
        override val destinationUrl: String? = null,
        override val destinationPageId: String? = null,
        override val backgroundColor: String? = null
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.Highlight")
    data class Highlight(
        val text: String = "",
        @SerialName("highlight_type")
        val type: String = "BUTTON", 
        val url: String? = null,
        val textColor: String? = null,
        val usePrimaryColor: Boolean = false,
        override val customLabel: String? = "Destaque",
        override val isFilterable: Boolean = false,
        override val destinationUrl: String? = null,
        override val destinationPageId: String? = null,
        override val backgroundColor: String? = null
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.ProductList")
    data class ProductList(
        val products: List<Product> = emptyList(),
        val isHorizontal: Boolean = false,
        override val customLabel: String? = "Produtos",
        override val isFilterable: Boolean = true,
        override val destinationUrl: String? = null,
        override val destinationPageId: String? = null,
        override val backgroundColor: String? = null
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.CategoryFilter")
    data class CategoryFilter(
        override val customLabel: String? = "Categorias",
        override val isFilterable: Boolean = false,
        override val destinationUrl: String? = null,
        override val destinationPageId: String? = null,
        override val backgroundColor: String? = null
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.StepProcess")
    data class StepProcess(
        val steps: List<StepItem> = emptyList(),
        override val customLabel: String? = "Processo",
        override val isFilterable: Boolean = false,
        override val destinationUrl: String? = null,
        override val destinationPageId: String? = null,
        override val backgroundColor: String? = null
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.Testimonial")
    data class Testimonial(
        val quote: String = "",
        val author: String = "",
        override val customLabel: String? = "Depoimento",
        override val isFilterable: Boolean = false,
        override val destinationUrl: String? = null,
        override val destinationPageId: String? = null,
        override val backgroundColor: String? = null
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.SocialLinks")
    data class SocialLinks(
        val instagram: String? = null,
        val whatsapp: String? = null,
        val email: String? = null,
        override val customLabel: String? = "Social",
        override val isFilterable: Boolean = false,
        override val destinationUrl: String? = null,
        override val destinationPageId: String? = null,
        override val backgroundColor: String? = null
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.ProfileHeader")
    data class ProfileHeader(
        val imageUrl: String = "",
        val name: String = "",
        val bio: String = "",
        val imageSize: Int = 120,
        val isCircular: Boolean = true,
        override val customLabel: String? = "Perfil",
        override val isFilterable: Boolean = false,
        override val destinationUrl: String? = null,
        override val destinationPageId: String? = null,
        override val backgroundColor: String? = null
    ) : PageComponent()

    // Mapeamento para o componente de Busca (Search)
    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.Search")
    data class Search(
        val placeholder: String = "O que você procura?",
        override val customLabel: String? = "Busca",
        override val isFilterable: Boolean = false,
        override val destinationUrl: String? = null,
        override val destinationPageId: String? = null,
        override val backgroundColor: String? = null
    ) : PageComponent()

    @Serializable
    @SerialName("unknown")
    data class Unknown(
        override val customLabel: String? = "Desconhecido",
        override val isFilterable: Boolean = false,
        override val destinationUrl: String? = null,
        override val destinationPageId: String? = null,
        override val backgroundColor: String? = null
    ) : PageComponent()
}

@Serializable
data class Page(
    val id: String = "",
    val title: String = "",
    val ownerId: String? = null,
    val customDomain: String? = null,
    val whatsapp: String? = null,
    val components: List<PageComponent> = emptyList(),
    val theme: PageThemeConfig = PageThemeConfig.ROYAL
)
