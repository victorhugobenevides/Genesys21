package com.itbenevides.genesys21.screenshot.pages

import app.cash.paparazzi.Paparazzi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.domain.model.PageThemeConfig
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.domain.model.StepItem
import com.itbenevides.genesys21.presentation.screens.viewer.PageComponentRenderer
import com.itbenevides.genesys21.screenshot.base.TestImageProvider
import com.itbenevides.genesys21.ui.components.appbar.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.button.GenesysIconButton
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test

/**
 * Testes de screenshot para a página pública (PageViewer) com todos os componentes.
 */
class PublicPageScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi()

    private fun sampleProducts() = listOf(
        Product(
            id = "p1",
            name = "Fone Bluetooth",
            price = 199.9,
            categoryName = "Eletrônicos",
            imageUrls = listOf(TestImageProvider.mockImageUrl())
        ),
        Product(
            id = "p2",
            name = "Smartwatch",
            price = 399.9,
            categoryName = "Eletrônicos",
            imageUrls = listOf(TestImageProvider.mockImageUrl())
        ),
        Product(
            id = "p3",
            name = "Camiseta",
            price = 49.9,
            categoryName = "Roupas",
            imageUrls = listOf(TestImageProvider.mockImageUrl())
        )
    )

    private fun buildPublicPage(theme: PageThemeConfig) = Page(
        id = "page_1",
        title = "Loja Demo",
        ownerId = "owner_1",
        theme = theme,
        components = listOf(
            PageComponent.Header(title = "Bem-vindo à Loja", textAlign = "CENTER", fontSize = 28),
            PageComponent.Typography(
                text = "Explore nossos produtos mais vendidos e ofertas especiais.",
                fontSize = 16,
                textAlign = "CENTER",
                usePrimaryColor = true
            ),
            PageComponent.Media(
                url = TestImageProvider.mockImageUrl(),
                title = "Destaque Principal",
                description = "Produtos exclusivos com entrega rápida.",
                layout = "FULL_WIDTH",
                size = 280,
                isRounded = true
            ),
            PageComponent.ProductList(products = sampleProducts(), isHorizontal = true),
            PageComponent.CategoryFilter(),
            PageComponent.ProductList(products = sampleProducts(), isHorizontal = false),
            PageComponent.Highlight(text = "Confira o catálogo completo", type = "BUTTON", usePrimaryColor = true),
            PageComponent.StepProcess(
                steps = listOf(
                    StepItem("Escolha", "Selecione seu produto favorito"),
                    StepItem("Pague", "Finalize sua compra"),
                    StepItem("Receba", "Entrega rápida na sua casa")
                )
            ),
            PageComponent.Testimonial(
                quote = "A melhor loja que já comprei! Atendimento incrível.",
                author = "Cliente Satisfeito"
            ),
            PageComponent.ProfileHeader(
                imageUrl = TestImageProvider.mockImageUrl(),
                name = "Loja Demo",
                bio = "Sua loja online favorita",
                imageSize = 120,
                isCircular = true
            ),
            PageComponent.Search(placeholder = "Buscar produtos..."),
            PageComponent.SocialLinks(
                instagram = "https://instagram.com/loja",
                whatsapp = "https://wa.me/5511999999999",
                email = "contato@loja.com"
            ),
            PageComponent.Image(
                url = TestImageProvider.mockImageUrl(),
                size = 220,
                isFullWidth = true,
                isCircular = false
            )
        )
    )

    private fun buildPublicPageAllConfigs(theme: PageThemeConfig) = Page(
        id = "page_variants",
        title = "Página Pública Completa",
        ownerId = "owner_1",
        theme = theme,
        components = listOf(
            PageComponent.Header(title = "Header Esquerda", textAlign = "LEFT", fontSize = 24),
            PageComponent.Header(title = "Header Centro", textAlign = "CENTER", fontSize = 28, isUppercase = true),
            PageComponent.Typography(text = "Texto normal", fontSize = 16, textAlign = "LEFT"),
            PageComponent.Typography(text = "Texto centralizado", fontSize = 18, textAlign = "CENTER", usePrimaryColor = true),
            PageComponent.Media(
                url = TestImageProvider.mockImageUrl(),
                title = "Mídia Full Width",
                description = "Banner principal",
                layout = "FULL_WIDTH",
                size = 240,
                isRounded = true
            ),
            PageComponent.Media(
                url = TestImageProvider.mockImageUrl(),
                title = "Mídia Side Text",
                description = "Imagem à esquerda com texto",
                layout = "SIDE_TEXT",
                size = 140,
                imageOnRight = false,
                isRounded = false
            ),
            PageComponent.Media(
                url = TestImageProvider.mockImageUrl(),
                title = "Mídia Side Text Direita",
                description = "Imagem à direita com texto",
                layout = "SIDE_TEXT",
                size = 140,
                imageOnRight = true,
                isRounded = true
            ),
            PageComponent.Media(
                url = TestImageProvider.mockImageUrl(),
                title = "Mídia Circular",
                layout = "CIRCULAR",
                size = 120,
                isRounded = true
            ),
            PageComponent.Image(url = TestImageProvider.mockImageUrl(), size = 180, isFullWidth = true),
            PageComponent.Image(url = TestImageProvider.mockImageUrl(), size = 140, isFullWidth = false, isCircular = true),
            PageComponent.Highlight(text = "Destaque primário", type = "BUTTON", usePrimaryColor = true),
            PageComponent.Highlight(text = "Destaque texto", type = "TEXT", usePrimaryColor = false),
            PageComponent.ProductList(products = sampleProducts(), isHorizontal = true),
            PageComponent.ProductList(products = sampleProducts(), isHorizontal = false),
            PageComponent.CategoryFilter(),
            PageComponent.StepProcess(
                steps = listOf(
                    StepItem("Passo 1", "Descrição 1"),
                    StepItem("Passo 2", "Descrição 2"),
                    StepItem("Passo 3", "Descrição 3")
                )
            ),
            PageComponent.Testimonial(quote = "Excelente compra!", author = "Cliente A"),
            PageComponent.Testimonial(quote = "Recomendo a loja!", author = "Cliente B"),
            PageComponent.ProfileHeader(
                imageUrl = TestImageProvider.mockImageUrl(),
                name = "Loja Demo",
                bio = "Bio com descrição mais longa",
                imageSize = 140,
                isCircular = true
            ),
            PageComponent.SocialLinks(instagram = "@loja", whatsapp = "5511999999999", email = "contato@loja.com"),
            PageComponent.Search(placeholder = "Buscar produtos...")
        )
    )

    @Composable
    private fun PublicPageContent(page: Page) {
        val selectedCategory = remember { mutableStateOf<String?>(null) }

        AppTheme(themeConfig = page.theme) {
            Scaffold(
                topBar = {
                    GenesysTopAppBar(
                        title = page.title,
                        onBack = {},
                        actions = {
                            GenesysIconButton(icon = GenesysIcons.List, onClick = {})
                            GenesysIconButton(icon = GenesysIcons.ShoppingBag, onClick = {})
                        }
                    )
                }
            ) { padding ->
                LazyColumn(
                    modifier = Modifier.padding(padding),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    itemsIndexed(page.components) { _, component ->
                        PageComponentRenderer(
                            component = component,
                            allComponents = page.components,
                            onProductClick = {},
                            selectedCategory = selectedCategory.value,
                            onCategorySelect = { selectedCategory.value = it }
                        )
                    }
                }
            }
        }
    }

    @Test
    fun testPublicPageAllComponentsRoyal() {
        val page = buildPublicPage(PageThemeConfig.ROYAL)
        paparazzi.snapshot(name = "public_page_royal") {
            PublicPageContent(page)
        }
    }

    @Test
    fun testPublicPageAllComponentsOcean() {
        val page = buildPublicPage(PageThemeConfig.OCEAN)
        paparazzi.snapshot(name = "public_page_ocean") {
            PublicPageContent(page)
        }
    }

    @Test
    fun testPublicPageAllComponentsDark() {
        val page = buildPublicPage(PageThemeConfig.DARK_MODE)
        paparazzi.snapshot(name = "public_page_dark") {
            PublicPageContent(page)
        }
    }

    @Test
    fun testPublicPageAllComponentConfigurations() {
        // Gerar snapshots para TODOS os temas com TODAS as configurações
        PageThemeConfig.entries.forEach { theme ->
            val page = buildPublicPageAllConfigs(theme)
            paparazzi.snapshot(name = "public_page_all_configurations_${theme.name.lowercase()}") {
                PublicPageContent(page)
            }
        }
    }

    @Test
    fun testPublicPageComponentMatrix() {
        // Testar matriz de componentes: (Media × Layout) × Temas
        val mediaLayouts = listOf(
            PageComponent.Media(layout = "FULL_WIDTH", title = "Full Width"),
            PageComponent.Media(layout = "SIDE_TEXT", title = "Side Left", imageOnRight = false),
            PageComponent.Media(layout = "SIDE_TEXT", title = "Side Right", imageOnRight = true),
            PageComponent.Media(layout = "CIRCULAR", title = "Circular")
        )

        PageThemeConfig.entries.take(5).forEach { theme ->
            val components = buildList {
                add(PageComponent.Header(title = "Media Variations - ${theme.name}"))
                addAll(mediaLayouts)
            }

            val page = Page(
                id = "media_matrix_${theme.name}",
                title = "Media Matrix",
                ownerId = "user-1",
                theme = theme,
                components = components
            )

            paparazzi.snapshot(name = "public_page_media_matrix_${theme.name.lowercase()}") {
                PublicPageContent(page)
            }
        }
    }

    @Test
    fun testPublicPageTypographyScale() {
        // Testar escala tipográfica em todos os temas
        PageThemeConfig.entries.forEach { theme ->
            val components = listOf(
                PageComponent.Typography(text = "Display Large", style = "HEADLINE", fontSize = 32, textAlign = "CENTER", fontWeight = "BOLD", isUppercase = true),
                PageComponent.Typography(text = "Heading Medium", style = "TITLE", fontSize = 24, textAlign = "LEFT", fontWeight = "BOLD", isUppercase = false),
                PageComponent.Typography(text = "Body text regular para testar leitura em ${theme.name}", style = "BODY", fontSize = 16, textAlign = "LEFT", fontWeight = "NORMAL", isUppercase = false),
                PageComponent.Typography(text = "CAPS FOR EMPHASIS", style = "LABEL", fontSize = 14, textAlign = "CENTER", fontWeight = "BOLD", isUppercase = true, usePrimaryColor = true)
            )

            val page = Page(
                id = "typography_${theme.name}",
                title = "Typography Scale",
                ownerId = "user-1",
                theme = theme,
                components = components
            )

            paparazzi.snapshot(name = "public_page_typography_${theme.name.lowercase()}") {
                PublicPageContent(page)
            }
        }
    }

    @Test
    fun testPublicPageCommerceVariants() {
        // Testar variações de comércio: produtos, categorias, carrinho
        val commerceConfigs = listOf(
            "Product List Horizontal" to listOf(
                PageComponent.ProductList(products = emptyList(), isHorizontal = true, customLabel = "Destaques")
            ),
            "Product List Vertical" to listOf(
                PageComponent.ProductList(products = emptyList(), isHorizontal = false, customLabel = "Todos os Produtos")
            ),
            "Category Filter + Products" to listOf(
                PageComponent.CategoryFilter(customLabel = "Categorias"),
                PageComponent.ProductList(products = emptyList(), isHorizontal = true, customLabel = "Mais Vendidos")
            ),
            "Search + Category + Products" to listOf(
                PageComponent.Search(placeholder = "Buscar produtos...", customLabel = "Buscar"),
                PageComponent.CategoryFilter(customLabel = "Categorias Populares"),
                PageComponent.ProductList(products = emptyList(), isHorizontal = false, customLabel = "Resultados")
            )
        )

        commerceConfigs.forEach { (configName, components) ->
            val page = Page(
                id = "commerce_${configName.lowercase().replace(" ", "_")}",
                title = configName,
                ownerId = "user-1",
                theme = PageThemeConfig.ROYAL,
                components = components
            )

            paparazzi.snapshot(name = "public_page_commerce_${configName.lowercase().replace(" ", "_")}") {
                PublicPageContent(page)
            }
        }
    }
}
