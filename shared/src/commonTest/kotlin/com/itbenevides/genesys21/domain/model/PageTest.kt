package com.itbenevides.genesys21.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PageTest {

    @Test
    fun page_should_create_with_default_values() {
        val page = Page()

        assertEquals("", page.id)
        assertEquals("", page.title)
        assertEquals(null, page.ownerId)
        assertEquals(null, page.customDomain)
        assertEquals(null, page.whatsapp)
        assertEquals(emptyList<PageComponent>(), page.components)
        assertEquals(PageThemeConfig.ROYAL, page.theme)
    }

    @Test
    fun page_should_create_with_all_values() {
        val components = listOf(
            PageComponent.Typography(text = "Hello"),
            PageComponent.Header(title = "Header")
        )
        val page = Page(
            id = "page1",
            title = "My Page",
            ownerId = "user1",
            customDomain = "mypage.com",
            whatsapp = "5511999999999",
            components = components,
            theme = PageThemeConfig.OCEAN
        )

        assertEquals("page1", page.id)
        assertEquals("My Page", page.title)
        assertEquals("user1", page.ownerId)
        assertEquals("mypage.com", page.customDomain)
        assertEquals("5511999999999", page.whatsapp)
        assertEquals(components, page.components)
        assertEquals(PageThemeConfig.OCEAN, page.theme)
    }
}

class PageComponentTest {

    @Test
    fun typography_should_create_with_defaults() {
        val component = PageComponent.Typography()

        assertEquals("", component.text)
        assertEquals("BODY", component.style)
        assertEquals(16, component.fontSize)
        assertEquals("LEFT", component.textAlign)
        assertEquals("NORMAL", component.fontWeight)
        assertEquals(false, component.isUppercase)
        assertEquals(false, component.usePrimaryColor)
        assertEquals("Texto", component.customLabel)
        assertEquals(false, component.isFilterable)
    }

    @Test
    fun typography_should_create_with_custom_values() {
        val component = PageComponent.Typography(
            text = "Hello World",
            style = "HEADING",
            fontSize = 24,
            textAlign = "CENTER",
            fontWeight = "BOLD",
            isUppercase = true,
            usePrimaryColor = true
        )

        assertEquals("Hello World", component.text)
        assertEquals("HEADING", component.style)
        assertEquals(24, component.fontSize)
        assertEquals("CENTER", component.textAlign)
        assertEquals("BOLD", component.fontWeight)
        assertEquals(true, component.isUppercase)
        assertEquals(true, component.usePrimaryColor)
    }

    @Test
    fun header_should_create_with_defaults() {
        val component = PageComponent.Header()

        assertEquals("", component.title)
        assertEquals("CENTER", component.textAlign)
        assertEquals(false, component.isUppercase)
        assertEquals(24, component.fontSize)
        assertEquals("Cabeçalho", component.customLabel)
    }

    @Test
    fun media_should_create_with_defaults() {
        val component = PageComponent.Media()

        assertEquals("", component.url)
        assertEquals(null, component.title)
        assertEquals(null, component.description)
        assertEquals("FULL_WIDTH", component.layout)
        assertEquals(false, component.imageOnRight)
        assertEquals(300, component.size)
        assertEquals(true, component.isRounded)
        assertEquals(false, component.hasBottomArc)
        assertEquals("Mídia", component.customLabel)
    }

    @Test
    fun image_should_create_with_defaults() {
        val component = PageComponent.Image()

        assertEquals("", component.url)
        assertEquals(200, component.size)
        assertEquals(true, component.isFullWidth)
        assertEquals(false, component.isCircular)
        assertEquals("Imagem", component.customLabel)
    }

    @Test
    fun highlight_should_create_with_defaults() {
        val component = PageComponent.Highlight()

        assertEquals("", component.text)
        assertEquals("BUTTON", component.type)
        assertEquals(null, component.url)
        assertEquals(null, component.textColor)
        assertEquals(false, component.usePrimaryColor)
        assertEquals("Destaque", component.customLabel)
    }

