package com.itbenevides.genesys21.screenshot.responsive

import app.cash.paparazzi.Paparazzi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.screenshot.base.DeviceConfigs
import org.junit.Rule
import org.junit.Test

/**
 * Testes de layouts adaptativos que mudam com base no tamanho da tela.
 */
class ResponsiveLayoutTest {

    @get:Rule
    val paparazziPhone = Paparazzi(deviceConfig = DeviceConfigs.STANDARD_PHONE)

    @get:Rule
    val paparazziTablet = Paparazzi(deviceConfig = DeviceConfigs.LARGE_TABLET)

    @Test
    fun testAdaptiveColumns() {
        val content: @Composable () -> Unit = {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val columns = if (maxWidth < 600.dp) 1 else 2
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Layout Adaptativo ($columns coluna${if (columns > 1) "s" else ""})",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    
                    repeat(4) { index ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            repeat(columns) { col ->
                                Card(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(120.dp)
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Card ${index * columns + col + 1}")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        paparazziPhone.snapshot("adaptive_columns_phone", content)
        paparazziTablet.snapshot("adaptive_columns_tablet", content)
    }

    @Test
    fun testAdaptiveNavigationRail() {
        val content: @Composable () -> Unit = {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val useRail = maxWidth >= 600.dp

                if (useRail) {
                    // Tablet: Navigation Rail
                    Row(modifier = Modifier.fillMaxSize()) {
                        NavigationRail {
                            NavigationRailItem(
                                selected = true,
                                onClick = {},
                                icon = { Icon(Icons.Default.Home, null) },
                                label = { Text("Início") }
                            )
                            NavigationRailItem(
                                selected = false,
                                onClick = {},
                                icon = { Icon(Icons.Default.Search, null) },
                                label = { Text("Buscar") }
                            )
                            NavigationRailItem(
                                selected = false,
                                onClick = {},
                                icon = { Icon(Icons.Default.Person, null) },
                                label = { Text("Perfil") }
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(16.dp)
                        ) {
                            Text("Conteúdo Principal")
                        }
                    }
                } else {
                    // Phone: Bottom Navigation
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text("Conteúdo Principal")
                        }
                        NavigationBar {
                            NavigationBarItem(
                                selected = true,
                                onClick = {},
                                icon = { Icon(Icons.Default.Home, null) },
                                label = { Text("Início") }
                            )
                            NavigationBarItem(
                                selected = false,
                                onClick = {},
                                icon = { Icon(Icons.Default.Search, null) },
                                label = { Text("Buscar") }
                            )
                            NavigationBarItem(
                                selected = false,
                                onClick = {},
                                icon = { Icon(Icons.Default.Person, null) },
                                label = { Text("Perfil") }
                            )
                        }
                    }
                }
            }
        }

        paparazziPhone.snapshot("adaptive_navigation_phone", content)
        paparazziTablet.snapshot("adaptive_navigation_tablet", content)
    }

    @Test
    fun testAdaptiveSidePanel() {
        val content: @Composable () -> Unit = {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val showSidePanel = maxWidth >= 840.dp

                Row(modifier = Modifier.fillMaxSize()) {
                    // Conteúdo principal
                    Column(
                        modifier = Modifier
                            .weight(if (showSidePanel) 0.7f else 1f)
                            .fillMaxHeight()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Conteúdo Principal",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        repeat(5) { index ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    "Item ${index + 1}",
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }

                    // Painel lateral (apenas em telas grandes)
                    if (showSidePanel) {
                        Surface(
                            modifier = Modifier
                                .weight(0.3f)
                                .fillMaxHeight(),
                            tonalElevation = 2.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "Informações Extras",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Divider()
                                Text("Detalhes adicionais visíveis apenas em tablets")
                            }
                        }
                    }
                }
            }
        }

        paparazziPhone.snapshot("adaptive_panel_phone", content)
        paparazziTablet.snapshot("adaptive_panel_tablet", content)
    }
}
