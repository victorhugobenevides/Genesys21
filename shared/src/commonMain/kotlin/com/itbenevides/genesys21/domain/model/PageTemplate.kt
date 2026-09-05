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
        serviceBooking,
        beautySalon,
        personalHub,
        emptyTemplate
    )

    private val beautySalon get() = PageTemplate(
        id = "beauty_salon",
        title = "Salão & Estética",
        description = "Design sofisticado para profissionais de beleza. Galeria de espaço, procedimentos e agendamento online.",
        category = TemplateCategory.SERVICES,
        thumbnailUrl = "https://images.unsplash.com/photo-1560066984-138dadb4c035?q=80&w=600",
        defaultTheme = PageThemeConfig.ELEGANCE,
        components = listOf(
            PageComponent.Hero(
                title = "Realce sua Beleza Natural",
                subtitle = "Tratamentos personalizados e um ambiente focado no seu bem-estar.",
                imageUrl = "https://images.unsplash.com/photo-1560750588-73207b1ef5b8?q=80&w=1200",
                buttonText = "Agendar Horário",
                height = 500,
                textAlign = "CENTER"
            ),
            PageComponent.Benefits(
                title = "Por que escolher o Espaço Aurora?",
                items = listOf(
                    PageComponent.BenefitItem("Biossegurança", "Materiais 100% esterilizados e descartáveis.", "Check"),
                    PageComponent.BenefitItem("Produtos Premium", "Trabalhamos apenas com as melhores marcas mundiais.", "Magic"),
                    PageComponent.BenefitItem("Especialistas", "Equipe certificada em tratamentos avançados.", "Magic")
                )
            ),
            PageComponent.Divider(),
            PageComponent.Header(title = "Nossos Procedimentos", fontSize = 28, fontWeight = "EXTRA_BOLD", textAlign = "CENTER", usePrimaryColor = true),
            PageComponent.ServiceList(
                title = "Serviços em Destaque",
                showPrice = true,
                services = listOf(
                    BookingService("serv_estetica_1", "store1", "Limpeza de Pele Profunda", "Remoção de impurezas e hidratação intensa.", 150.0, 60),
                    BookingService("serv_estetica_2", "store1", "Drenagem Linfática", "Redução de inchaço e melhora da circulação.", 120.0, 50),
                    BookingService("serv_estetica_3", "store1", "Peeling Químico", "Renovação celular e rejuvenescimento.", 250.0, 45)
                )
            ),
            PageComponent.Divider(),
            PageComponent.Header(title = "Social Proof", fontSize = 24, fontWeight = "BOLD", textAlign = "CENTER"),
            PageComponent.Testimonial(
                quote = "O melhor atendimento que já recebi. A pele ficou maravilhosa logo na primeira sessão!",
                author = "Mariana Silva",
                authorTitle = "Advogada",
                rating = 5
            ),
            PageComponent.Divider(),
            PageComponent.Header(title = "Conheça nosso Espaço", fontSize = 22, fontWeight = "BOLD", textAlign = "CENTER"),
            PageComponent.Grid(
                columns = 2,
                items = listOf(
                    PageComponent.GridItem(components = listOf(PageComponent.Image(url = "https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?q=80&w=600", isRounded = true, size = 300))),
                    PageComponent.GridItem(components = listOf(PageComponent.Image(url = "https://images.unsplash.com/photo-1512496015851-a90fb38ba796?q=80&w=600", isRounded = true, size = 300))),
                    PageComponent.GridItem(components = listOf(PageComponent.Image(url = "https://images.unsplash.com/photo-1600334129128-685c5582fd35?q=80&w=600", isRounded = true, size = 300))),
                    PageComponent.GridItem(components = listOf(PageComponent.Image(url = "https://images.unsplash.com/photo-1596178060671-7a80dc8059ea?q=80&w=600", isRounded = true, size = 300)))
                )
            ),
            PageComponent.Divider(),
            PageComponent.BusinessHours(
                title = "Horário de Atendimento",
                items = listOf(
                    PageComponent.BusinessDay("Segunda a Sexta", "09:00 - 19:00"),
                    PageComponent.BusinessDay("Sábado", "09:00 - 14:00"),
                    PageComponent.BusinessDay("Domingo", "", isClosed = true)
                )
            ),
            PageComponent.Spacer(height = 24),
            PageComponent.SocialLinks(instagram = "#", whatsapp = "https://wa.me/5511999999999", email = "contato@espacoaurora.com"),
            PageComponent.Button(text = "Falar com Atendente no WhatsApp 📱", url = "https://wa.me/5511999999999", isPrimary = false)
        )
    )

    private val premiumStore get() = PageTemplate(
        id = "premium_store",
        title = "Vendas Premium",
        description = "A experiência de compra definitiva. Focado em produtos de alto valor com design imersivo.",
        category = TemplateCategory.SALES,
        thumbnailUrl = "https://images.unsplash.com/photo-1441986300917-64674bd600d8?q=80&w=600",
        defaultTheme = PageThemeConfig.ELEGANCE,
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
                    PageComponent.BenefitItem("Entrega Global", "Enviamos para todo o mundo com segurança.", "Inventory"),
                    PageComponent.BenefitItem("Suporte VIP", "Atendimento personalizado 24h.", "Check")
                )
            ),
            PageComponent.CategoryFilter(),
            PageComponent.ProductList(customLabel = "Destaques da Temporada", isHorizontal = true),
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
        title = "Agendamento Profissional",
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
            PageComponent.ServiceList(title = "Escolha seu plano", customLabel = "Opções de Mentoria"),
            PageComponent.Grid(
                columns = 2,
                title = "O que você vai aprender",
                items = listOf(
                    PageComponent.GridItem(components = listOf(
                        PageComponent.Image(url = "https://images.unsplash.com/photo-1551288049-bbbda5366392?q=80&w=400", isRounded = true),
                        PageComponent.Text(content = "Gestão de Dados", textAlign = "CENTER", fontWeight = "BOLD")
                    )),
                    PageComponent.GridItem(components = listOf(
                        PageComponent.Image(url = "https://images.unsplash.com/photo-1460925895917-afdab827c52f?q=80&w=400", isRounded = true),
                        PageComponent.Text(content = "Performance Web", textAlign = "CENTER", fontWeight = "BOLD")
                    ))
                )
            ),
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
        components = listOf(
            PageComponent.ProfileHeader(
                imageUrl = "https://ui-avatars.com/api/?name=Genesys+User&size=300&background=000&color=fff",
                name = "Seu Nome",
                bio = "Criador de Conteúdo | Engenheiro de Software | Mentor",
            ),
            PageComponent.SocialLinks(instagram = "#", whatsapp = "#", youtube = "#"),
            PageComponent.Spacer(height = 24),
            PageComponent.Grid(
                columns = 1,
                items = listOf(
                    PageComponent.GridItem(components = listOf(PageComponent.Button(text = "🚀 Último Vídeo no YouTube", url = "#"))),
                    PageComponent.GridItem(components = listOf(PageComponent.Button(text = "📚 Baixar meu E-book Grátis", url = "#"))),
                    PageComponent.GridItem(components = listOf(PageComponent.Button(text = "🎙️ Ouça meu Podcast", url = "#")))
                )
            ),
            PageComponent.Header(title = "Projetos em Destaque", fontSize = 20),
            PageComponent.ProductList(isHorizontal = true)
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

        // CORREÇÃO: Sincroniza o storeId em todos os componentes que possuem referências a produtos/serviços
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
