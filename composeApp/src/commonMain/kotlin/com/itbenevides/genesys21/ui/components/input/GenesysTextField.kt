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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * GenesysTextField ultra-estabilizado para WasmJs.
 * Utiliza TextFieldValue para manter a composição do teclado nativo (IME) intacta.
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
    visualTransformation: VisualTransformation = VisualTransformation.None,
    shape: Shape = RoundedCornerShape(16.dp),
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
    ),
    weightValue: Float = 0f // Mantido para compatibilidade
) {
    GenesysTextFieldContent(
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
        colors = colors
    )
}

/**
 * Implementação interna para evitar recursão infinita.
 */
@Composable
internal fun GenesysTextFieldContent(
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
    visualTransformation: VisualTransformation = VisualTransformation.None,
    shape: Shape = RoundedCornerShape(16.dp),
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
    )
) {
    var textFieldValueState by remember { 
        mutableStateOf(TextFieldValue(text = value, selection = TextRange(value.length))) 
    }

    LaunchedEffect(value) {
        if (value != textFieldValueState.text) {
            textFieldValueState = textFieldValueState.copy(text = value)
        }
    }

    OutlinedTextField(
        value = textFieldValueState,
        onValueChange = { newValue ->
            textFieldValueState = newValue
            if (value != newValue.text) {
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
 * Extensão para RowScope que aplica o peso proporcional.
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
    visualTransformation: VisualTransformation = VisualTransformation.None,
    weightValue: Float = 0f,
    modifier: Modifier = Modifier
) {
    val finalModifier = if (weightValue > 0f) modifier.weight(weightValue) else modifier
    GenesysTextFieldContent(
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
        modifier = finalModifier
    )
}

/**
 * Extensão para ColumnScope que aplica o peso proporcional.
 */
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
    visualTransformation: VisualTransformation = VisualTransformation.None,
    weightValue: Float = 0f,
    modifier: Modifier = Modifier
) {
    val finalModifier = if (weightValue > 0f) modifier.weight(weightValue) else modifier
    GenesysTextFieldContent(
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
        modifier = finalModifier
    )
}
