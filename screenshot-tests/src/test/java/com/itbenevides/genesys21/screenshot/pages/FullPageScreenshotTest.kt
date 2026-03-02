package com.itbenevides.genesys21.screenshot.pages

import app.cash.paparazzi.Paparazzi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.domain.model.PageThemeConfig
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.domain.model.StepItem
import com.itbenevides.genesys21.presentation.screens.viewer.PageComponentRenderer
import com.itbenevides.genesys21.screenshot.base.TestImageProvider
import com.itbenevides.genesys21.ui.components.appbar.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.button.GenesysIconButton
import com.itbenevides.genesys21.ui.components.layout.GenesysLazyColumn
import com.itbenevides.genesys21.ui.components.layout.GenesysSpacing
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test

/**
 * Testes de screenshot para páginas completas com todos os temas disponíveis.
 * Cada teste renderiza uma página inteira em um tema específico.
 */
class FullPageScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi()

    private fun sampleProducts() = listOf(
        Product(id = "1", name = "Produto 1", price = 99.9, categoryName = "Categoria A", imageUrls = listOf(TestImageProvider.mockImageUrl())),
        Product(id = "2", name = "Produto 2", price = 149.9, categoryName = "Categoria B", imageUrls = listOf(TestImageProvider.mockImageUrl())),
        Product(id = "3", name = "Produto 3", price = 199.9, categoryName = "Categoria A", imageUrls = listOf(TestImageProvider.mockImageUrl()))
    )

    private fun buildFullPage(theme: PageThemeConfig, title: String) = Page(
        id = "page_test",
        title = title,
        ownerId = "owner_test",
        theme = theme,
        components = listOf(
            PageComponent.Header(title = "Bem-vindo", fontSize = 28, textAlign = "CENTER"),
            PageComponent.Typography(text = "Texto de apresentação", fontSize = 16, textAlign = "CENTER"),
            PageComponent.Media(url = TestImageProvider.mockImageUrl(), title = "Banner", layout = "FULL_WIDTH"),
            PageComponent.Highlight(text = "Destaque Principal", type = "BUTTON", usePrimaryColor = true),
            PageComponent.ProductList(products = sampleProducts(), isHorizontal = true),
            PageComponent.ProductList(products = sampleProducts(), isHorizontal = false),
            PageComponent.StepProcess(steps = listOf(
                StepItem("Passo 1", "Descrição do passo 1"),
                StepItem("Passo 2", "Descrição do passo 2"),
                StepItem("Passo 3", "Descrição do passo 3")
            )),
            PageComponent.Testimonial(quote = "Excelente serviço!", author = "Cliente Satisfeito"),
            PageComponent.ProfileHeader(imageUrl = TestImageProvider.mockImageUrl(), name = "Loja Demo", bio = "Sua loja favorita"),
            PageComponent.SocialLinks(instagram = "@loja", whatsapp = "5511999999999", email = "contato@loja.com"),
            PageComponent.Search(placeholder = "Buscar produtos..."),
            PageComponent.CategoryFilter(),
            PageComponent.Image(url = TestImageProvider.mockImageUrl(), size = 200, isFullWidth = true)
        )
    )

    @Composable
    private fun FullPageContent(page: Page) {
        val selectedCategory = remember { mutableStateOf<String?>(null) }

        AppTheme(themeConfig = page.theme) {
            Scaffold(
                topBar = {
                    GenesysTopAppBar(
                        title = page.title,
                        onBack = {},
                        actions = {
                            GenesysIconButton(icon = GenesysIcons.Search, onClick = {})
                            GenesysIconButton(icon = GenesysIcons.ShoppingBag, onClick = {})
                        }
                    )
                }
            ) { padding ->
                GenesysLazyColumn(
                    items = page.components,
                    modifier = Modifier.padding(padding),
                    usePadding = true,
                    spacing = GenesysSpacing.Medium
                ) { component ->
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

    @Test
    fun testFullPageThemeRoyal() {
        val page = buildFullPage(PageThemeConfig.ROYAL, "Loja Royal")
        paparazzi.snapshot(name = "full_page_royal") {
            FullPageContent(page)
        }
    }

    @Test
    fun testFullPageThemeOcean() {
        val page = buildFullPage(PageThemeConfig.OCEAN, "Loja Ocean")
        paparazzi.snapshot(name = "full_page_ocean") {
            FullPageContent(page)
        }
    }

    @Test
    fun testFullPageThemeForest() {
        val page = buildFullPage(PageThemeConfig.FOREST, "Loja Forest")
        paparazzi.snapshot(name = "full_page_forest") {
            FullPageContent(page)
        }
    }

    @Test
    fun testFullPageThemeSunset() {
        val page = buildFullPage(PageThemeConfig.SUNSET, "Loja Sunset")
        paparazzi.snapshot(name = "full_page_sunset") {
            FullPageContent(page)
        }
    }

    @Test
    fun testFullPageThemeBerry() {
        val page = buildFullPage(PageThemeConfig.BERRY, "Loja Berry")
        paparazzi.snapshot(name = "full_page_berry") {
            FullPageContent(page)
        }
    }

    @Test
    fun testFullPageThemeMinimal() {
        val page = buildFullPage(PageThemeConfig.MINIMAL, "Loja Minimal")
        paparazzi.snapshot(name = "full_page_minimal") {
            FullPageContent(page)
        }
    }

    @Test
    fun testFullPageThemeDarkMode() {
        val page = buildFullPage(PageThemeConfig.DARK_MODE, "Loja Dark")
        paparazzi.snapshot(name = "full_page_dark") {
            FullPageContent(page)
        }
    }

    @Test
    fun testFullPageThemeVintage() {
        val page = buildFullPage(PageThemeConfig.VINTAGE, "Loja Vintage")
        paparazzi.snapshot(name = "full_page_vintage") {
            FullPageContent(page)
        }
    }

    @Test
    fun testFullPageThemeNordic() {
        val page = buildFullPage(PageThemeConfig.NORDIC, "Loja Nordic")
        paparazzi.snapshot(name = "full_page_nordic") {
            FullPageContent(page)
        }
    }

    @Test
    fun testFullPageThemeCoffee() {
        val page = buildFullPage(PageThemeConfig.COFFEE, "Loja Coffee")
        paparazzi.snapshot(name = "full_page_coffee") {
            FullPageContent(page)
        }
    }

    @Test
    fun testFullPageThemeCandy() {
        val page = buildFullPage(PageThemeConfig.CANDY, "Loja Candy")
        paparazzi.snapshot(name = "full_page_candy") {
            FullPageContent(page)
        }
    }

    @Test
    fun testFullPageThemeMidnight() {
        val page = buildFullPage(PageThemeConfig.MIDNIGHT, "Loja Midnight")
        paparazzi.snapshot(name = "full_page_midnight") {
            FullPageContent(page)
        }
    }

    @Test
    fun testFullPageThemeNeon() {
        val page = buildFullPage(PageThemeConfig.NEON, "Loja Neon")
        paparazzi.snapshot(name = "full_page_neon") {
            FullPageContent(page)
        }
    }

    @Test
    fun testFullPageThemeDefault() {
        val page = buildFullPage(PageThemeConfig.DEFAULT, "Loja Default")
        paparazzi.snapshot(name = "full_page_default") {
            FullPageContent(page)
        }
    }
}