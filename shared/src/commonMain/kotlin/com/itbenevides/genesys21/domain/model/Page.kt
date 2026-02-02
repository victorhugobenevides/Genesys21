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
    val categoryId: Int? = null,
    val categoryName: String? = null,
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
    RADARANI, // ADICIONADO: Novo tema baseado na imagem
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
        val textAlign: String = "LEFT",
        val fontWeight: String = "NORMAL",
        val isUppercase: Boolean = false,
        val usePrimaryColor: Boolean = false,
        override val customLabel: String? = null,
        override val isFilterable: Boolean = false
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.Header")
    data class Header(
        val title: String,
        val fontSize: Int = 28,
        val textAlign: String = "LEFT",
        val fontWeight: String = "EXTRA_BOLD",
        val isUppercase: Boolean = false,
        val usePrimaryColor: Boolean = true,
        override val customLabel: String? = null,
        override val isFilterable: Boolean = false
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.Image")
    data class Image(
        val url: String, 
        val string: String = "",
        val size: Int = 200,
        val destinationPageId: String? = null,
        val isFullWidth: Boolean = true,
        val isRounded: Boolean = true,
        val isCircular: Boolean = false,
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
        val placeholder: String = "O que você procura hoje?",
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
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.ProfileHeader")
    data class ProfileHeader(
        val imageUrl: String = "",
        val name: String = "",
        val bio: String = "",
        val imageSize: Int = 120,
        val isCircular: Boolean = true,
        override val customLabel: String? = "Perfil",
        override val isFilterable: Boolean = false
    ) : PageComponent()

    @Serializable
    @SerialName("com.itbenevides.genesys21.domain.model.PageComponent.SocialLinks")
    data class SocialLinks(
        val instagram: String? = null,
        val whatsapp: String? = null,
        val youtube: String? = null,
        val email: String? = null,
        val website: String? = null,
        override val customLabel: String? = "Redes Sociais",
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
) {
    companion object {
        fun defaultTemplate(id: String, title: String): Page {
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
                title = if (title.isBlank()) "Sua Vitrine" else title,
                theme = PageThemeConfig.OCEAN,
                components = listOf(
                    PageComponent.Header(
                        title = if (title.isBlank()) "Bem-vindo" else title, 
                        textAlign = "CENTER",
                        fontSize = 36
                    ),
                    PageComponent.Image(
                        url = "https://images.unsplash.com/photo-1483985988355-763728e1935b?q=80&w=1200",
                        isFullWidth = true
                    ),
                    PageComponent.Filter(),
                    PageComponent.CategoryFilter(),
                    PageComponent.ProductList(
                        products = demoProducts.take(8), 
                        isHorizontal = true, 
                        customLabel = "Destaques"
                    ),
                    PageComponent.ProductList(
                        products = demoProducts.drop(8), 
                        isHorizontal = false, 
                        customLabel = "Todos os Produtos"
                    )
                )
            )
        }

        fun profileTemplate(id: String, title: String): Page {
            return Page(
                id = id,
                title = if (title.isBlank()) "Meu Perfil" else title,
                theme = PageThemeConfig.RADARANI, // ATUALIZADO: Inicia com o tema Radarani
                components = listOf(
                    PageComponent.ProfileHeader(
                        imageUrl = "https://picsum.photos/seed/profile/300/300",
                        name = if (title.isBlank()) "Seu Nome Aqui" else title,
                        bio = "Desenvolvedor & Criador de Conteúdo. Bem-vindo aos meus links oficiais!"
                    ),
                    PageComponent.SocialLinks(
                        instagram = "https://instagram.com",
                        whatsapp = "https://wa.me/5500000000000",
                        youtube = "https://youtube.com",
                        email = "seuemail@exemplo.com"
                    ),
                    PageComponent.Header(title = "Conteúdo Exclusivo", fontSize = 22, textAlign = "CENTER"),
                    PageComponent.Button(text = "📚 Meu Curso Online", url = "https://exemplo.com/curso"),
                    PageComponent.Button(text = "🎙️ Podcast Semanal", url = "https://exemplo.com/podcast"),
                    PageComponent.Button(text = "🛍️ Minha Loja", url = "https://exemplo.com/loja"),
                    PageComponent.Header(title = "Últimas do Instagram", fontSize = 18, textAlign = "CENTER"),
                    PageComponent.ProductList(
                        products = (1..4).map { i ->
                            Product(
                                id = "post_$i",
                                name = "Post #$i",
                                price = 0.0,
                                imageUrls = listOf("https://picsum.photos/seed/insta$i/400/400")
                            )
                        },
                        isHorizontal = true,
                        customLabel = "Instagram Feed"
                    )
                )
            )
        }
    }
}
