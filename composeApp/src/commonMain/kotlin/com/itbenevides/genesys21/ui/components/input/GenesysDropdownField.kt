package com.itbenevides.genesys21.ui.components.input

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Componente Dropdown do Design System otimizado para WasmJs.
 * Garante que a seleção de itens feche o menu e propague o valor corretamente.
 */
@Composable
fun GenesysDropdownField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    options: List<String>,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    placeholder: String? = null
) {
    var expanded by remember { mutableStateOf(false) }

    val filteredOptions = remember(value, options) {
        if (value.isEmpty()) options 
        else options.filter { it.contains(value, ignoreCase = true) }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        GenesysTextField(
            value = value,
            onValueChange = { 
                onValueChange(it)
                // Abre o menu apenas se houver algo digitado para sugerir
                if (it.isNotEmpty()) expanded = true
            },
            label = label,
            placeholder = placeholder,
            icon = icon,
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = "Ver opções"
                    )
                }
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            if (filteredOptions.isEmpty() && value.isNotEmpty()) {
                DropdownMenuItem(
                    text = { Text("Nenhuma categoria encontrada") },
                    onClick = { expanded = false },
                    enabled = false
                )
            } else {
                filteredOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = option) },
                        onClick = {
                            // IMPORTANTE: Notifica o pai e fecha o menu
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
