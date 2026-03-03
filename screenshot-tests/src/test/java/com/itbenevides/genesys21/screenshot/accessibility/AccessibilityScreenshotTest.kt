package com.itbenevides.genesys21.screenshot.accessibility

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.screenshot.base.EnhancedPaparazziTestBase
import org.junit.Test

/**
 * Testes de acessibilidade focados em font scaling e contraste.
 */
class AccessibilityScreenshotTest : EnhancedPaparazziTestBase() {

    @Test
    fun testButtonWithFontScaling() {
        snapshotAllFontScales("button_accessibility") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {}) {
                    Text("Adicionar ao Carrinho")
                }
                OutlinedButton(onClick = {}) {
                    Text("Ver Detalhes")
                }
                TextButton(onClick = {}) {
                    Text("Cancelar")
                }
            }
        }
    }

    @Test
    fun testTextFieldWithFontScaling() {
        snapshotAllFontScales("textfield_accessibility") {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                TextField(
                    value = "exemplo@email.com",
                    onValueChange = {},
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = "",
                    onValueChange = {},
                    label = { Text("Nome Completo") },
                    placeholder = { Text("Digite seu nome") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    @Test
    fun testCardWithFontScaling() {
        snapshotAllFontScales("card_accessibility") {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Produto Premium",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Descrição detalhada do produto com informações importantes",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "R$ 299,90",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    @Test
    fun testListItemWithFontScaling() {
        snapshotAllFontScales("listitem_accessibility") {
            Column {
                repeat(3) { index ->
                    ListItem(
                        headlineContent = { Text("Item ${index + 1}") },
                        supportingContent = { Text("Descrição do item") },
                        trailingContent = { Text("R$ ${(index + 1) * 50},00") }
                    )
                    if (index < 2) Divider()
                }
            }
        }
    }

    @Test
    fun testNavigationWithFontScaling() {
        snapshotAllFontScales("navigation_accessibility") {
            Column {
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

    @Test
    fun testDialogWithFontScaling() {
        snapshotAllFontScales("dialog_accessibility") {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Confirmar Ação") },
                text = { Text("Tem certeza que deseja continuar com esta operação? Esta ação não pode ser desfeita.") },
                confirmButton = {
                    TextButton(onClick = {}) {
                        Text("Confirmar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {}) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }

    @Test
    fun testChipsWithFontScaling() {
        snapshotAllFontScaling("chips_accessibility") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = true,
                    onClick = {},
                    label = { Text("Eletrônicos") }
                )
                FilterChip(
                    selected = false,
                    onClick = {},
                    label = { Text("Livros") }
                )
                FilterChip(
                    selected = false,
                    onClick = {},
                    label = { Text("Vestuário") }
                )
            }
        }
    }
}
