package com.itbenevides.genesys21.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class TemplateCategory {
    SALES,
    SERVICES,
    PERSONAL,
    CONTENT,
    EMPTY
}

@Serializable
data class PageTemplate(
    val id: String,
    val title: String,
    val description: String,
    val category: TemplateCategory,
    val thumbnailUrl: String? = null,
    val defaultTheme: PageThemeConfig = PageThemeConfig.ROYAL,
    val components: List<PageComponent> = emptyList(),
    val customTheme: CustomThemeConfig? = null
)

object PageTemplateRegistry {
    val templates = listOf(
        professionalVitrine,
        bioProfile,
        barberShop,
        proDesign,
        blogPost,
        emptyTemplate
    )

    private val professionalVitrine get() = PageTemplate(
        id = "professional_vitrine",
        title = "Vitrine Profissional",
        description = "Focada em produtos e vendas rápidas com banner de impacto e busca.",
        category = TemplateCategory.SALES,
        thumbnailUrl = "https://images.unsplash.com/photo-1441986300917-64674bd600d8?q=80&w=400",
        defaultTheme = PageThemeConfig.OCEAN,
        components = listOf(
            PageComponent.Hero(
                title = "Nova Coleção 2025",
                subtitle = "Descubra as tendências que vão dominar o ano.",
                imageUrl = "https://images.unsplash.com/photo-1441986300917-64674bd600d8?q=80&w=1200",
                buttonText = "Ver Novidades",
                height = 500
            ),
            PageComponent.Benefits(
                items = listOf(
                    PageComponent.BenefitItem("Frete Grátis", "Para compras acima de R$ 200", "Inventory"),
                    PageComponent.BenefitItem("Pagamento Seguro", "Parcele em até 12x sem juros", "Payments"),
                    PageComponent.BenefitItem("Qualidade Garantida", "Troca grátis em até 30 dias", "Check")
                )
            ),
            PageComponent.Filter(),
            PageComponent.CategoryFilter(),
            PageComponent.ProductList(customLabel = "Destaques da Semana", isHorizontal = true),
            PageComponent.Testimonial(
                quote = "A melhor experiência de compra que já tive. O layout é lindo e o atendimento impecável!",
                author = "Ana Silva",
                authorTitle = "Cliente VIP"
            ),
            PageComponent.ProductList(customLabel = "Todos os Itens")
        )
    )

    private val bioProfile get() = PageTemplate(
        id = "bio_profile",
        title = "Link na Bio",
        description = "Minimalista, ideal para redes sociais e cartões de visita digitais.",
        category = TemplateCategory.PERSONAL,
        thumbnailUrl = "https://picsum.photos/seed/profile/400/400",
        defaultTheme = PageThemeConfig.RADARANI,
        components = listOf(
            PageComponent.ProfileHeader(
                imageUrl = "https://picsum.photos/seed/profile/300/300",
                name = "Seu Nome Aqui",
                bio = "Desenvolvedor & Criador de Conteúdo. Bem-vindo aos meus links oficiais!",
            ),
            PageComponent.SocialLinks(
                instagram = "https://instagram.com",
                whatsapp = "https://wa.me/5500000000000",
                email = "seuemail@exemplo.com",
            ),
            PageComponent.Header(title = "Conteúdo Exclusivo", fontSize = 22, textAlign = "CENTER"),
            PageComponent.Button(text = "📚 Meu Curso Online", url = "https://exemplo.com/curso"),
            PageComponent.Button(text = "🎙️ Podcast Semanal", url = "https://exemplo.com/podcast"),
            PageComponent.Button(text = "🛍️ Minha Loja", url = "https://exemplo.com/loja")
        )
    )

