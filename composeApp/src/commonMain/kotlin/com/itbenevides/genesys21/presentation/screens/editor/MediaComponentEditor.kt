package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.presentation.screens.viewer.PageComponentRenderer
import com.itbenevides.genesys21.ui.components.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.input.GenesysSlider
import com.itbenevides.genesys21.ui.components.input.GenesysTextField
import com.itbenevides.genesys21.ui.components.layout.*
import com.itbenevides.genesys21.ui.components.text.*
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.GenesysStrings

@Composable
fun MediaComponentEditor(
    component: PageComponent.Media,
    isUploading: Boolean,
    onPickImage: () -> Unit,
    onSave: (PageComponent.Media) -> Unit
) {
    var url by remember { mutableStateOf(component.url) }
    var title by remember { mutableStateOf(component.title ?: "") }
    var description by remember { mutableStateOf(component.description ?: "") }
    var layout by remember { mutableStateOf(component.layout) }
    var imageOnRight by remember { mutableStateOf(component.imageOnRight) }
    var size by remember { mutableStateOf(component.size.toFloat()) }
    var isRounded by remember { mutableStateOf(component.isRounded) }
    var hasBottomArc by remember { mutableStateOf(component.hasBottomArc) }

    val previewComponent = remember(url, title, description, layout, imageOnRight, size, isRounded, hasBottomArc) {
        component.copy(
            url = url, title = title.ifBlank { null }, description = description.ifBlank { null },
            layout = layout, imageOnRight = imageOnRight, size = size.toInt(),
            isRounded = isRounded, hasBottomArc = hasBottomArc
        )
    }

    GenesysColumn(usePadding = false) {
        GenesysText(text = GenesysStrings.Preview, style = GenesysTextStyle.Label)
        GenesysSpacer(GenesysSpacing.Small)
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            PageComponentRenderer(component = previewComponent, isEditMode = false)
        }

        GenesysSpacer(GenesysSpacing.Large)
        GenesysLoadingButton(text = if (isUploading) "Enviando..." else "Trocar Imagem", icon = GenesysIcons.CloudUpload, onClick = onPickImage, isLoading = isUploading, fillWidth = true)

        GenesysSpacer(GenesysSpacing.Medium)
        GenesysText(text = "Layout do Bloco", style = GenesysTextStyle.Label)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("FULL_WIDTH" to "Cheia", "SIDE_TEXT" to "Lado a Lado", "CIRCULAR" to "Avatar").forEach { (id, label) ->
                FilterChip(selected = layout == id, onClick = { layout = id }, label = { Text(label) })
            }
        }

        if (layout == "SIDE_TEXT") {
            GenesysSpacer(GenesysSpacing.Medium)
            GenesysTextField(value = title, onValueChange = { title = it }, label = "Título", icon = GenesysIcons.Edit)
            GenesysSpacer(GenesysSpacing.Small)
            GenesysTextField(value = description, onValueChange = { description = it }, label = "Descrição", singleLine = false, minLines = 3)
            
            GenesysRow(verticalAlignment = Alignment.CenterVertically) {
                GenesysText("Imagem na Direita?", style = GenesysTextStyle.Body, weightValue = 1f)
                Switch(checked = imageOnRight, onCheckedChange = { imageOnRight = it })
            }
        }

        GenesysSpacer(GenesysSpacing.Medium)
        GenesysSlider(value = size, onValueChange = { size = it }, label = "Tamanho: ${size.toInt()}px", valueRange = 50f..800f)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                GenesysText("Cantos Redondos?", style = GenesysTextStyle.Body)
                Switch(checked = isRounded, onCheckedChange = { isRounded = it })
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                GenesysText("Estilo Arco?", style = GenesysTextStyle.Body)
                Switch(checked = hasBottomArc, onCheckedChange = { hasBottomArc = it })
            }
        }

        GenesysSpacer(GenesysSpacing.Large)
        GenesysLoadingButton(text = "Confirmar Mídia", fillWidth = true, onClick = { onSave(previewComponent) })
    }
}
