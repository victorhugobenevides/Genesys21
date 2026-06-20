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

    companion object
}

@Serializable
data class Page(
    val id: String,
    val title: String,
    val ownerId: String? = null,
    val customDomain: String? = null,
    val whatsapp: String? = null,
    val components: List<PageComponent> = emptyList(),
    val theme: PageThemeConfig = PageThemeConfig.ROYAL,
) {
    companion object {
        fun defaultTemplate(
            id: String,
            title: String,
        ): Page {
            val oldSchoolCategories =
                listOf("Vintage Denim", "Retro Graphic Tees", "Old School Jackets", "90s Accessories", "Classic Sneakers")
            val demoProducts =
                (1..30).map { i ->
                    val category = oldSchoolCategories[i % oldSchoolCategories.size]
                    Product(
                        id = "vint_$i",
                        name =
                            when (category) {
                                "Vintage Denim" -> listOf("Calça Jeans 501", "Shorts Acid Wash", "Jaqueta Jeans Oversized").random() + " #$i"
                                "Retro Graphic Tees" -> listOf("T-Shirt Flamingo", "Camiseta Arcade 80s", "Baby Look Neon").random() + " #$i"
                                "Old School Jackets" -> listOf("Windbreaker Turquesa", "Bomber Varsity", "Corta Vento Color Block").random() + " #$i"
                                "90s Accessories" -> listOf("Boné Snapback", "Óculos Tartaruga", "Pochete Retro").random() + " #$i"
                                else -> "Tênis cano alto Vintage" + " #$i"
                            },
                        price = (49..399).random().toDouble() + 0.90,
                        categoryName = category,
                        stock = (5..50).random(),
                        imageUrls = listOf("https://picsum.photos/seed/vint$i/500/500"),
                    )
                }

            return Page(
                id = id,
                title = if (title.isBlank()) "Sua Vitrine" else title,
                theme = PageThemeConfig.OCEAN,
                components =
                    listOf(
                        PageComponent.Header(
                            title = if (title.isBlank()) "Bem-vindo" else title,
                            textAlign = "CENTER",
                            fontSize = 36,
                        ),
                        PageComponent.Image(
                            url = "https://images.unsplash.com/photo-1483985988355-763728e1935b?q=80&w=1200",
                            isFullWidth = true,
                        ),
                        PageComponent.Filter(),
                        PageComponent.CategoryFilter(),
                        PageComponent.ProductList(
                            products = demoProducts.take(8),
                            isHorizontal = true,
                            customLabel = "Destaques",
                        ),
                        PageComponent.ProductList(
                            products = demoProducts.drop(8),
                            isHorizontal = false,
                            customLabel = "Todos os Produtos",
                        ),
                    ),
            )
        }

        fun profileTemplate(
            id: String,
            title: String,
        ): Page {
            return Page(
                id = id,
                title = if (title.isBlank()) "Meu Perfil" else title,
                theme = PageThemeConfig.RADARANI,
                components =
                    listOf(
                        PageComponent.ProfileHeader(
                            imageUrl = "https://picsum.photos/seed/profile/300/300",
                            name = if (title.isBlank()) "Seu Nome Aqui" else title,
                            bio = "Desenvolvedor & Criador de Conteúdo. Bem-vindo aos meus links oficiais!",
                        ),
                        PageComponent.SocialLinks(
                            instagram = "https://instagram.com",
                            whatsapp = "https://wa.me/5500000000000",
                            youtube = "https://youtube.com",
                            email = "seuemail@exemplo.com",
                        ),
                        PageComponent.Header(title = "Conteúdo Exclusivo", fontSize = 22, textAlign = "CENTER"),
                        PageComponent.Button(text = "📚 Meu Curso Online", url = "https://exemplo.com/curso"),
                        PageComponent.Button(text = "🎙️ Podcast Semanal", url = "https://exemplo.com/podcast"),
                        PageComponent.Button(text = "🛍️ Minha Loja", url = "https://exemplo.com/loja"),
                        PageComponent.Header(title = "Últimas do Instagram", fontSize = 18, textAlign = "CENTER"),
                        PageComponent.ProductList(
                            products =
                                (1..4).map { i ->
                                    Product(
                                        id = "post_$i",
                                        name = "Post #$i",
                                        price = 0.0,
                                        imageUrls = listOf("https://picsum.photos/seed/insta$i/400/400"),
                                    )
                                },
                            isHorizontal = true,
                            customLabel = "Instagram Feed",
                        ),
                    ),
            )
        }

        fun blogPostTemplate(
            id: String,
            title: String,
        ): Page {
            return Page(
                id = id,
                title = if (title.isBlank()) "Meu Artigo" else title,
                theme = PageThemeConfig.MINIMAL,
                components =
                    listOf(
                        PageComponent.Header(
                            title = if (title.isBlank()) "Título do Post no Estilo Blog" else title,
                            textAlign = "LEFT",
                            fontSize = 32,
                        ),
                        PageComponent.ProfileHeader(
                            imageUrl = "https://picsum.photos/seed/author/150/150",
                            name = "Por Autor Nome",
                            bio = "Publicado em 30 de Janeiro, 2025 • 5 min de leitura",
                            imageSize = 40,
                            isCircular = true,
                        ),
                        PageComponent.Image(
                            url = "https://images.unsplash.com/photo-1499750310107-5fef28a66643?q=80&w=1200",
                            isFullWidth = true,
                            isRounded = true,
                        ),
                        PageComponent.Text(
                            content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
                            fontSize = 18,
                            fontWeight = "NORMAL",
                        ),
                        PageComponent.Header(title = "Subtítulo Importante", fontSize = 24),
                        PageComponent.Text(
                            content = "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
                            fontSize = 18,
                        ),
                        PageComponent.Image(
                            url = "https://images.unsplash.com/photo-1432821596592-e2c18b78144f?q=80&w=800",
                            isFullWidth = false,
                            size = 300,
                        ),
                        PageComponent.Text(
                            content = "Conclusão do pensamento do blog. Espero que este conteúdo tenha sido útil para você!",
                            fontSize = 18,
                            fontWeight = "BOLD",
                        ),
                        PageComponent.DividerTemplate(),
                        PageComponent.SocialLinks(
                            instagram = "https://instagram.com",
                            whatsapp = "https://wa.me/5500000000000",
                        ),
                        PageComponent.Button(text = "💬 Comentar no WhatsApp", url = "https://wa.me/5500000000000"),
                    ),
            )
        }

        private fun PageComponent.Companion.DividerTemplate() =
            PageComponent.Text(
                content = "⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯",
                textAlign = "CENTER",
                usePrimaryColor = true,
            )
    }
}
