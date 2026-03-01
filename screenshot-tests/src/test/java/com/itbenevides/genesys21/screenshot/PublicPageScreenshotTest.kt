package com.itbenevides.genesys21.screenshot

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
            imageUrls = listOf("https://picsum.photos/200/200")
        ),
        Product(
            id = "p2",
            name = "Smartwatch",
            price = 399.9,
            categoryName = "Eletrônicos",
            imageUrls = listOf("https://picsum.photos/200/201")
        ),
        Product(
            id = "p3",
            name = "Camiseta",
            price = 49.9,
            categoryName = "Roupas",
            imageUrls = listOf("https://picsum.photos/200/202")
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
                url = "https://picsum.photos/800/400",
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
                imageUrl = "https://picsum.photos/100/100",
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
                url = "https://picsum.photos/600/400",
                size = 220,
                isFullWidth = true,
                isCircular = false
            )
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
}
