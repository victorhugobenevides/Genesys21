package com.itbenevides.genesys21.screenshot.responsive

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
 * Testes de screenshot responsivos.
 * Usamos instâncias separadas para evitar IllegalStateException na troca de configuração.
 */
class ResponsiveScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi()

    @Test
    fun testPhoneSize() {
        // Usando a configuração padrão da regra (Pixel 5)
        paparazzi.snapshot(name = "responsive_phone") {
            AppTheme(themeConfig = PageThemeConfig.ROYAL) {
                Surface(modifier = Modifier.fillMaxWidth().height(800.dp).padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GenesysText("Layout Phone (Portrait)", style = GenesysTextStyle.Title)
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
    fun testSmallPhone() {
        // Forçamos um tamanho fixo no Modifier para simular o device menor sem crashar a regra
        paparazzi.snapshot(name = "responsive_small_phone") {
            AppTheme(themeConfig = PageThemeConfig.ROYAL) {
                Surface(modifier = Modifier.width(320.dp).height(568.dp).padding(12.dp)) {
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
    fun testTabletSize() {
        // Simulando Tablet via Modifier em vez de mudar a regra global
        paparazzi.snapshot(name = "responsive_tablet") {
            AppTheme(themeConfig = PageThemeConfig.ROYAL) {
                Surface(modifier = Modifier.width(800.dp).height(1280.dp).padding(24.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        GenesysText("Layout Tablet (Simulado)", style = GenesysTextStyle.Title)
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
    fun testDesktopSize() {
        // Simulando Widescreen via Modifier
        paparazzi.snapshot(name = "responsive_desktop") {
            AppTheme(themeConfig = PageThemeConfig.OCEAN) {
                Surface(modifier = Modifier.width(1280.dp).height(800.dp).padding(40.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        // Sidebar
                        Column(modifier = Modifier.width(250.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            GenesysText("Menu Admin", style = GenesysTextStyle.Title)
                            repeat(4) {
                                GenesysLoadingButton(text = "Opção ${it + 1}", onClick = {}, fillWidth = true, containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            }
                        }

                        // Conteúdo Principal
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                            GenesysText("Dashboard Desktop", style = GenesysTextStyle.Headline)
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                repeat(3) {
                                    GenesysStatsCard(
                                        label = "Métrica ${it + 1}", 
                                        value = "${it * 25}%", 
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier.fillMaxWidth().height(300.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                            ) {
                                Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                                    GenesysText("Conteúdo Centralizado")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
