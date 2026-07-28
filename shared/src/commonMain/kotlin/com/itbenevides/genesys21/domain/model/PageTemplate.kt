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
        showcasePortfolio,
        proDesign,
        blogPost,
        emptyTemplate
    )

    private val professionalVitrine get() = PageTemplate(
        id = "professional_vitrine",
        title = "Vitrine de Luxo",
        description = "Experiência de e-commerce premium com banner de impacto, benefícios em grade e vitrine otimizada.",
        category = TemplateCategory.SALES,
        thumbnailUrl = "https://images.unsplash.com/photo-1441986300917-64674bd600d8?q=80&w=600",
        defaultTheme = PageThemeConfig.OCEAN,
        components = listOf(
            PageComponent.Hero(
                title = "Estilo & Performance",
                subtitle = "A coleção 2025 chegou para redefinir seus padrões de elegância.",
                imageUrl = "https://images.unsplash.com/photo-1441984908746-d47b8b24eabc?q=80&w=1200",
                buttonText = "Explorar Coleção",
                height = 550
            ),
            PageComponent.Grid(
                columns = 3,
                title = "Por que escolher nossa marca?",
                items = listOf(
                    PageComponent.GridItem(components = listOf(
                        PageComponent.Header(title = "Envio Expresso", fontSize = 18, textAlign = "CENTER"),
                        PageComponent.Text(content = "Entrega em até 48h para capitais.", textAlign = "CENTER", fontSize = 14)
                    )),
                    PageComponent.GridItem(components = listOf(
                        PageComponent.Header(title = "Segurança Total", fontSize = 18, textAlign = "CENTER"),
                        PageComponent.Text(content = "Pagamento 100% criptografado.", textAlign = "CENTER", fontSize = 14)
                    )),
                    PageComponent.GridItem(components = listOf(
                        PageComponent.Header(title = "Suporte 24/7", fontSize = 18, textAlign = "CENTER"),
                        PageComponent.Text(content = "Time especializado à sua disposição.", textAlign = "CENTER", fontSize = 14)
                    ))
                )
            ),
            PageComponent.Filter(placeholder = "Busque por nome ou categoria..."),
            PageComponent.CategoryFilter(),
            PageComponent.ProductList(customLabel = "🔥 Mais Vendidos", isHorizontal = true),
            PageComponent.Testimonial(
                quote = "Os produtos são de uma qualidade absurda. O processo de compra foi extremamente simples e fluido.",
                author = "Isabela Martins",
                authorTitle = "Fashion Blogger"
            ),
            PageComponent.ProductList(customLabel = "Nossa Vitrine Completa")
        )
    )

    private val bioProfile get() = PageTemplate(
        id = "bio_profile",
        title = "Bio Digital PRO",
        description = "Transforme seus seguidores em clientes com um hub de links moderno e organizado em grade.",
        category = TemplateCategory.PERSONAL,
        thumbnailUrl = "https://images.unsplash.com/photo-1522202176988-66273c2fd55f?q=80&w=600",
        defaultTheme = PageThemeConfig.RADARANI,
        components = listOf(
            PageComponent.ProfileHeader(
                imageUrl = "https://ui-avatars.com/api/?name=Genesys+User&size=200&background=random&color=fff",
                name = "Seu Nome / Marca",
                bio = "Estrategista Digital | Especialista em Conversão. Criando experiências que conectam.",
            ),
            PageComponent.SocialLinks(
                instagram = "#", whatsapp = "#", youtube = "#", email = "contato@exemplo.com",
            ),
            PageComponent.Header(title = "Meus Canais", fontSize = 20, textAlign = "CENTER"),
            PageComponent.Grid(
                columns = 2,
                items = listOf(
                    PageComponent.GridItem(components = listOf(PageComponent.Button(text = "🚀 Mentoria", url = "#"))),
                    PageComponent.GridItem(components = listOf(PageComponent.Button(text = "📚 E-books", url = "#"))),
                    PageComponent.GridItem(components = listOf(PageComponent.Button(text = "🎙️ Podcast", url = "#"))),
                    PageComponent.GridItem(components = listOf(PageComponent.Button(text = "🛍️ Minha Loja", url = "#")))
                )
            ),
            PageComponent.Testimonial(
                quote = "Acompanho o trabalho há anos e a entrega é sempre acima da média.",
                author = "Lucas Silva",
                rating = 5
            )
        )
    )

    private val barberShop get() = PageTemplate(
        id = "barber_shop",
        title = "Classic Barber & Grooming",
        description = "O mestre dos agendamentos. Focado em serviços, horários e experiência do cliente.",
        category = TemplateCategory.SERVICES,
        thumbnailUrl = "https://images.unsplash.com/photo-1503951914875-452162b0f3f1?q=80&w=600",
        defaultTheme = PageThemeConfig.ROYAL,
        components = listOf(
            PageComponent.Hero(
                title = "Muito Mais que um Corte",
                subtitle = "Tradição e modernidade para o homem de bom gosto.",
                imageUrl = "https://images.unsplash.com/photo-1585747860715-2ba37e788b70?q=80&w=1200",
                buttonText = "Garantir meu Horário",
                height = 500
            ),
            PageComponent.Header(title = "Nossos Serviços", fontSize = 26, fontWeight = "EXTRA_BOLD", textAlign = "CENTER"),
            PageComponent.ServiceList(title = "Selecione o tratamento", customLabel = "Menu de Serviços"),
            PageComponent.Grid(
                columns = 2,
                title = "Experiência Premium",
                items = listOf(
                    PageComponent.GridItem(components = listOf(
                        PageComponent.Image(url = "https://images.unsplash.com/photo-1599351431202-1e0f0137899a?q=80&w=400", isRounded = true),
                        PageComponent.Text(content = "Ambiente Climatizado", textAlign = "CENTER", fontWeight = "BOLD")
                    )),
                    PageComponent.GridItem(components = listOf(
                        PageComponent.Image(url = "https://images.unsplash.com/photo-1532713109658-f9ca9f07f282?q=80&w=400", isRounded = true),
                        PageComponent.Text(content = "Open Bar Cortesia", textAlign = "CENTER", fontWeight = "BOLD")
                    ))
                )
            ),
            PageComponent.Testimonial(
                quote = "Melhor barbearia da cidade. Atendimento nota mil!",
                author = "Felipe Amaral",
                rating = 5
            ),
            PageComponent.Header(title = "Linha de Cuidados", fontSize = 24),
            PageComponent.ProductList(customLabel = "Produtos de Uso Profissional", isHorizontal = true),
            PageComponent.SocialLinks(instagram = "#", whatsapp = "#")
        )
    )

    private val showcasePortfolio get() = PageTemplate(
        id = "showcase_portfolio",
        title = "Portfólio de Impacto",
        description = "Perfeito para agências e criativos. Exiba seus projetos em grades elegantes e capture leads.",
        category = TemplateCategory.CONTENT,
        thumbnailUrl = "https://images.unsplash.com/photo-1460925895917-afdab827c52f?q=80&w=600",
        defaultTheme = PageThemeConfig.MODERN,
        components = listOf(
            PageComponent.Header(title = "Criatividade Sem Limites", fontSize = 36, fontWeight = "EXTRA_BOLD"),
            PageComponent.Text(content = "Ajudamos marcas a se destacarem no mundo digital através de design e tecnologia de ponta.", fontSize = 18),
            PageComponent.Grid(
                columns = 2,
                title = "Nossos Cases",
                items = listOf(
                    PageComponent.GridItem(components = listOf(
                        PageComponent.Image(url = "https://images.unsplash.com/photo-1551288049-bbbda5366392?q=80&w=600", isRounded = true),
                        PageComponent.Header(title = "Projeto Alpha", fontSize = 20),
                        PageComponent.Text(content = "Branding & Web Design")
                    )),
                    PageComponent.GridItem(components = listOf(
                        PageComponent.Image(url = "https://images.unsplash.com/photo-1460925895917-afdab827c52f?q=80&w=600", isRounded = true),
                        PageComponent.Header(title = "Projeto Beta", fontSize = 20),
                        PageComponent.Text(content = "Campanha de Performance")
                    ))
                )
            ),
            PageComponent.Benefits(
                title = "Nossa Expertise",
                items = listOf(
                    PageComponent.BenefitItem("Design UI/UX", "Interfaces centradas no usuário.", "Magic"),
                    PageComponent.BenefitItem("Performance", "Sites velozes e otimizados.", "Check")
                )
            ),
            PageComponent.Button(text = "Solicitar Orçamento", url = "#", isPrimary = true)
        )
    )

    private val proDesign get() = PageTemplate(
        id = "pro_design",
        title = "Dark Mode Premium",
        description = "Focado em produtos de alto valor com interface escura, fontes luxuosas e glassmorfismo.",
        category = TemplateCategory.SALES,
        thumbnailUrl = "https://images.unsplash.com/photo-1523275335684-37898b6baf30?q=80&w=600",
        defaultTheme = PageThemeConfig.MODERN,
        customTheme = CustomThemeConfig(
            primaryColor = "#BB86FC",
            backgroundColor = "#121212",
            cornerRadius = 16,
            glassIntensity = 0.5f,
            typographySet = TypographySet.MODERN_SANS,
        ),
        components = listOf(
            PageComponent.Hero(
                title = "O Futuro é Agora",
                subtitle = "Tecnologia e design em perfeita harmonia.",
                imageUrl = "https://images.unsplash.com/photo-1523275335684-37898b6baf30?q=80&w=1200",
                buttonText = "Ver Detalhes",
                height = 600
            ),
            PageComponent.Header(title = "Exclusive Tech", fontSize = 32, usePrimaryColor = true, textAlign = "CENTER"),
            PageComponent.ProductList(isHorizontal = true),
            PageComponent.SocialLinks(instagram = "#")
        )
    )

    private val blogPost get() = PageTemplate(
        id = "blog_post",
        title = "Storytelling Minimal",
        description = "Foco total na leitura. Tipografia equilibrada e layout limpo para conteúdos longos.",
        category = TemplateCategory.CONTENT,
        thumbnailUrl = "https://images.unsplash.com/photo-1499750310107-5fef28a66643?q=80&w=600",
        defaultTheme = PageThemeConfig.MINIMAL,
        components = listOf(
            PageComponent.Header(title = "Como escalar seu negócio em 2025", textAlign = "LEFT", fontSize = 34, fontWeight = "EXTRA_BOLD"),
            PageComponent.ProfileHeader(
                imageUrl = "https://ui-avatars.com/api/?name=Author&size=100&background=000&color=fff",
                name = "Victor Hugo",
                bio = "Escrito em 10 de Junho • 8 min de leitura",
                imageSize = 44
            ),
            PageComponent.Image(url = "https://images.unsplash.com/photo-1499750310107-5fef28a66643?q=80&w=1200", isFullWidth = true, isRounded = true),
            PageComponent.Text(content = "O segredo para o sucesso no novo cenário digital não é apenas tecnologia, mas como você conecta as pessoas ao seu propósito...", fontSize = 19),
            PageComponent.Header(title = "A Era da Inteligência Artificial", fontSize = 26),
            PageComponent.Text(content = "Estamos vivendo a maior transformação tecnológica da história recente. Adaptar-se não é mais uma opção, é sobrevivência.", fontSize = 19),
            PageComponent.SocialLinks(instagram = "#", whatsapp = "#"),
            PageComponent.Button(text = "Entrar na Comunidade", url = "#")
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
