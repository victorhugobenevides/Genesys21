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
 * Testes de screenshot exclusivos para Desktop (Widescreen).
 */
class DesktopScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.NEXUS_10.copy(
            screenWidth = 1920,
            screenHeight = 1080,
            xdpi = 160,
            ydpi = 160,
            density = com.android.resources.Density.MEDIUM
        )
    )

    @Test
    fun testAdminDashboardDesktop() {
        paparazzi.snapshot(name = "desktop_admin_dashboard") {
            AppTheme(themeConfig = PageThemeConfig.OCEAN) {
                Surface(modifier = Modifier.fillMaxSize().padding(40.dp)) {
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
                            GenesysText("Dashboard Widescreen", style = GenesysTextStyle.Headline)
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                repeat(4) {
                                    GenesysStatsCard(
                                        label = "Métrica ${it + 1}", 
                                        value = "${it * 25}%", 
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier.fillMaxWidth().height(400.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                            ) {
                                Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                                    GenesysText("Visualização em 1920x1080")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
