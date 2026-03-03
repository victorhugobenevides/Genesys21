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
 * Testes de screenshot exclusivos para Tablet.
 */
class TabletScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.NEXUS_7)

    @Test
    fun testTabletLayout() {
        paparazzi.snapshot(name = "tablet_layout") {
            AppTheme(themeConfig = PageThemeConfig.ROYAL) {
                Surface(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        GenesysText("Layout Tablet (NEXUS 7)", style = GenesysTextStyle.Title)
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
}
