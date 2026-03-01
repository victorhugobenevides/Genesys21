package com.itbenevides.genesys21.ui.components.input

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * GenesysSearchBar estabilizada para WasmJs com debounce.
 * 
 * @param debounceMillis Tempo de debounce em milissegundos (padrão: 300ms)
 */
@Composable
fun GenesysSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    onClear: () -> Unit = { onValueChange("") },
    debounceMillis: Long = 300L,
    onSearch: ((String) -> Unit)? = null
) {
    // Estado interno para o valor do campo
    var internalValue by remember { mutableStateOf(value) }
    
    // Efeito para sincronizar com o valor externo
    LaunchedEffect(value) {
        if (value != internalValue) {
            internalValue = value
        }
    }
    
    // Debounce para notificar mudanças
    LaunchedEffect(internalValue) {
        if (debounceMillis > 0 && onSearch != null) {
            delay(debounceMillis)
            if (internalValue == value) {
                onSearch(internalValue)
            }
        }
    }
    
    GenesysTextField(
        value = internalValue,
        onValueChange = { 
            internalValue = it
            onValueChange(it)
        },
        placeholder = placeholder,
        icon = Icons.Default.Search,
        trailingIcon = {
            if (internalValue.isNotEmpty()) {
                IconButton(onClick = {
                    internalValue = ""
                    onClear()
                }) {
                    Icon(
                        imageVector = Icons.Default.Close, 
                        contentDescription = "Limpar busca"
                    )
                }
            }
        },
        modifier = modifier.heightIn(min = 56.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search
        )
    )
}
