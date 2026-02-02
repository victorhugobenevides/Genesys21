package com.itbenevides.genesys21.ui.components.input

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

/**
 * GenesysTextField ultra-estabilizado para WasmJs.
 * Resolve o bug de perda de caracteres (espaços) e reset de texto no Android Chrome.
 * Utiliza ancoragem de estado para proteger o buffer do teclado (IME).
 */
@Composable
fun GenesysTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String? = null,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    minLines: Int = 1,
    placeholder: String? = null,
    isError: Boolean = false,
    supportingText: String? = null,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    shape: Shape = RoundedCornerShape(16.dp),
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
    ),
    weightValue: Float = 0f
) {
    GenesysTextFieldBase(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        icon = icon,
        trailingIcon = trailingIcon,
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
        minLines = minLines,
        placeholder = placeholder,
        isError = isError,
        supportingText = supportingText,
        visualTransformation = visualTransformation,
        shape = shape,
        colors = colors,
        weightValue = weightValue
    )
}

@Composable
internal fun GenesysTextFieldBase(
    value: String,
    onValueChange: (String) -> Unit,
    label: String? = null,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    minLines: Int = 1,
    placeholder: String? = null,
    isError: Boolean = false,
    supportingText: String? = null,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    shape: Shape = RoundedCornerShape(16.dp),
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
    ),
    weightValue: Float = 0f
) {
    // 1. Estado local que blinda a digitação contra recomposições atrasadas
    var textFieldValueState by remember { 
        mutableStateOf(TextFieldValue(text = value, selection = TextRange(value.length))) 
    }

    // 2. Rastreia o último valor que o componente enviou para o pai
    var lastValueSent by remember { mutableStateOf(value) }

    // 3. Sincronização Externo -> Interno protegida:
    // Só aceita o valor da prop 'value' se ele for diferente do que o componente emitiu.
    if (value != lastValueSent && value != textFieldValueState.text) {
        textFieldValueState = textFieldValueState.copy(
            text = value,
            selection = TextRange(value.length)
        )
        lastValueSent = value
    }

    OutlinedTextField(
        value = textFieldValueState,
        onValueChange = { newValue ->
            // 4. Prioridade síncrona: protege o buffer do teclado no WasmJs/Android
            textFieldValueState = newValue
            
            // 5. Notifica o pai apenas se o texto mudou em relação à nossa âncora
            if (newValue.text != lastValueSent) {
                lastValueSent = newValue.text
                onValueChange(newValue.text)
            }
        },
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let { { Text(it) } },
        leadingIcon = icon?.let { { Icon(it, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary) } },
        trailingIcon = trailingIcon,
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = keyboardOptions,
        isError = isError,
        supportingText = supportingText?.let { { Text(it) } },
        visualTransformation = visualTransformation,
        colors = colors
    )
}

/**
 * Extensões de escopo padronizadas.
 */
@Composable
fun RowScope.GenesysTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String? = null,
    icon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    minLines: Int = 1,
    placeholder: String? = null,
    isError: Boolean = false,
    supportingText: String? = null,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    weightValue: Float = 0f,
    modifier: Modifier = Modifier
) {
    GenesysTextFieldBase(
        value = value,
        onValueChange = onValueChange,
        label = label,
        icon = icon,
        trailingIcon = trailingIcon,
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
        minLines = minLines,
        placeholder = placeholder,
        isError = isError,
        supportingText = supportingText,
        visualTransformation = visualTransformation,
        weightValue = weightValue,
        modifier = modifier.weight(if (weightValue > 0f) weightValue else 1f, fill = weightValue > 0f)
    )
}

@Composable
fun ColumnScope.GenesysTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String? = null,
    icon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    minLines: Int = 1,
    placeholder: String? = null,
    isError: Boolean = false,
    supportingText: String? = null,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    weightValue: Float = 0f,
    modifier: Modifier = Modifier
) {
    GenesysTextFieldBase(
        value = value,
        onValueChange = onValueChange,
        label = label,
        icon = icon,
        trailingIcon = trailingIcon,
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
        minLines = minLines,
        placeholder = placeholder,
        isError = isError,
        supportingText = supportingText,
        visualTransformation = visualTransformation,
        weightValue = weightValue,
        modifier = modifier
    )
}
