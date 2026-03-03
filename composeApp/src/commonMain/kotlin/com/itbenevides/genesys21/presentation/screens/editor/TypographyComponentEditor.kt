package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.presentation.screens.viewer.PageComponentRenderer
import com.itbenevides.genesys21.ui.components.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.input.GenesysTextField
import com.itbenevides.genesys21.ui.components.layout.*
import com.itbenevides.genesys21.ui.components.text.*
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.GenesysStrings

@Composable
fun TypographyComponentEditor(
    component: PageComponent.Typography,
    onSave: (PageComponent.Typography) -> Unit
) {
    var text by remember { mutableStateOf(component.text) }
    var style by remember { mutableStateOf(component.style) }
    var fontSize by remember { mutableStateOf(component.fontSize.toFloat()) }
    var textAlign by remember { mutableStateOf(component.textAlign) }
    var fontWeight by remember { mutableStateOf(component.fontWeight) }
    var isUppercase by remember { mutableStateOf(component.isUppercase) }
    var usePrimaryColor by remember { mutableStateOf(component.usePrimaryColor) }
    var backgroundColor by remember { mutableStateOf(component.backgroundColor) }

    val previewComponent = remember(text, style, fontSize, textAlign, fontWeight, isUppercase, usePrimaryColor, backgroundColor) {
        component.copy(
            text = text, style = style, fontSize = fontSize.toInt(),
            textAlign = textAlign, fontWeight = fontWeight,
            isUppercase = isUppercase, usePrimaryColor = usePrimaryColor,
            backgroundColor = backgroundColor
        )
    }

    GenesysColumn(usePadding = false) {
        GenesysText(text = GenesysStrings.Preview, style = GenesysTextStyle.Label)
        GenesysSpacer(GenesysSpacing.Small)
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            PageComponentRenderer(component = previewComponent, isEditMode = false)
        }

        GenesysSpacer(GenesysSpacing.Large)
        
        // Seletor de Estilo
        GenesysText(text = "Estilo do Texto", style = GenesysTextStyle.Label)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("BODY" to "Corpo", "HEADER" to "Título", "SHADOW" to "Título 3D").forEach { (id, label) ->
                FilterChip(
                    selected = style == id,
                    onClick = { 
                        style = id
                        if (id == "HEADER") fontSize = 28f
                        if (id == "SHADOW") fontSize = 42f
                    },
                    label = { Text(label) },
                    modifier = Modifier.testTag("chip_style_$id")
                )
            }
        }

        GenesysSpacer(GenesysSpacing.Medium)
        
        BackgroundColorEditControls(
            backgroundColor = backgroundColor,
            onColorChange = { backgroundColor = it }
        )

        GenesysSpacer(GenesysSpacing.Medium)
        
        // CORREÇÃO: Passando a tag exata que o teste espera
        GenesysTextField(
            value = text, 
            onValueChange = { text = it }, 
            label = "Conteúdo do Texto", 
            singleLine = false, 
            minLines = 3, 
            icon = GenesysIcons.Edit,
            modifier = Modifier.testTag("input_typography_text")
        )
        
        GenesysSpacer(GenesysSpacing.Medium)
        TextFormatControls(
            fontSize = fontSize, onFontSizeChange = { fontSize = it },
            textAlign = textAlign, onTextAlignChange = { textAlign = it },
            fontWeight = fontWeight, onFontWeightChange = { fontWeight = it },
            isUppercase = isUppercase, onUppercaseChange = { isUppercase = it }
        )

        GenesysRow(verticalAlignment = Alignment.CenterVertically) {
            // CORREÇÃO: Usando Modifier.weight(1f)
            GenesysText(
                text = "Usar cor da marca?", 
                style = GenesysTextStyle.Body, 
                modifier = Modifier.weight(1f)
            )
            Switch(checked = usePrimaryColor, onCheckedChange = { usePrimaryColor = it })
        }

        GenesysSpacer(GenesysSpacing.Large)
        GenesysLoadingButton(
            text = "Confirmar Texto", 
            fillWidth = true, 
            onClick = { onSave(previewComponent) },
            modifier = Modifier.testTag("btn_confirm_typography")
        )
    }
}
