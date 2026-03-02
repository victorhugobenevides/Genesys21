package com.itbenevides.genesys21.screenshot.components

import app.cash.paparazzi.Paparazzi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.PageThemeConfig
import com.itbenevides.genesys21.ui.components.feedback.ShimmerCirclePlaceholder
import com.itbenevides.genesys21.ui.components.feedback.ShimmerRectPlaceholder
import com.itbenevides.genesys21.ui.components.feedback.ShimmerTextPlaceholder
import com.itbenevides.genesys21.ui.components.image.GenesysImage
import com.itbenevides.genesys21.screenshot.base.TestImageProvider
import com.itbenevides.genesys21.ui.components.layout.GenesysSpacer
import com.itbenevides.genesys21.ui.components.layout.GenesysSpacing
import com.itbenevides.genesys21.ui.components.text.GenesysText
import com.itbenevides.genesys21.ui.components.text.GenesysTextStyle
import com.itbenevides.genesys21.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test

/**
 * Testes de screenshot específicos para o sistema de shimmer e loading states.
 */
class ShimmerSystemScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi()

    @Test
    fun testShimmerVariations() {
        paparazzi.snapshot(name = "shimmer_all_variations") {
            AppTheme(themeConfig = PageThemeConfig.ROYAL) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        GenesysText("Sistema Shimmer", style = GenesysTextStyle.Title)
                        
                        GenesysText("Retângulo", style = GenesysTextStyle.Label)
                        ShimmerRectPlaceholder(width = 200.dp, height = 100.dp)
                        
                        GenesysText("Círculo", style = GenesysTextStyle.Label)
                        ShimmerCirclePlaceholder(size = 64.dp)
                        
                        GenesysText("Texto", style = GenesysTextStyle.Label)
                        ShimmerTextPlaceholder(width = 0.8f)
                        
                        GenesysSpacer(GenesysSpacing.Medium)
                        
                        GenesysText("Lista", style = GenesysTextStyle.Label)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            repeat(3) {
                                ShimmerTextPlaceholder(width = 0.9f - (it * 0.2f))
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testImageLoadingStates() {
        paparazzi.snapshot(name = "image_loading_states") {
            AppTheme(themeConfig = PageThemeConfig.OCEAN) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        GenesysText("Estados de Imagem", style = GenesysTextStyle.Title)
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                                GenesysText("Placeholder", style = GenesysTextStyle.Label)
                                GenesysImage(url = "", size = 80.dp, showShimmer = false)
                            }
                            Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                                GenesysText("Com Shimmer", style = GenesysTextStyle.Label)
                                GenesysImage(url = "loading", size = 80.dp, showShimmer = true)
                            }
                            Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                                GenesysText("Erro", style = GenesysTextStyle.Label)
                                GenesysImage(url = "error", size = 80.dp, showShimmer = false)
                            }
                        }
                        
                        GenesysSpacer(GenesysSpacing.Medium)
                        
                        GenesysText("Variações Circulares", style = GenesysTextStyle.Label)
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            GenesysImage(url = "", size = 64.dp, isCircular = true)
                            GenesysImage(url = "loading", size = 64.dp, isCircular = true, showShimmer = true)
                            GenesysImage(url = TestImageProvider.mockImageUrl(), size = 64.dp, isCircular = true)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testLoadingPageMockup() {
        paparazzi.snapshot(name = "loading_page_mockup") {
            AppTheme(themeConfig = PageThemeConfig.SUNSET) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        ShimmerRectPlaceholder(width = 200.dp, height = 24.dp)
                        GenesysSpacer(GenesysSpacing.Small)
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            ShimmerCirclePlaceholder(size = 80.dp)
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                ShimmerTextPlaceholder(width = 0.6f)
                                ShimmerTextPlaceholder(width = 0.4f)
                            }
                        }
                        
                        GenesysSpacer(GenesysSpacing.Medium)
                        
                        repeat(3) {
                            ShimmerRectPlaceholder(height = 80.dp)
                            GenesysSpacer(GenesysSpacing.Small)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testShimmerVsStatic() {
        paparazzi.snapshot(name = "shimmer_vs_static_comparison") {
            AppTheme(themeConfig = PageThemeConfig.FOREST) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        GenesysText("Comparação: Shimmer vs Estático", style = GenesysTextStyle.Title)
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                                GenesysText("Com Shimmer", style = GenesysTextStyle.Label)
                                GenesysImage(url = "loading", size = 100.dp, showShimmer = true)
                            }
                            Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                                GenesysText("Estático", style = GenesysTextStyle.Label)
                                GenesysImage(url = "", size = 100.dp, showShimmer = false)
                            }
                            Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                                GenesysText("Carregado", style = GenesysTextStyle.Label)
                                GenesysImage(url = TestImageProvider.mockImageUrl(), size = 100.dp)
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testCardLoadingStates() {
        paparazzi.snapshot(name = "card_loading_states") {
            AppTheme(themeConfig = PageThemeConfig.BERRY) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GenesysText("Cards em Loading", style = GenesysTextStyle.Title)
                        
                        com.itbenevides.genesys21.ui.components.card.GenesysCard {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                ShimmerTextPlaceholder(width = 0.7f)
                                ShimmerTextPlaceholder(width = 0.5f)
                                GenesysSpacer(GenesysSpacing.Small)
                                ShimmerRectPlaceholder(height = 120.dp)
                            }
                        }
                        
                        com.itbenevides.genesys21.ui.components.card.GenesysCard {
                            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                ShimmerCirclePlaceholder(size = 60.dp)
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                                    ShimmerTextPlaceholder(width = 0.8f)
                                    ShimmerTextPlaceholder(width = 0.6f)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}