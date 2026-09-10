package com.itbenevides.genesys21.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class TemplateCategory {
    SALES,
    SERVICES,
    PERSONAL,
    EMPTY
}

@Serializable
data class PageTemplate(
    val id: String,
    val title: String,
    val description: String,
    val category: TemplateCategory,
    val thumbnailUrl: String? = null,
    val defaultTheme: PageThemeConfig = PageThemeConfig.ELEGANCE,
    val components: List<PageComponent> = emptyList(),
    val customTheme: CustomThemeConfig? = null
)

object PageTemplateRegistry {
    val templates = listOf(
        premiumStore,
        techLanding,
        legalProfessional,
        serviceBooking,
        beautySalon,
        personalHub,
        emptyTemplate
    )

    private val legalProfessional get() = PageTemplate(
        id = "legal_professional",
        title = "Consultoria & Direito",
        description = "Design sóbrio e autoritário para profissionais liberais. Tipografia clássica e cores executivas.",
        category = TemplateCategory.SERVICES,
        thumbnailUrl = "https://images.unsplash.com/photo-1505664194779-8beaceb93744?q=80&w=600",
        defaultTheme = PageThemeConfig.ELEGANCE,
        customTheme = CustomThemeConfig(
            primaryColor = "#1A1C1E",
            onPrimaryColor = "#FFFFFF",
            secondaryColor = "#D4AF37",
            cornerRadius = 4,
            typographySet = TypographySet.CLASSIC_SERIF
        ),
        components = listOf(
            PageComponent.Header(title = "Excelência Jurídica & Consultoria", fontSize = 32, fontWeight = "BOLD", textAlign = "CENTER"),
            PageComponent.Text(content = "Comprometimento com a justiça e a transparência em cada causa.", textAlign = "CENTER"),
            PageComponent.Benefits(
                title = "Áreas de Atuação",
                items = listOf(
                    PageComponent.BenefitItem("Direito Civil", "Assessoria completa em contratos e responsabilidade.", "Description"),
                    PageComponent.BenefitItem("Consultoria B2B", "Blindagem patrimonial e estratégia societária.", "BusinessCenter"),
                    PageComponent.BenefitItem("Compliance", "Adequação normativa e ética corporativa.", "Shield")
                )
            ),
            PageComponent.Divider(),
            PageComponent.Button(text = "Agendar Consulta Presencial", url = "#", isPrimary = true)
        )
    )

    private val techLanding get() = PageTemplate(
        id = "tech_landing",
        title = "Inovação SaaS",
        description = "Layout futurista com efeito Glassmorphism e bordas arredondadas. Perfeito para produtos digitais.",
        category = TemplateCategory.SALES,
        thumbnailUrl = "https://images.unsplash.com/photo-1550745165-9bc0b252726f?q=80&w=600",
        defaultTheme = PageThemeConfig.MIDNIGHT,
        customTheme = CustomThemeConfig(
            cornerRadius = 28,
            glassIntensity = 0.25f,
            typographySet = TypographySet.MODERN_SANS
        ),
        components = listOf(
            PageComponent.Hero(
                title = "O Futuro é Agora",
                subtitle = "Sua plataforma de dados escalável com segurança de ponta a ponta.",
                imageUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=1200",
                buttonText = "Começar Grátis",
                height = 500
            ),
            PageComponent.Grid(
                columns = 2,
                items = listOf(
                    PageComponent.GridItem(components = listOf(PageComponent.Header("API First"), PageComponent.Text("Integração total via Webhooks."))),
                    PageComponent.GridItem(components = listOf(PageComponent.Header("Cloud Native"), PageComponent.Text("Escalabilidade infinita na AWS.")))
                )
            ),
            PageComponent.Divider(usePadding = true),
            PageComponent.ProductList(customLabel = "Nossos Planos", isHorizontal = true)
        )
    )

    private val beautySalon get() = PageTemplate(
        id = "beauty_salon",
        title = "Salão & Estética Pro",
        description = "Design sofisticado com bordas orgânicas. Ideal para o mercado de luxo e bem-estar.",
        category = TemplateCategory.SERVICES,
        thumbnailUrl = "https://images.unsplash.com/photo-1560066984-138dadb4c035?q=80&w=600",
        defaultTheme = PageThemeConfig.ELEGANCE,
        customTheme = CustomThemeConfig(
            cornerRadius = 32,
            primaryColor = "#2B0116",
            backgroundColor = "#FDFCFB"
        ),
        components = listOf(
            PageComponent.Hero(
                title = "Realce sua Beleza Natural",
                subtitle = "Tratamentos personalizados e um ambiente focado no seu bem-estar.",
                imageUrl = "https://images.unsplash.com/photo-1560750588-73207b1ef5b8?q=80&w=1200",
                buttonText = "Agendar Horário",
                height = 500,
                textAlign = "CENTER"
            ),
            PageComponent.ServiceList(
                title = "Procedimentos",
                showPrice = true,
                services = listOf(
                    BookingService("serv_estetica_1", "store1", "Limpeza de Pele Profunda", "Remoção de impurezas.", 150.0, 60),
                    BookingService("serv_estetica_2", "store1", "Drenagem Linfática", "Redução de inchaço.", 120.0, 50)
                )
            ),
            PageComponent.Divider(),
            PageComponent.BusinessHours(
                title = "Horário",
                items = listOf(
                    PageComponent.BusinessDay("Segunda a Sexta", "09:00 - 19:00"),
                    PageComponent.BusinessDay("Sábado", "09:00 - 14:00")
                )
            ),
            PageComponent.SocialLinks(instagram = "#", whatsapp = "#")
        )
    )

