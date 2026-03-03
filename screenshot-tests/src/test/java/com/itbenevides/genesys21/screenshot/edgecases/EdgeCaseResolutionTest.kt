package com.itbenevides.genesys21.screenshot.edgecases

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test

/**
 * Testes para casos extremos: textos longos, listas vazias, overflow, etc.
 */
class EdgeCaseResolutionTest {

    @get:Rule
    val paparazzi = Paparazzi()

    @Test
    fun testLongTextOverflow() {
        paparazzi.snapshot("long_text_overflow") {
            Card(modifier = Modifier.width(300.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Título Extremamente Longo Que Pode Causar Problemas de Layout",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        "Este é um texto muito longo que testa como o componente lida com conteúdo extenso. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    @Test
    fun testEmptyStates() {
        paparazzi.snapshot("empty_list") {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Inbox,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Nenhum item encontrado",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "Tente ajustar seus filtros",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }

    @Test
    fun testVeryLargeList() {
        paparazzi.snapshot("large_list") {
            LazyColumn(
                modifier = Modifier.height(600.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(50) { index ->
                    ListItem(
                        headlineContent = { Text("Item ${index + 1}") },
                        supportingContent = { Text("Descrição do item") }
                    )
                }
            }
        }
    }

    @Test
    fun testSingleCharacterInputs() {
        paparazzi.snapshot("single_char_inputs") {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = "A",
                    onValueChange = {},
                    label = { Text("Nome") }
                )
                TextField(
                    value = "X",
                    onValueChange = {},
                    label = { Text("Sobrenome") }
                )
            }
        }
    }

    @Test
    fun testSpecialCharacters() {
        paparazzi.snapshot("special_characters") {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("ção áéíóú ÇÃO")
                Text("€ ¥ £ $ ₽")
                Text("♥ ♠ ♣ ♦")
                Text("😀 😍 🥳")
                Text("中文 日本語 한국어")
            }
        }
    }

    @Test
    fun testZeroValues() {
        paparazzi.snapshot("zero_values") {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Preço: R$ 0,00")
                Text("Quantidade: 0")
                LinearProgressIndicator(
                    progress = { 0f },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("0% concluído")
            }
        }
    }

    @Test
    fun testMaxValues() {
        paparazzi.snapshot("max_values") {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Preço: R$ 999.999,99")
                Text("Quantidade: 999+")
                LinearProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("100% concluído")
                Badge { Text("999+") }
            }
        }
    }
}