    @Test
    fun productList_should_create_with_defaults() {
        val component = PageComponent.ProductList()

        assertEquals(emptyList<Product>(), component.products)
        assertEquals(false, component.isHorizontal)
        assertEquals("Produtos", component.customLabel)
        assertEquals(true, component.isFilterable)
    }

    @Test
    fun productList_should_calculate_products() {
        val products = listOf(
            Product(id = "p1", name = "Product 1", price = 10.0),
            Product(id = "p2", name = "Product 2", price = 20.0)
        )
        val component = PageComponent.ProductList(products = products)

        assertEquals(2, component.products.size)
        assertEquals("Product 1", component.products[0].name)
    }

    @Test
    fun categoryFilter_should_create_with_defaults() {
        val component = PageComponent.CategoryFilter()

        assertEquals("Categorias", component.customLabel)
        assertEquals(false, component.isFilterable)
    }

    @Test
    fun stepProcess_should_create_with_defaults() {
        val component = PageComponent.StepProcess()

        assertEquals(emptyList<StepItem>(), component.steps)
        assertEquals("Processo", component.customLabel)
    }

    @Test
    fun stepProcess_should_create_with_steps() {
        val steps = listOf(
            StepItem("Step 1", "Description 1"),
            StepItem("Step 2", "Description 2")
        )
        val component = PageComponent.StepProcess(steps = steps)

        assertEquals(2, component.steps.size)
        assertEquals("Step 1", component.steps[0].title)
    }

    @Test
    fun testimonial_should_create_with_defaults() {
        val component = PageComponent.Testimonial()

        assertEquals("", component.quote)
        assertEquals("", component.author)
        assertEquals("Depoimento", component.customLabel)
    }

    @Test
    fun testimonial_should_create_with_values() {
        val component = PageComponent.Testimonial(
            quote = "Great product!",
            author = "John Doe"
        )

        assertEquals("Great product!", component.quote)
        assertEquals("John Doe", component.author)
    }

    @Test
    fun socialLinks_should_create_with_defaults() {
        val component = PageComponent.SocialLinks()

        assertEquals(null, component.instagram)
        assertEquals(null, component.whatsapp)
        assertEquals(null, component.email)
        assertEquals("Social", component.customLabel)
    }

    @Test
    fun socialLinks_should_create_with_all_links() {
        val component = PageComponent.SocialLinks(
            instagram = "@user",
            whatsapp = "5511999999999",
            email = "user@example.com"
        )

        assertEquals("@user", component.instagram)
        assertEquals("5511999999999", component.whatsapp)
        assertEquals("user@example.com", component.email)
    }

    @Test
    fun profileHeader_should_create_with_defaults() {
        val component = PageComponent.ProfileHeader()

        assertEquals("", component.imageUrl)
        assertEquals("", component.name)
        assertEquals("", component.bio)
        assertEquals(120, component.imageSize)
        assertEquals(true, component.isCircular)
        assertEquals("Perfil", component.customLabel)
    }

    @Test
    fun profileHeader_should_create_with_values() {
        val component = PageComponent.ProfileHeader(
            imageUrl = "https://example.com/photo.jpg",
            name = "Victor",
            bio = "Developer",
            imageSize = 150,
            isCircular = false
        )

        assertEquals("https://example.com/photo.jpg", component.imageUrl)
        assertEquals("Victor", component.name)
        assertEquals("Developer", component.bio)
        assertEquals(150, component.imageSize)
        assertEquals(false, component.isCircular)
    }

    @Test
    fun search_should_create_with_defaults() {
        val component = PageComponent.Search()

        assertEquals("O que você procura?", component.placeholder)
        assertEquals("Busca", component.customLabel)
    }

    @Test
    fun unknown_should_create_with_defaults() {
        val component = PageComponent.Unknown()

        assertEquals("Desconhecido", component.customLabel)
        assertEquals(false, component.isFilterable)
    }
}
