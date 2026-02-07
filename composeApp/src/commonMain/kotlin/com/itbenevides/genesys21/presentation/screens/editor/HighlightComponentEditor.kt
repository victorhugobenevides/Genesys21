package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
fun HighlightComponentEditor(
    component: PageComponent.Highlight,
    onSave: (PageComponent.Highlight) -> Unit
) {
    var text by remember { mutableStateOf(component.text) }
    var type by remember { mutableStateOf(component.type) }
    var url by remember { mutableStateOf(component.url ?: "") }
    var bgColor by remember { mutableStateOf(component.backgroundColor ?: "#BC1B1B") }
    var textColor by remember { mutableStateOf(component.textColor ?: "#FFFFFF") }

    val previewComponent = remember(text, type, url, bgColor, textColor) {
        component.copy(
            text = text, type = type, url = url.ifBlank { null },
            backgroundColor = bgColor, textColor = textColor
        )
    }

    GenesysColumn(usePadding = false) {
        GenesysText(text = GenesysStrings.Preview, style = GenesysTextStyle.Label)
        GenesysSpacer(GenesysSpacing.Small)
        PageComponentRenderer(component = previewComponent, isEditMode = false)

        GenesysSpacer(GenesysSpacing.Large)
        GenesysText(text = "Tipo de Destaque", style = GenesysTextStyle.Label)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("BUTTON" to "Botão", "MARQUEE" to "Rotativo", "BADGE" to "Pílula").forEach { (id, label) ->
                FilterChip(selected = type == id, onClick = { type = id }, label = { Text(label) })
            }
        }

        GenesysSpacer(GenesysSpacing.Medium)
        GenesysTextField(value = text, onValueChange = { text = it }, label = "Texto", icon = GenesysIcons.Edit)

        if (type == "BUTTON") {
            GenesysSpacer(GenesysSpacing.Small)
            GenesysTextField(value = url, onValueChange = { url = it }, label = "Link de Destino", icon = GenesysIcons.Language)
        }

        if (type == "MARQUEE") {
            GenesysSpacer(GenesysSpacing.Small)
            GenesysRow {
                GenesysWeightBox(1f) { GenesysTextField(value = bgColor, onValueChange = { bgColor = it }, label = "Fundo (Hex)") }
                GenesysSpacer(GenesysSpacing.Small)
                GenesysWeightBox(1f) { GenesysTextField(value = textColor, onValueChange = { textColor = it }, label = "Texto (Hex)") }
            }
        }

        GenesysSpacer(GenesysSpacing.Large)
        GenesysLoadingButton(text = "Confirmar Destaque", fillWidth = true, onClick = { onSave(previewComponent) })
    }
}
