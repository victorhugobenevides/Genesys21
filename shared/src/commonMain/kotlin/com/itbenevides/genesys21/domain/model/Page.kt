package com.itbenevides.genesys21.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.domain.model.PageThemeConfig

@Serializable
sealed class PageComponent {
    @Transient
    open val customLabel: String? = null
    @Transient
    open val isFilterable: Boolean = false
    
    @Transient
    open val destinationPageId: String? = null
    @Transient
    open val destinationUrl: String? = null

    @Serializable
    data class Text(
        val content: String,
        val style: String = "BODY", // TITLE, SUBTITLE, BODY, CAPTION
        val textAlign: String = "START", // START, CENTER, END
        val usePrimaryColor: Boolean = false,
        val isUppercase: Boolean = false,
        val fontWeight: String = "NORMAL",
        val fontSize: Int = 16,
        @Transient
        override val customLabel: String? = null,
        @Transient
        override val isFilterable: Boolean = false,
        @Transient
        override val destinationPageId: String? = null,
        @Transient
        override val destinationUrl: String? = null
    ) : PageComponent()

    @Serializable
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
        @Transient
        override val destinationPageId: String? = null,
        @Transient
        override val destinationUrl: String? = null
    ) : PageComponent()

    @Serializable
    data class Image(
        val url: String,
        val caption: String? = null,
        val aspectRatio: Float = 1f,
        val size: Int = 200,
        val isCircular: Boolean = false,
        val isFullWidth: Boolean = false,
        @Transient
        override val customLabel: String? = null,
        @Transient
        override val isFilterable: Boolean = false,
        @Transient
        override val destinationPageId: String? = null,
        @Transient
        override val destinationUrl: String? = null
    ) : PageComponent()

    @Serializable
    data class Button(
        val text: String,
        val url: String,
        val isPrimary: Boolean = true,
        @Transient
        override val customLabel: String? = null,
        @Transient
        override val isFilterable: Boolean = false,
        @Transient
        override val destinationPageId: String? = null,
        @Transient
        override val destinationUrl: String? = null
    ) : PageComponent()

    @Serializable
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
        @Transient
        override val destinationPageId: String? = null,
        @Transient
        override val destinationUrl: String? = null
    ) : PageComponent()

    @Serializable
    data class SocialLinks(
        val instagram: String? = null,
        val whatsapp: String? = null,
        val youtube: String? = null,
        val email: String? = null,
        @Transient
        override val customLabel: String? = null,
        @Transient
        override val isFilterable: Boolean = false,
        @Transient
        override val destinationPageId: String? = null,
        @Transient
        override val destinationUrl: String? = null
    ) : PageComponent()

    @Serializable
    data class ProductGrid(
        val productIds: List<String>,
        val columns: Int = 2,
        val showPrice: Boolean = true,
        @Transient
        override val customLabel: String? = null,
        @Transient
        override val isFilterable: Boolean = false,
        @Transient
        override val destinationPageId: String? = null,
        @Transient
        override val destinationUrl: String? = null
    ) : PageComponent()

    @Serializable
    data class CartComponent(
        val title: String = "Carrinho",
        @Transient
        override val customLabel: String? = null,
        @Transient
        override val isFilterable: Boolean = false,
        @Transient
        override val destinationPageId: String? = null,
        @Transient
        override val destinationUrl: String? = null
    ) : PageComponent()

    @Serializable
    data class OrderTrackingComponent(
        val title: String = "Acompanhar Pedido",
        @Transient
        override val customLabel: String? = null,
        @Transient
        override val isFilterable: Boolean = false,
        @Transient
        override val destinationPageId: String? = null,
        @Transient
        override val destinationUrl: String? = null
    ) : PageComponent()

    @Serializable
    data class CategoryComponent(
        val categoryName: String,
        val title: String? = null,
        val layout: String = "GRID", // GRID, HORIZONTAL
        @Transient
        override val customLabel: String? = null,
        @Transient
        override val isFilterable: Boolean = false,
        @Transient
        override val destinationPageId: String? = null,
        @Transient
        override val destinationUrl: String? = null
    ) : PageComponent()

    @Serializable
    data class CategoryFilter(
        @Transient
        override val customLabel: String? = null,
        @Transient
        override val isFilterable: Boolean = false,
        @Transient
        override val destinationPageId: String? = null,
        @Transient
        override val destinationUrl: String? = null
    ) : PageComponent()

    @Serializable
    data class Filter(
        val placeholder: String = "Buscar...",
        @Transient
        override val customLabel: String? = null,
        @Transient
        override val isFilterable: Boolean = false,
        @Transient
        override val destinationPageId: String? = null,
        @Transient
        override val destinationUrl: String? = null
    ) : PageComponent()

    @Serializable
    data class FeaturedProductsComponent(
        val productIds: List<String>,
        val title: String = "Destaques",
        @Transient
        override val customLabel: String? = null,
        @Transient
        override val isFilterable: Boolean = false,
        @Transient
        override val destinationPageId: String? = null,
        @Transient
        override val destinationUrl: String? = null
    ) : PageComponent()

    @Serializable
    data class ProductList(
        val products: List<Product> = emptyList(),
        val title: String = "Lista de Produtos",
        val isHorizontal: Boolean = false,
        @Transient
        override val customLabel: String? = null,
        @Transient
        override val isFilterable: Boolean = false,
        @Transient
        override val destinationPageId: String? = null,
        @Transient
        override val destinationUrl: String? = null
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
) {
    companion object {
        fun defaultTemplate(id: String, title: String): Page = createDefaultPageTemplate(id, title)
        fun profileTemplate(id: String, title: String): Page = com.itbenevides.genesys21.domain.model.profileTemplate(id, title)
        fun blogPostTemplate(id: String, title: String): Page = com.itbenevides.genesys21.domain.model.blogPostTemplate(id, title)
    }
}

