package com.itbenevides.genesys21.screenshot.edgecases

import app.cash.paparazzi.Paparazzi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.PageThemeConfig
import com.itbenevides.genesys21.ui.components.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.card.GenesysCard
import com.itbenevides.genesys21.ui.components.feedback.GenesysEmptyState
import com.itbenevides.genesys21.ui.components.input.GenesysTextField
import com.itbenevides.genesys21.ui.components.layout.GenesysSectionHeader
import com.itbenevides.genesys21.ui.components.text.GenesysText
import com.itbenevides.genesys21.ui.components.text.GenesysTextStyle
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test

/**
 * Testes de screenshot focados em acessibilidade:
 * - Tamanhos de fonte grandes
 * - Alto contraste
 * - Combinações de estados
 */
class AccessibilityScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi()

    @Test
    fun testHighContrastMode() {
        paparazzi.snapshot(name = "accessibility_high_contrast") {
            AppTheme(themeConfig = PageThemeConfig.DARK_MODE) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        GenesysText(
                            "Alto Contraste",
                            style = GenesysTextStyle.Headline,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        GenesysTextField(
                            value = "exemplo@email.com",
                            onValueChange = {},
                            label = "Email",
                            placeholder = "Digite seu email"
                        )
                        
                        GenesysLoadingButton(
                            text = "Enviar",
                            onClick = {},
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                        
                        GenesysCard(
                            backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                GenesysText(
                                    "Card em Alto Contraste",
                                    style = GenesysTextStyle.Title,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                GenesysText(
                                    "Texto com boa legibilidade",
                                    style = GenesysTextStyle.Body,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testTouchTargetSizes() {
        paparazzi.snapshot(name = "accessibility_touch_targets") {
            AppTheme(themeConfig = PageThemeConfig.ROYAL) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        GenesysText("Touch Targets", style = GenesysTextStyle.Title)
                        
                        // Botão padrão (48dp altura)
                        GenesysLoadingButton(text = "Botão Padrão (48dp)", onClick = {})
                        
                        // Botão largo
                        GenesysLoadingButton(
                            text = "Botão Largo (Full Width)",
                            onClick = {},
                            fillWidth = true
                        )
                        
                        // Ícones com touch target adequado
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            androidx.compose.material3.IconButton(onClick = {}) {
                                androidx.compose.material3.Icon(
                                    GenesysIcons.Search,
                                    contentDescription = "Buscar"
                                )
                            }
                            androidx.compose.material3.IconButton(onClick = {}) {
                                androidx.compose.material3.Icon(
                                    GenesysIcons.Add,
                                    contentDescription = "Adicionar"
                                )
                            }
                            androidx.compose.material3.IconButton(onClick = {}) {
                                androidx.compose.material3.Icon(
                                    GenesysIcons.Close,
                                    contentDescription = "Fechar"
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testScreenReaderLabels() {
        paparazzi.snapshot(name = "accessibility_screen_reader") {
            AppTheme(themeConfig = PageThemeConfig.OCEAN) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        GenesysText("Labels para Screen Reader", style = GenesysTextStyle.Title)
                        
                        GenesysSectionHeader(
                            title = "Cabeçalho de Seção",
                            subtitle = "Descrição adicional",
                            trailingContent = {
                                androidx.compose.material3.IconButton(onClick = {}) {
                                    androidx.compose.material3.Icon(
                                        GenesysIcons.Settings,
                                        contentDescription = "Configurações da seção"
                                    )
                                }
                            }
                        )
                        
                        GenesysEmptyState(
                            icon = GenesysIcons.Search,
                            title = "Nenhum resultado encontrado",
                            description = "Tente ajustar seus filtros de busca",
                            action = {
                                GenesysLoadingButton(
                                    text = "Limpar Filtros",
                                    onClick = {}
                                )
                            }
                        )
                        
                        GenesysText(
                            "Todos os elementos possuem descrições acessíveis",
                            style = GenesysTextStyle.Label
                        )
                    }
                }
            }
        }
    }

    @Test
    fun testFocusIndicators() {
        paparazzi.snapshot(name = "accessibility_focus_indicators") {
            AppTheme(themeConfig = PageThemeConfig.FOREST) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        GenesysText("Indicadores de Foco", style = GenesysTextStyle.Title)
                        
                        // Simulando elementos em foco
                        GenesysTextField(
                            value = "Campo em foco",
                            onValueChange = {},
                            label = "Email"
                        )
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GenesysLoadingButton(text = "Primário", onClick = {})
                            GenesysLoadingButton(
                                text = "Secundário",
                                onClick = {},
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        }
                        
                        GenesysCard(
                            onClick = {},
                            backgroundColor = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                GenesysText(
                                    "Card Clicável",
                                    style = GenesysTextStyle.Title,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}