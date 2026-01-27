package com.itbenevides.genesys21.ui.components.input

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.ui.components.text.GenesysText
import com.itbenevides.genesys21.ui.components.text.GenesysTextStyle

/**
 * GenesysTextField ultra-estabilizado para WasmJs.
 * Resolve o bug de perda de caracteres (espaços) e reset de texto no Android.
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
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    weightValue: Float = 0f
) {
    // 1. Estado local de TextFieldValue: Garante fluidez e conexão com o buffer do teclado.
    var textFieldValueState by remember { 
        mutableStateOf(TextFieldValue(text = value, selection = TextRange(value.length))) 
    }

    // 2. Sincronização Externo -> Interno:
    // Só atualizamos se o valor externo mudar (reset ou carga), ignorando o delay da digitação.
    LaunchedEffect(value) {
        if (value != textFieldValueState.text) {
            textFieldValueState = textFieldValueState.copy(
                text = value,
                selection = TextRange(value.length)
            )
        }
    }

    val finalModifier = if (weightValue > 0f) modifier.fillMaxWidth() else modifier.fillMaxWidth()

    Column(modifier = finalModifier) {
        label?.let {
            com.itbenevides.genesys21.ui.components.text.GenesysText(
                text = it,
                style = GenesysTextStyle.Label,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .border(
                    width = 1.dp,
                    color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant,
                    shape = shape
                )
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp).padding(end = 12.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    if (textFieldValueState.text.isEmpty() && placeholder != null) {
                        com.itbenevides.genesys21.ui.components.text.GenesysText(
                            text = placeholder,
                            style = GenesysTextStyle.Body,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }

                    // 3. O Input Real: Atualização imediata para proteger o buffer do teclado no WasmJs.
                    BasicTextField(
                        value = textFieldValueState,
                        onValueChange = { newValue ->
                            textFieldValueState = newValue
                            if (newValue.text != value) {
                                onValueChange(newValue.text)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = keyboardOptions,
                        singleLine = singleLine,
                        minLines = minLines,
                        visualTransformation = visualTransformation
                    )
                }

                trailingIcon?.let {
                    Box(modifier = Modifier.padding(start = 8.dp)) { it() }
                }
            }
        }

        if (isError && supportingText != null) {
            com.itbenevides.genesys21.ui.components.text.GenesysText(
                text = supportingText,
                style = GenesysTextStyle.Label,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp),
                color = MaterialTheme.colorScheme.error
            )
        }
    }
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
    shape: Shape = RoundedCornerShape(16.dp),
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    weightValue: Float = 0f,
    modifier: Modifier = Modifier
) {
    val finalWeight = if (weightValue > 0f) weightValue else 1f
    Box(modifier = modifier.weight(finalWeight)) {
        com.itbenevides.genesys21.ui.components.input.GenesysTextField(
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
            shape = shape,
            colors = colors
        )
    }
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
    shape: Shape = RoundedCornerShape(16.dp),
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    weightValue: Float = 0f,
    modifier: Modifier = Modifier
) {
    com.itbenevides.genesys21.ui.components.input.GenesysTextField(
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
        shape = shape,
        colors = colors,
        weightValue = weightValue
    )
}