    private val barberShop get() = PageTemplate(
        id = "barber_shop",
        title = "Barbearia & Serviços",
        description = "Layout focado em agendamentos, lista de serviços e produtos de cuidado.",
        category = TemplateCategory.SERVICES,
        thumbnailUrl = "https://images.unsplash.com/photo-1585747860715-2ba37e788b70?q=80&w=400",
        defaultTheme = PageThemeConfig.ROYAL,
        components = listOf(
            PageComponent.Hero(
                title = "O Estilo que Você Merece",
                subtitle = "Barba, Cabelo e Bigode com excelência desde 1990.",
                imageUrl = "https://images.unsplash.com/photo-1503951914875-452162b0f3f1?q=80&w=1200",
                buttonText = "Agendar Agora",
                height = 450
            ),
            PageComponent.Benefits(
                items = listOf(
                    PageComponent.BenefitItem("Profissionais Tops", "Especialistas em cortes clássicos e modernos", "Magic"),
                    PageComponent.BenefitItem("Conforto Total", "Cerveja gelada e ambiente climatizado", "Check")
                )
            ),
            PageComponent.Header(title = "Nossos Serviços", fontSize = 24),
            PageComponent.ServiceList(title = "Escolha o seu trato", customLabel = "Serviços de Barbearia"),
            PageComponent.Testimonial(
                quote = "Ambiente nota 10 e os barbeiros são realmente feras. Recomendo de olhos fechados!",
                author = "Marcos Oliveira"
            ),
            PageComponent.Header(title = "Produtos Premium", fontSize = 24),
            PageComponent.ProductList(customLabel = "Leve a Barbearia para Casa", isHorizontal = true),
            PageComponent.SocialLinks(instagram = "https://instagram.com", whatsapp = "https://wa.me/5500000000000")
        )
    )

    private val proDesign get() = PageTemplate(
        id = "pro_design",
        title = "Design PRO 💎",
        description = "Alta performance visual com glassmorfismo e cores modernas.",
        category = TemplateCategory.SALES,
        thumbnailUrl = "https://picsum.photos/seed/pro/400/400",
        defaultTheme = PageThemeConfig.MODERN,
        customTheme = CustomThemeConfig(
            primaryColor = "#FF5722",
            backgroundColor = "#F5F5F5",
            cornerRadius = 12,
            glassIntensity = 0.3f,
            typographySet = TypographySet.MODERN_SANS,
        ),
        components = listOf(
            PageComponent.ProfileHeader(
                imageUrl = "https://picsum.photos/seed/pro/400/400",
                name = "Marca Premium",
                bio = "Elegância e Performance em cada detalhe.",
                isCircular = false,
            ),
            PageComponent.Header(title = "Produtos Selecionados", fontSize = 28),
            PageComponent.ProductList(isHorizontal = true),
            PageComponent.SocialLinks(instagram = "https://instagram.com")
        )
    )

    private val blogPost get() = PageTemplate(
        id = "blog_post",
        title = "Artigo de Blog",
        description = "Ideal para publicações longas, guias e storytelling.",
        category = TemplateCategory.CONTENT,
        thumbnailUrl = "https://images.unsplash.com/photo-1499750310107-5fef28a66643?q=80&w=400",
        defaultTheme = PageThemeConfig.MINIMAL,
        components = listOf(
            PageComponent.Header(title = "Título do Post", textAlign = "LEFT", fontSize = 32),
            PageComponent.ProfileHeader(
                imageUrl = "https://picsum.photos/seed/author/150/150",
                name = "Autor do Artigo",
                bio = "Publicado recentemente • 5 min de leitura",
                imageSize = 40
            ),
            PageComponent.Image(url = "https://images.unsplash.com/photo-1499750310107-5fef28a66643?q=80&w=1200", isFullWidth = true, isRounded = true),
            PageComponent.Text(content = "Seu conteúdo começa aqui...", fontSize = 18),
            PageComponent.Header(title = "Subtítulo", fontSize = 24),
            PageComponent.Text(content = "Mais detalhes sobre o seu pensamento.", fontSize = 18),
            PageComponent.SocialLinks(instagram = "https://instagram.com", whatsapp = "https://wa.me/5500000000000"),
            PageComponent.Button(text = "💬 Comentar no WhatsApp", url = "https://wa.me/5500000000000")
        )
    )

    private val emptyTemplate get() = PageTemplate(
        id = "empty",
        title = "Página em Branco",
        description = "Comece do zero e monte sua página componente por componente.",
        category = TemplateCategory.EMPTY,
        thumbnailUrl = null,
        components = emptyList()
    )

    fun createPageFromTemplate(templateId: String, pageId: String, storeId: String, customTitle: String? = null): Page {
        val template = templates.find { it.id == templateId } ?: emptyTemplate
        return Page(
            id = pageId,
            storeId = storeId,
            title = customTitle ?: template.title,
            theme = template.defaultTheme,
            customTheme = template.customTheme,
            components = template.components
        )
    }
}
