package com.itbenevides.genesys21.ui.components.input

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Componente Dropdown do Design System com busca/filtro.
 * Otimizado para WasmJs: A abertura do menu é controlada para não roubar o foco durante a digitação.
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

    // Filtra as opções baseada no texto atual para facilitar a escolha
    val filteredOptions = remember(value, options) {
        if (value.isEmpty()) options 
        else options.filter { it.contains(value, ignoreCase = true) }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        GenesysTextField(
            value = value,
            onValueChange = { 
                onValueChange(it)
                // Não abrimos o menu automaticamente aqui para evitar o bug de foco do WasmJs
            },
            label = label,
            placeholder = placeholder,
            icon = icon,
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = "Ver sugestões"
                    )
                }
            }
        )

        // O menu só aparece se houver opções filtradas e o usuário clicar na seta
        if (filteredOptions.isNotEmpty()) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.8f),
                // focusable = false ajuda a manter o foco no TextField em algumas versões do Compose
            ) {
                filteredOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