    private val premiumStore get() = PageTemplate(
        id = "premium_store",
        title = "Vendas Premium",
        description = "A experiência de compra definitiva. Focado em produtos de alto valor com design imersivo.",
        category = TemplateCategory.SALES,
        thumbnailUrl = "https://images.unsplash.com/photo-1441986300917-64674bd600d8?q=80&w=600",
        defaultTheme = PageThemeConfig.ELEGANCE,
        customTheme = CustomThemeConfig(
            cornerRadius = 16,
            primaryColor = "#1A1C1E",
            secondaryColor = "#D4AF37"
        ),
        components = listOf(
            PageComponent.Hero(
                title = "Excelência em Cada Detalhe",
                subtitle = "Descubra a nova coleção que está redefinindo o luxo moderno.",
                imageUrl = "https://images.unsplash.com/photo-1490481651871-ab68de25d43d?q=80&w=1200",
                buttonText = "Ver Coleção",
                height = 550
            ),
            PageComponent.Benefits(
                title = "Por que somos diferentes",
                items = listOf(
                    PageComponent.BenefitItem("Qualidade Curada", "Materiais selecionados a dedo.", "Magic"),
                    PageComponent.BenefitItem("Entrega Global", "Enviamos para todo o mundo.", "Inventory"),
                    PageComponent.BenefitItem("Suporte VIP", "Atendimento personalizado 24h.", "Check")
                )
            ),
            PageComponent.CategoryFilter(),
            PageComponent.ProductList(customLabel = "Destaques", isHorizontal = true),
            PageComponent.Testimonial(
                quote = "A experiência de compra foi impecável. O produto superou todas as minhas expectativas.",
                author = "Clara Mendes",
                authorTitle = "Arquiteta"
            ),
            PageComponent.ProductList(customLabel = "Catálogo Completo")
        )
    )

    private val serviceBooking get() = PageTemplate(
        id = "service_booking",
        title = "Mentoria & Performance",
        description = "Ideal para especialistas que vendem tempo e conhecimento. Limpo, direto e focado em conversão.",
        category = TemplateCategory.SERVICES,
        thumbnailUrl = "https://images.unsplash.com/photo-1585747860715-2ba37e788b70?q=80&w=600",
        defaultTheme = PageThemeConfig.VIBRANT,
        components = listOf(
            PageComponent.Hero(
                title = "Evolua sua Carreira Hoje",
                subtitle = "Mentoria estratégica para profissionais que buscam o próximo nível.",
                imageUrl = "https://images.unsplash.com/photo-1552664730-d307ca884978?q=80&w=1200",
                buttonText = "Agendar Mentoria",
                height = 450
            ),
            PageComponent.Header(title = "Nossos Serviços", fontSize = 28, fontWeight = "EXTRA_BOLD", textAlign = "CENTER"),
            PageComponent.ServiceList(title = "Escolha seu plano"),
            PageComponent.SocialLinks(instagram = "#", whatsapp = "#", email = "contato@exemplo.com")
        )
    )

    private val personalHub get() = PageTemplate(
        id = "personal_hub",
        title = "Personal Hub",
        description = "Sua nova central de links. Elegante, pessoal e perfeita para redes sociais.",
        category = TemplateCategory.PERSONAL,
        thumbnailUrl = "https://images.unsplash.com/photo-1522202176988-66273c2fd55f?q=80&w=600",
        defaultTheme = PageThemeConfig.MONO,
        customTheme = CustomThemeConfig(cornerRadius = 50), // Ultra-rounded buttons
        components = listOf(
            PageComponent.ProfileHeader(
                imageUrl = "https://ui-avatars.com/api/?name=Genesys+User\u0026size=300\u0026background=000\u0026color=fff",
                name = "Seu Nome",
                bio = "Criador de Conteúdo | Engenheiro de Software",
            ),
            PageComponent.SocialLinks(instagram = "#", whatsapp = "#", youtube = "#"),
            PageComponent.Spacer(height = 24),
            PageComponent.Grid(
                columns = 1,
                items = listOf(
                    PageComponent.GridItem(components = listOf(PageComponent.Button(text = "🚀 Último Vídeo no YouTube", url = "#"))),
                    PageComponent.GridItem(components = listOf(PageComponent.Button(text = "📚 Baixar meu E-book Grátis", url = "#")))
                )
            ),
            PageComponent.ProductList(customLabel = "Minhas Indicações", isHorizontal = true)
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

        val updatedComponents = template.components.map { component ->
            when (component) {
                is PageComponent.ProductList -> component.copy(
                    products = component.products.map { it.copy(storeId = storeId) }
                )
                is PageComponent.ServiceList -> component.copy(
                    services = component.services.map { it.copy(storeId = storeId) }
                )
                is PageComponent.SingleProduct -> component.copy(
                    product = component.product.copy(storeId = storeId)
                )
                is PageComponent.SingleService -> component.copy(
                    service = component.service.copy(storeId = storeId)
                )
                else -> component
            }
        }

        return Page(
            id = pageId,
            storeId = storeId,
            title = customTitle ?: template.title,
            theme = template.defaultTheme,
            customTheme = template.customTheme,
            components = updatedComponents
        )
    }
}
