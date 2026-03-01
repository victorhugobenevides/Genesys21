package com.itbenevides.genesys21.screenshot

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.PageThemeConfig
import com.itbenevides.genesys21.ui.components.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.card.GenesysStatsCard
import com.itbenevides.genesys21.ui.components.text.GenesysText
import com.itbenevides.genesys21.ui.components.text.GenesysTextStyle
import com.itbenevides.genesys21.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test

/**
 * Testes de screenshot em diferentes dispositivos e configurações.
 * Testa responsividade e adaptação de layout.
 */
class ResponsiveScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi()

    @Test
    fun testPhoneSize() {
        paparazzi.snapshot(name = "responsive_phone") {
            AppTheme(themeConfig = PageThemeConfig.ROYAL) {
                Surface(modifier = Modifier.width(360.dp).padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GenesysText("Layout Phone (360dp)", style = GenesysTextStyle.Title)
                        GenesysLoadingButton(text = "Ação Principal", onClick = {}, fillWidth = true)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GenesysStatsCard(label = "Vendas", value = "1.2k", color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                            GenesysStatsCard(label = "Pedidos", value = "45", color = MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testTabletSize() {
        paparazzi.snapshot(name = "responsive_tablet") {
            AppTheme(themeConfig = PageThemeConfig.ROYAL) {
                Surface(modifier = Modifier.width(600.dp).padding(24.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        GenesysText("Layout Tablet (600dp)", style = GenesysTextStyle.Title)
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            GenesysStatsCard(label = "Vendas", value = "1.2k", color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                            GenesysStatsCard(label = "Pedidos", value = "45", color = MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1f))
                            GenesysStatsCard(label = "Clientes", value = "89", color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            GenesysLoadingButton(text = "Ação 1", onClick = {}, modifier = Modifier.weight(1f))
                            GenesysLoadingButton(text = "Ação 2", onClick = {}, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testSmallPhone() {
        paparazzi.snapshot(name = "responsive_small_phone") {
            AppTheme(themeConfig = PageThemeConfig.ROYAL) {
                Surface(modifier = Modifier.width(320.dp).padding(12.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        GenesysText("Small Phone (320dp)", style = GenesysTextStyle.Title)
                        GenesysLoadingButton(text = "Ação", onClick = {}, fillWidth = true)
                        GenesysStatsCard(label = "Total", value = "R$ 99", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }

    @Test
    fun testAllThemeVariations() {
        val themes = listOf(
            PageThemeConfig.ROYAL,
            PageThemeConfig.OCEAN,
            PageThemeConfig.FOREST,
            PageThemeConfig.SUNSET,
            PageThemeConfig.BERRY,
            PageThemeConfig.MINIMAL,
            PageThemeConfig.DARK_MODE
        )
        
        themes.forEach { theme ->
            paparazzi.snapshot(name = "theme_grid_${theme.name.lowercase()}") {
                AppTheme(themeConfig = theme) {
                    Surface(modifier = Modifier.padding(16.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            GenesysText(theme.name, style = GenesysTextStyle.Title)
                            GenesysLoadingButton(text = "Primário", onClick = {})
                            GenesysStatsCard(label = "Teste", value = "123", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testContentDensity() {
        paparazzi.snapshot(name = "responsive_density_compact") {
            AppTheme(themeConfig = PageThemeConfig.ROYAL) {
                Surface(modifier = Modifier.padding(8.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        GenesysText("Layout Compacto", style = GenesysTextStyle.Title)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            GenesysLoadingButton(text = "A", onClick = {})
                            GenesysLoadingButton(text = "B", onClick = {})
                            GenesysLoadingButton(text = "C", onClick = {})
                        }
                    }
                }
            }
        }
        
        paparazzi.snapshot(name = "responsive_density_comfortable") {
            AppTheme(themeConfig = PageThemeConfig.ROYAL) {
                Surface(modifier = Modifier.padding(24.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        GenesysText("Layout Confortável", style = GenesysTextStyle.Title)
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            GenesysLoadingButton(text = "Ação 1", onClick = {})
                            GenesysLoadingButton(text = "Ação 2", onClick = {})
                        }
                    }
                }
            }
        }
    }
}