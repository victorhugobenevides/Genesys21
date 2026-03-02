package com.itbenevides.genesys21.screenshot.integration

import app.cash.paparazzi.Paparazzi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.domain.model.PageThemeConfig
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.domain.model.StepItem
import com.itbenevides.genesys21.presentation.screens.viewer.PageComponentRenderer
import com.itbenevides.genesys21.screenshot.base.TestImageProvider
import com.itbenevides.genesys21.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test

/**
 * Testa todas as configurações possíveis de componentes na tela WhiteLabel (modo edição).
 */
class WhiteLabelAllConfigurationsScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi()

    private fun sampleProducts() = listOf(
        Product(id = "p1", name = "Produto 1", price = 99.9, categoryName = "Eletrônicos", imageUrls = listOf(TestImageProvider.mockImageUrl())),
        Product(id = "p2", name = "Produto 2", price = 49.9, categoryName = "Roupas", imageUrls = listOf(TestImageProvider.mockImageUrl()))
    )

    @Test
    fun testWhiteLabelAllComponentConfigurations() {
        val components = listOf(
            PageComponent.Typography(text = "Texto padrão", fontSize = 16, textAlign = "LEFT"),
            PageComponent.Typography(text = "Texto centralizado", fontSize = 18, textAlign = "CENTER", fontWeight = "BOLD"),
            PageComponent.Typography(text = "Texto em destaque", fontSize = 20, textAlign = "RIGHT", usePrimaryColor = true, isUppercase = true),
            PageComponent.Header(title = "Título padrão", fontSize = 24, textAlign = "LEFT"),
            PageComponent.Header(title = "Título Central", fontSize = 28, textAlign = "CENTER", isUppercase = true),
            PageComponent.Media(
                url = TestImageProvider.mockImageUrl(),
                title = "Mídia Full Width",
                description = "Descrição da mídia em largura total",
                layout = "FULL_WIDTH",
                size = 220,
                isRounded = true
            ),
            PageComponent.Media(
                url = TestImageProvider.mockImageUrl(),
                title = "Mídia Side Text",
                description = "Layout lado a lado com texto",
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
            PageComponent.Highlight(text = "Destaque secundário", type = "TEXT", usePrimaryColor = false),
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
            PageComponent.Testimonial(quote = "Excelente atendimento!", author = "Cliente A"),
            PageComponent.Testimonial(quote = "Entrega rápida e produto ótimo.", author = "Cliente B"),
            PageComponent.SocialLinks(instagram = "@loja", whatsapp = "5511999999999", email = "contato@loja.com"),
            PageComponent.ProfileHeader(
                imageUrl = TestImageProvider.mockImageUrl(),
                name = "Loja Demo",
                bio = "Bio com mais detalhes sobre a marca",
                imageSize = 120,
                isCircular = true
            ),
            PageComponent.Search(placeholder = "Buscar produtos...")
        )

        PageThemeConfig.entries.forEach { theme ->
            paparazzi.snapshot(name = "whitelabel_all_component_configurations_${theme.name.lowercase()}") {
                AppTheme(themeConfig = theme) {
                    Surface(modifier = Modifier.padding(16.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            components.forEach { component ->
                                PageComponentRenderer(
                                    component = component,
                                    allComponents = components,
                                    isEditMode = true
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
