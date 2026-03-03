package com.itbenevides.genesys21.domain.model

import kotlin.random.Random

object PageTemplates {
    fun create(type: PageTemplateType, title: String): Page {
        val generatedId = generateId()
        return when (type) {
            PageTemplateType.EMPTY -> Page(id = generatedId, title = title)
            PageTemplateType.STORE -> createStoreTemplate(generatedId, title)
            PageTemplateType.BIO -> createBioTemplate(generatedId, title)
            PageTemplateType.LANDING -> createLandingTemplate(generatedId, title)
        }
    }

    private fun generateId(): String {
        val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..12)
            .map { Random.nextInt(0, charPool.size).let { charPool[it] } }
            .joinToString("")
    }

    private fun createStoreTemplate(id: String, title: String): Page {
        val products = listOf(
            Product(
                id = "p1",
                name = "Smartphone Galaxy S24",
                price = 5999.0,
                description = "O mais novo smartphone com IA integrada.",
                categoryName = "Eletrônicos",
                stock = 12,
                imageUrls = listOf("https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?w=800")
            ),
            Product(
                id = "p2",
                name = "iPhone 15 Pro",
                price = 7299.0,
                description = "Titânio. Tão forte. Tão leve. Tão Pro.",
                categoryName = "Eletrônicos",
                stock = 5,
                imageUrls = listOf("https://images.unsplash.com/photo-1696446701796-da61225697cc?w=800")
            ),
            Product(
                id = "p3",
                name = "Camiseta Algodão Premium",
                price = 129.90,
                description = "Conforto e estilo para o dia a dia.",
                categoryName = "Moda",
                stock = 50,
                imageUrls = listOf("https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=800")
            ),
            Product(
                id = "p4",
                name = "Tênis Esportivo Run",
                price = 349.90,
                description = "Alta performance para suas corridas.",
                categoryName = "Moda",
                stock = 20,
                imageUrls = listOf("https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800")
            ),
            Product(
                id = "p5",
                name = "Fone Bluetooth Noise Cancelling",
                price = 899.0,
                description = "Mergulhe na sua música sem interrupções.",
                categoryName = "Acessórios",
                stock = 15,
                imageUrls = listOf("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800")
            ),
            Product(
                id = "p6",
                name = "Cafeteira Espresso Automática",
                price = 1450.0,
                description = "O melhor café no conforto da sua casa.",
                categoryName = "Casa",
                stock = 8,
                imageUrls = listOf("https://images.unsplash.com/photo-1517668808822-99021d5c76ec?w=800")
            )
        )

        val components = listOf(
            PageComponent.Header(
                title = title,
                textAlign = "CENTER",
                fontSize = 28
            ),
            PageComponent.Search(
                placeholder = "O que você procura hoje?"
            ),
            PageComponent.Media(
                url = "https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=1200",
                title = "Nova Coleção de Inverno",
                description = "Confira as tendências que acabaram de chegar com descontos de até 40%.",
                layout = "FULL_WIDTH",
                size = 250
            ),
            PageComponent.CategoryFilter(),
            PageComponent.Typography(
                text = "Ofertas em Destaque",
                fontWeight = "EXTRA_BOLD",
                fontSize = 20,
                textAlign = "LEFT"
            ),
            PageComponent.ProductList(
                products = products.take(3),
                isHorizontal = true
            ),
            PageComponent.Highlight(
                text = "VER TODAS AS OFERTAS",
                usePrimaryColor = true
            ),
            PageComponent.Typography(
                text = "Catálogo Completo",
                fontWeight = "EXTRA_BOLD",
                fontSize = 20,
                textAlign = "LEFT"
            ),
            PageComponent.ProductList(
                products = products,
                isHorizontal = false
            ),
            PageComponent.StepProcess(
                steps = listOf(
                    StepItem("Escolha seu Produto", "Navegue pelo nosso catálogo e adicione ao carrinho."),
                    StepItem("Pagamento Seguro", "Aceitamos diversas formas de pagamento com total segurança."),
                    StepItem("Receba em Casa", "Entrega rápida e rastreada em todo o Brasil.")
                )
            ),
            PageComponent.Testimonial(
                quote = "A melhor experiência de compra que já tive. O produto chegou antes do prazo e a qualidade é impecável!",
                author = "Ricardo Oliveira"
            ),
            PageComponent.SocialLinks(
                instagram = "https://instagram.com/genesys21",
                whatsapp = "https://wa.me/5511999999999",
                email = "contato@loja.com"
            ),
            PageComponent.Typography(
                text = "© 2024 $title - Todos os direitos reservados.",
                fontSize = 12,
                textAlign = "CENTER"
            )
        )

        return Page(
            id = id,
            title = title,
            components = components,
            theme = PageThemeConfig.OCEAN
        )
    }

    private fun createBioTemplate(id: String, title: String): Page {
        return Page(
            id = id,
            title = title,
            theme = PageThemeConfig.BERRY,
            components = listOf(
                PageComponent.ProfileHeader(
                    imageUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400",
                    name = title,
                    bio = "Empreendedor | Criador de Conteúdo | Tech Enthusiast"
                ),
                PageComponent.Highlight(text = "Meu Novo Curso", usePrimaryColor = true),
                PageComponent.Highlight(text = "Canal no YouTube"),
                PageComponent.Highlight(text = "Agende uma Consultoria"),
                PageComponent.SocialLinks(
                    instagram = "https://instagram.com",
                    whatsapp = "https://wa.me/5511999999999"
                )
            )
        )
    }

    private fun createLandingTemplate(id: String, title: String): Page {
        return Page(
            id = id,
            title = title,
            theme = PageThemeConfig.ROYAL,
            components = listOf(
                PageComponent.Header(title = title, fontSize = 32),
                PageComponent.Typography(text = "A solução definitiva para o seu negócio crescer de forma escalável e sustentável.", textAlign = "CENTER"),
                PageComponent.Media(
                    url = "https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=1200",
                    layout = "FULL_WIDTH",
                    size = 300
                ),
                PageComponent.Highlight(text = "QUERO COMEÇAR AGORA", usePrimaryColor = true),
                PageComponent.StepProcess(
                    steps = listOf(
                        StepItem("Fase 1", "Análise de mercado"),
                        StepItem("Fase 2", "Implementação"),
                        StepItem("Fase 3", "Escala")
                    )
                )
            )
        )
    }
}
