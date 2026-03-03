package com.itbenevides.genesys21.screenshot.responsive

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.screenshot.base.DeviceConfigs
import com.itbenevides.genesys21.screenshot.base.PaparazziTestBase
import org.junit.Rule
import org.junit.Test

/**
 * Testes completos de componentes em múltiplas resoluções.
 */
class MultiResolutionScreenshotTest : PaparazziTestBase() {

    @get:Rule
    val paparazziSmallPhone = Paparazzi(deviceConfig = DeviceConfigs.SMALL_PHONE)

    @get:Rule
    val paparazziLargePhone = Paparazzi(deviceConfig = DeviceConfigs.LARGE_PHONE)

    @get:Rule
    val paparazziTablet = Paparazzi(deviceConfig = DeviceConfigs.LARGE_TABLET)

    @Test
    fun testProductListAcrossDevices() {
        val content: @Composable () -> Unit = {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(5) { index ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(80.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {}
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    "Produto ${index + 1}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "Descrição breve do produto",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    "R$ ${(index + 1) * 99},90",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        paparazziSmallPhone.snapshot("product_list_small_phone", content)
        paparazziLargePhone.snapshot("product_list_large_phone", content)
        paparazziTablet.snapshot("product_list_tablet", content)
    }

    @Test
    fun testProductGridAcrossDevices() {
        val content: @Composable () -> Unit = {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(12) { index ->
                    Card {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {}
                            Text(
                                "Item ${index + 1}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "R$ ${(index + 1) * 50}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        paparazziSmallPhone.snapshot("product_grid_small_phone", content)
        paparazziLargePhone.snapshot("product_grid_large_phone", content)
        paparazziTablet.snapshot("product_grid_tablet", content)
    }

    @Test
    fun testNavigationAcrossDevices() {
        val content: @Composable () -> Unit = {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = { Text("Minha Loja") },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Search, contentDescription = null)
                        }
                        IconButton(onClick = {}) {
                            Badge { Text("3") }
                            Icon(Icons.Default.ShoppingCart, contentDescription = null)
                        }
                    }
                )
                Divider()
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Text(
                        "Conteúdo da página",
                        modifier = Modifier.padding(16.dp)
                    )
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
                        icon = { Icon(Icons.Default.Category, null) },
                        label = { Text("Categorias") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = {},
                        icon = { Icon(Icons.Default.Favorite, null) },
                        label = { Text("Favoritos") }
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

        paparazziSmallPhone.snapshot("navigation_small_phone", content)
        paparazziLargePhone.snapshot("navigation_large_phone", content)
        paparazziTablet.snapshot("navigation_tablet", content)
    }

    @Test
    fun testFormLayoutAcrossDevices() {
        val content: @Composable () -> Unit = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Cadastro de Usuário",
                    style = MaterialTheme.typography.headlineMedium
                )
                TextField(
                    value = "",
                    onValueChange = {},
                    label = { Text("Nome Completo") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = "",
                    onValueChange = {},
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = "",
                    onValueChange = {},
                    label = { Text("Telefone") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(checked = false, onCheckedChange = {})
                    Text(
                        "Aceito os termos e condições",
                        modifier = Modifier.weight(1f)
                    )
                }
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cadastrar")
                }
            }
        }

        paparazziSmallPhone.snapshot("form_small_phone", content)
        paparazziLargePhone.snapshot("form_large_phone", content)
        paparazziTablet.snapshot("form_tablet", content)
    }

    @Test
    fun testDetailScreenAcrossDevices() {
        val content: @Composable () -> Unit = {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {}
                }
                item {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Produto Premium",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            "R$ 599,90",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Divider()
                        Text(
                            "Descrição",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Divider()
                        Text(
                            "Especificações",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text("• Característica 1")
                        Text("• Característica 2")
                        Text("• Característica 3")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {},
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Adicionar ao Carrinho")
                        }
                    }
                }
            }
        }

        paparazziSmallPhone.snapshot("detail_small_phone", content)
        paparazziLargePhone.snapshot("detail_large_phone", content)
        paparazziTablet.snapshot("detail_tablet", content)
    }
}
