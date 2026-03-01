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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * GenesysTextField - Versão de Resistência Industrial (Samsung Bug Fix).
 * Resolve o problema do WasmJs onde o texto some ao apertar espaço usando
 * um buffer de integridade e bloqueio de reset súbito.
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
    colors: TextFieldColors = adaptiveTextFieldColors(),
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

/**
 * Cores adaptativas para o TextField que melhoram contraste em temas escuros.
 */
@Composable
private fun adaptiveTextFieldColors(): TextFieldColors {
    val isDarkTheme = isDarkTheme()
    
    return OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = if (isDarkTheme) {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
        } else {
            MaterialTheme.colorScheme.outline
        },
        unfocusedContainerColor = if (isDarkTheme) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
        },
        focusedContainerColor = if (isDarkTheme) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
        },
        // Placeholder mais visível em temas escuros
        disabledPlaceholderColor = if (isDarkTheme) {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        },
        // Cursor sempre visível
        cursorColor = MaterialTheme.colorScheme.primary,
        // Texto desabilitado mais visível
        disabledTextColor = if (isDarkTheme) {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        }
    )
}

// Helper para detectar tema escuro
@Composable
private fun isDarkTheme(): Boolean {
    val bg = MaterialTheme.colorScheme.background
    val luminance = 0.2126f * bg.red + 0.7152f * bg.green + 0.0722f * bg.blue
    return luminance < 0.5f
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
    visualTransformation: VisualTransformation = VisualTransformation.None,
    shape: Shape = RoundedCornerShape(16.dp),
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
    ),
    weightValue: Float = 0f
) {
    var localValue by remember { mutableStateOf(TextFieldValue(text = value)) }
    var isFocused by remember { mutableStateOf(false) }
    var lastSafeText by remember { mutableStateOf(value) }

    LaunchedEffect(value) {
        if (!isFocused && value != localValue.text) {
            localValue = localValue.copy(text = value)
            lastSafeText = value
        }
    }

    OutlinedTextField(
        value = localValue,
        onValueChange = { next ->
            if (isFocused && next.text.isEmpty() && lastSafeText.isNotEmpty()) {
                return@OutlinedTextField
            }
            localValue = next
            lastSafeText = next.text
            if (next.text != value) {
                onValueChange(next.text)
            }
        },
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let { { Text(it) } },
        leadingIcon = icon?.let { { Icon(it, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary) } },
        trailingIcon = trailingIcon,
        modifier = modifier
            .fillMaxWidth()
            // CORREÇÃO: Adicionando testTag baseada no label para facilitar testes automatizados
            .then(if (label != null) Modifier.testTag(label) else Modifier)
            .onFocusChanged { 
                isFocused = it.isFocused 
                if (!it.isFocused) {
                    localValue = localValue.copy(text = value)
                }
            },
        shape = shape,
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = keyboardOptions.copy(
            autoCorrectEnabled = false,
            keyboardType = if (keyboardOptions.keyboardType == KeyboardType.Text) KeyboardType.Password else keyboardOptions.keyboardType,
            capitalization = KeyboardCapitalization.None,
            imeAction = if (singleLine) ImeAction.Done else ImeAction.Default
        ),
        visualTransformation = visualTransformation,
        isError = isError,
        supportingText = supportingText?.let { { Text(it) } },
        colors = colors
    )
}

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
    visualTransformation: VisualTransformation = VisualTransformation.None,
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
