package com.itbenevides.genesys21.screenshot.states

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.screenshot.base.EnhancedPaparazziTestBase
import org.junit.Test

/**
 * Testes de estados de componentes interativos.
 */
class ComponentStatesScreenshotTest : EnhancedPaparazziTestBase() {

    @Test
    fun testButtonStates() {
        snapshotStates(
            baseName = "button",
            states = mapOf(
                "enabled" to {
                    Button(onClick = {}) {
                        Text("Enabled Button")
                    }
                },
                "disabled" to {
                    Button(
                        onClick = {},
                        enabled = false
                    ) {
                        Text("Disabled Button")
                    }
                },
                "loading" to {
                    Button(
                        onClick = {},
                        enabled = false
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text("Loading...")
                        }
                    }
                }
            )
        )
    }

    @Test
    fun testTextFieldStates() {
        snapshotStates(
            baseName = "textfield",
            states = mapOf(
                "empty" to {
                    TextField(
                        value = "",
                        onValueChange = {},
                        label = { Text("Email") },
                        placeholder = { Text("exemplo@email.com") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                "filled" to {
                    TextField(
                        value = "usuario@email.com",
                        onValueChange = {},
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                "error" to {
                    TextField(
                        value = "email-invalido",
                        onValueChange = {},
                        label = { Text("Email") },
                        isError = true,
                        supportingText = { Text("Email inválido") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                "disabled" to {
                    TextField(
                        value = "campo@bloqueado.com",
                        onValueChange = {},
                        label = { Text("Email") },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            )
        )
    }

    @Test
    fun testCheckboxStates() {
        snapshotStates(
            baseName = "checkbox",
            states = mapOf(
                "unchecked" to {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Checkbox(checked = false, onCheckedChange = {})
                        Text("Aceito os termos")
                    }
                },
                "checked" to {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Checkbox(checked = true, onCheckedChange = {})
                        Text("Aceito os termos")
                    }
                },
                "disabled_unchecked" to {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Checkbox(
                            checked = false,
                            onCheckedChange = {},
                            enabled = false
                        )
                        Text("Aceito os termos")
                    }
                },
                "disabled_checked" to {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Checkbox(
                            checked = true,
                            onCheckedChange = {},
                            enabled = false
                        )
                        Text("Aceito os termos")
                    }
                }
            )
        )
    }

    @Test
    fun testSwitchStates() {
        snapshotStates(
            baseName = "switch",
            states = mapOf(
                "off" to {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Notificações")
                        Spacer(Modifier.weight(1f))
                        Switch(checked = false, onCheckedChange = {})
                    }
                },
                "on" to {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Notificações")
                        Spacer(Modifier.weight(1f))
                        Switch(checked = true, onCheckedChange = {})
                    }
                },
                "disabled_off" to {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Notificações")
                        Spacer(Modifier.weight(1f))
                        Switch(
                            checked = false,
                            onCheckedChange = {},
                            enabled = false
                        )
                    }
                },
                "disabled_on" to {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Notificações")
                        Spacer(Modifier.weight(1f))
                        Switch(
                            checked = true,
                            onCheckedChange = {},
                            enabled = false
                        )
                    }
                }
            )
        )
    }

    @Test
    fun testCardStates() {
        snapshotStates(
            baseName = "card",
            states = mapOf(
                "default" to {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Card Padrão",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text("Conteúdo do card")
                        }
                    }
                },
                "outlined" to {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Card Outlined",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text("Conteúdo do card")
                        }
                    }
                },
                "elevated" to {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Card Elevado",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text("Conteúdo do card")
                        }
                    }
                }
            )
        )
    }

    @Test
    fun testChipStates() {
        snapshotStates(
            baseName = "chip",
            states = mapOf(
                "unselected" to {
                    FilterChip(
                        selected = false,
                        onClick = {},
                        label = { Text("Categoria") }
                    )
                },
                "selected" to {
                    FilterChip(
                        selected = true,
                        onClick = {},
                        label = { Text("Categoria") }
                    }
                },
                "disabled_unselected" to {
                    FilterChip(
                        selected = false,
                        onClick = {},
                        label = { Text("Categoria") },
                        enabled = false
                    )
                },
                "disabled_selected" to {
                    FilterChip(
                        selected = true,
                        onClick = {},
                        label = { Text("Categoria") },
                        enabled = false
                    )
                }
            )
        )
    }

    @Test
    fun testProgressIndicatorStates() {
        snapshotStates(
            baseName = "progress",
            states = mapOf(
                "indeterminate" to {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        CircularProgressIndicator()
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                },
                "determinate_50" to {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        CircularProgressIndicator(progress = { 0.5f })
                        LinearProgressIndicator(
                            progress = { 0.5f },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("50% concluído")
                    }
                },
                "determinate_100" to {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        CircularProgressIndicator(progress = { 1f })
                        LinearProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("Concluído!")
                    }
                }
            )
        )
    }
}