fun createDefaultPageTemplate(id: String, title: String): Page {
    val oldSchoolCategories = listOf("Vintage Denim", "Retro Graphic Tees", "Old School Jackets", "90s Accessories", "Classic Sneakers")
    val demoProducts = (1..30).map { i ->
        val category = oldSchoolCategories[i % oldSchoolCategories.size]
        Product(
            id = "vint_$i",
            name = when(category) {
                "Vintage Denim" -> listOf("Calça Jeans 501", "Shorts Acid Wash", "Jaqueta Jeans Oversized").random() + " #$i"
                "Retro Graphic Tees" -> listOf("T-Shirt Flamingo", "Camiseta Arcade 80s", "Baby Look Neon").random() + " #$i"
                "Old School Jackets" -> listOf("Windbreaker Turquesa", "Bomber Varsity", "Corta Vento Color Block").random() + " #$i"
                "90s Accessories" -> listOf("Boné Snapback", "Óculos Tartaruga", "Pochete Retro").random() + " #$i"
                else -> "Tênis cano alto Vintage" + " #$i"
            },
            price = (49..399).random().toDouble() + 0.90,
            categoryName = category,
            stock = (5..50).random(),
            imageUrls = listOf("https://picsum.photos/seed/vint$i/500/500")
        )
    }

    return Page(
        id = id,
        title = title,
        whatsapp = "5500000000000",
        theme = PageThemeConfig.ROYAL,
        components = listOf(
            PageComponent.Text(content = title, style = "TITLE", textAlign = "CENTER"),
            PageComponent.Text(content = "Estilo Atemporal para Almas Vintage", style = "SUBTITLE", textAlign = "CENTER"),
            dividerTemplate(),
            PageComponent.Text(content = "Curadoria exclusiva de peças que contam histórias. Do denim clássico aos acessórios que marcaram gerações.", style = "BODY", textAlign = "CENTER"),
            dividerTemplate(),
            PageComponent.Text(content = "Explorar por Categoria", style = "TITLE", textAlign = "START"),
            PageComponent.CategoryComponent(categoryName = "Retro Graphic Tees", title = "Camisetas de Época", layout = "HORIZONTAL"),
            PageComponent.CategoryComponent(categoryName = "Vintage Denim", title = "O Melhor do Jeans", layout = "HORIZONTAL"),
            dividerTemplate(),
            PageComponent.Text(content = "Nossas Peças em Destaque", style = "TITLE", textAlign = "START"),
            PageComponent.ProductList(products = demoProducts.take(15), title = "Recém Chegados"),
            dividerTemplate(),
            PageComponent.Button(text = "Falar com Consultor no WhatsApp", url = "https://wa.me/5500000000000"),
            PageComponent.Text(content = "Entregamos em todo o Brasil com embalagens sustentáveis.", style = "CAPTION", textAlign = "CENTER")
        )
    )
}

fun profileTemplate(id: String, title: String): Page {
    return Page(
        id = id,
        title = title,
        theme = PageThemeConfig.CLEAN,
        components = listOf(
            PageComponent.ProfileHeader(
                imageUrl = "https://picsum.photos/seed/profile/400/400",
                name = title,
                bio = "Especialista em Curadoria Digital & Design"
            ),
            PageComponent.SocialLinks(
                instagram = "https://instagram.com",
                whatsapp = "https://wa.me/5500000000000"
            ),
            PageComponent.Button(text = "Meu Portfólio", url = "#"),
            PageComponent.Button(text = "Agendar Mentoria", url = "#"),
            PageComponent.Button(text = "LinkedIn", url = "#", isPrimary = false)
        )
    )
}

fun blogPostTemplate(id: String, title: String): Page {
    return Page(
        id = id,
        title = title,
        theme = PageThemeConfig.MODERN,
        components = listOf(
            PageComponent.Image(url = "https://picsum.photos/seed/blog/800/400", aspectRatio = 2f, isFullWidth = true),
            PageComponent.Header(title = title, textAlign = "START"),
            PageComponent.Text(content = "Publicado em 20 de Março, 2024", style = "CAPTION", textAlign = "START"),
            PageComponent.Text(
                content = "A tecnologia está mudando a forma como interagimos com o mundo. " +
                        "Neste artigo, exploramos as tendências para o futuro do desenvolvimento multiplatforma...",
                style = "BODY",
                textAlign = "START"
            ),
            PageComponent.Button(text = "Ler mais artigos", url = "#", isPrimary = false),
            PageComponent.Button(text = "💬 Comentar no WhatsApp", url = "https://wa.me/5500000000000")
        )
    )
}

private fun dividerTemplate() = PageComponent.Text(
    content = "⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯",
    textAlign = "CENTER",
    usePrimaryColor = true
)
