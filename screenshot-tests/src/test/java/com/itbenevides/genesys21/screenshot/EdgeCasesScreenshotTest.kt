package com.itbenevides.genesys21.screenshot

import app.cash.paparazzi.Paparazzi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.PageThemeConfig
import com.itbenevides.genesys21.ui.components.badge.GenesysBadge
import com.itbenevides.genesys21.ui.components.badge.GenesysStatusBadge
import com.itbenevides.genesys21.ui.components.badge.GenesysStockBadge
import com.itbenevides.genesys21.ui.components.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.card.GenesysCard
import com.itbenevides.genesys21.ui.components.feedback.*
import com.itbenevides.genesys21.ui.components.image.GenesysImage
import com.itbenevides.genesys21.ui.components.input.GenesysTextField
import com.itbenevides.genesys21.ui.components.layout.GenesysSpacer
import com.itbenevides.genesys21.ui.components.layout.GenesysSpacing
import com.itbenevides.genesys21.ui.components.text.GenesysText
import com.itbenevides.genesys21.ui.components.text.GenesysTextStyle
import com.itbenevides.genesys21.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test

/**
 * Testes de screenshot para edge cases: estados de erro, loading, vazio,
 * e situações extremas de UI.
 */
class EdgeCasesScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi()

    @Test
    fun testAllEmptyStates() {
        paparazzi.snapshot(name = "empty_states_all_variations") {
            AppTheme(themeConfig = PageThemeConfig.ROYAL) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        GenesysText("Estados Vazios", style = GenesysTextStyle.Title)
                        
                        GenesysEmptyState(
                            icon = com.itbenevides.genesys21.ui.components.theme.GenesysIcons.Search,
                            title = "Nenhum resultado",
                            description = "Tente uma busca diferente"
                        )
                        
                        GenesysEmptyState(
                            icon = com.itbenevides.genesys21.ui.components.theme.GenesysIcons.ShoppingBag,
                            title = "Carrinho vazio",
                            description = "Adicione produtos ao carrinho",
                            action = { GenesysLoadingButton(text = "Ver Produtos", onClick = {}) }
                        )
                        
                        GenesysEmptyState(
                            icon = com.itbenevides.genesys21.ui.components.theme.GenesysIcons.List,
                            title = "Sem pedidos",
                            description = "Você ainda não fez nenhum pedido"
                        )
                    }
                }
            }
        }
    }

    @Test
    fun testAllLoadingStates() {
        paparazzi.snapshot(name = "loading_states_all_variations") {
            AppTheme(themeConfig = PageThemeConfig.OCEAN) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        GenesysText("Estados de Loading", style = GenesysTextStyle.Title)
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            GenesysLoadingIndicator(size = 16.dp)
                            GenesysLoadingIndicator(size = 24.dp)
                            GenesysLoadingIndicator(size = 32.dp)
                            GenesysLoadingIndicator(size = 48.dp)
                        }
                        
                        Box(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                            GenesysLoadingOverlay()
                        }
                        
                        GenesysLoadingButton(text = "Carregando...", onClick = {}, isLoading = true)
                    }
                }
            }
        }
    }

    @Test
    fun testAllErrorStates() {
        paparazzi.snapshot(name = "error_states_all_variations") {
            AppTheme(themeConfig = PageThemeConfig.BERRY) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        GenesysText("Estados de Erro", style = GenesysTextStyle.Title)
                        
                        GenesysTextField(
                            value = "",
                            onValueChange = {},
                            label = "Email",
                            isError = true,
                            supportingText = "Email inválido"
                        )
                        
                        GenesysCard(
                            backgroundColor = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                GenesysText(
                                    "Erro de conexão",
                                    style = GenesysTextStyle.Title,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                GenesysText(
                                    "Não foi possível conectar ao servidor",
                                    style = GenesysTextStyle.Body,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                GenesysSpacer(GenesysSpacing.Small)
                                GenesysLoadingButton(
                                    text = "Tentar Novamente",
                                    onClick = {},
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        
                        // Error image placeholder
                        GenesysImage(
                            url = "invalid_url",
                            size = 80.dp,
                            showShimmer = false
                        )
                    }
                }
            }
        }
    }

    @Test
    fun testLongTextOverflow() {
        paparazzi.snapshot(name = "long_text_overflow") {
            AppTheme(themeConfig = PageThemeConfig.ROYAL) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GenesysText("Textos Longos", style = GenesysTextStyle.Title)
                        
                        GenesysText(
                            "Este é um título extremamente longo que deveria ser truncado corretamente sem quebrar o layout",
                            style = GenesysTextStyle.Title,
                            maxLines = 2
                        )
                        
                        GenesysText(
                            "Descrição muito longa que continua e continua sem parar, mostrando como o texto é tratado quando excede os limites do container disponível",
                            style = GenesysTextStyle.Body,
                            maxLines = 3
                        )
                        
                        GenesysBadge(
                            label = "NOVÍSSIMO",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    @Test
    fun testShimmerEffects() {
        paparazzi.snapshot(name = "shimmer_effects_variations") {
            AppTheme(themeConfig = PageThemeConfig.FOREST) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        GenesysText("Efeitos Shimmer", style = GenesysTextStyle.Title)
                        
                        ShimmerRectPlaceholder(width = 200.dp, height = 120.dp)
                        ShimmerCirclePlaceholder(size = 64.dp)
                        ShimmerTextPlaceholder(width = 0.8f)
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            repeat(3) {
                                ShimmerCirclePlaceholder(size = 48.dp)
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testComponentStatesMatrix() {
        paparazzi.snapshot(name = "component_states_matrix") {
            AppTheme(themeConfig = PageThemeConfig.SUNSET) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GenesysText("Matriz de Estados", style = GenesysTextStyle.Title)
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GenesysLoadingButton(text = "Normal", onClick = {})
                            GenesysLoadingButton(text = "Disabled", onClick = {}, enabled = false)
                            GenesysLoadingButton(text = "Loading", onClick = {}, isLoading = true)
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GenesysBadge(label = "5", color = MaterialTheme.colorScheme.primary)
                            GenesysBadge(label = "99+", color = MaterialTheme.colorScheme.error)
                            GenesysBadge(label = "NOVO", color = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                }
            }
        }
    }
}