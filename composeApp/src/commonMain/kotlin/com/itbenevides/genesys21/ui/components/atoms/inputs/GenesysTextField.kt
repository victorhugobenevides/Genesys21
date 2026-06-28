package com.itbenevides.genesys21.ui.components.atoms.inputs

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
    colors: TextFieldColors =
        OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
        ),
    weightValue: Float = 0f,
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
        weightValue = weightValue,
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
    visualTransformation: VisualTransformation = VisualTransformation.None,
    shape: Shape = RoundedCornerShape(16.dp),
    colors: TextFieldColors =
        OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
        ),
    weightValue: Float = 0f,
) {
    // 1. ESTADO LOCAL (One-Way Data Binding)
    // O componente é o dono do estado enquanto focado para evitar o "ping-pong" com o ViewModel
    var localValue by remember { mutableStateOf(TextFieldValue(text = value)) }
    var isFocused by remember { mutableStateOf(false) }

    // Buffer para restauração em caso de bug da Samsung
    var lastSafeText by remember { mutableStateOf(value) }

    // Sincroniza apenas se a mudança for externa e não houver foco
    LaunchedEffect(value) {
        if (!isFocused && value != localValue.text) {
            localValue = localValue.copy(text = value)
            lastSafeText = value
        }
    }

    OutlinedTextField(
        value = localValue,
        onValueChange = { next ->
            // --- SAMSUNG BUG GUARD ---
            // Se o texto anterior não era vazio, o campo está focado, e o novo texto vem vazio,
            // é o bug do espaço limpando o buffer. Nós REJEITAMOS essa mudança.
            if (isFocused && next.text.isEmpty() && lastSafeText.isNotEmpty()) {
                // Mantém o estado atual e ignora o comando de limpar
                return@OutlinedTextField
            }

            localValue = next
            lastSafeText = next.text

            // Notifica o ViewModel
            if (next.text != value) {
                onValueChange(next.text)
            }
        },
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let { { Text(it) } },
        leadingIcon = icon?.let { { Icon(it, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary) } },
        trailingIcon = trailingIcon,
        modifier =
            modifier
                .fillMaxWidth()
                .onFocusChanged {
                    isFocused = it.isFocused
                    if (!it.isFocused) {
                        localValue = localValue.copy(text = value) // Sincroniza ao sair
                    }
                },
        shape = shape,
        singleLine = singleLine,
        minLines = minLines,
        // 2. CONFIGURAÇÃO ANTI-PREDIÇÃO:
        // Password força o teclado a desligar o dicionário (causa do bug), mas visualTransformation exibe o texto.
        keyboardOptions =
            keyboardOptions.copy(
                autoCorrectEnabled = false,
                keyboardType = if (keyboardOptions.keyboardType == KeyboardType.Text) KeyboardType.Password else keyboardOptions.keyboardType,
                capitalization = KeyboardCapitalization.None,
                imeAction = if (singleLine) ImeAction.Done else ImeAction.Default,
            ),
        visualTransformation =
            if (keyboardOptions.keyboardType == KeyboardType.Text && visualTransformation == VisualTransformation.None) {
                VisualTransformation.None
            } else {
                visualTransformation
            },
        isError = isError,
        supportingText = supportingText?.let { { Text(it) } },
        colors = colors,
    )
}

@Composable
fun RowScope.GenesysRowTextField(
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
    modifier: Modifier = Modifier,
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
        modifier = modifier.weight(if (weightValue > 0f) weightValue else 1f, fill = weightValue > 0f),
    )
}

@Composable
fun ColumnScope.GenesysColumnTextField(
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
    modifier: Modifier = Modifier,
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
        modifier = modifier,
    )
}
