package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.presentation.screens.viewer.PageComponentRenderer
import com.itbenevides.genesys21.ui.components.input.GenesysTextField
import com.itbenevides.genesys21.ui.components.input.GenesysSlider
import com.itbenevides.genesys21.ui.components.layout.*
import com.itbenevides.genesys21.ui.components.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.text.*
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.GenesysStrings

@Composable
fun ProfileHeaderComponentEditor(
    component: PageComponent.ProfileHeader,
    onSave: (PageComponent.ProfileHeader) -> Unit,
    onPickImage: () -> Unit,
    isUploading: Boolean = false
) {
    var imageUrl by remember(component) { mutableStateOf(component.imageUrl) }
    var name by remember(component) { mutableStateOf(component.name) }
    var bio by remember(component) { mutableStateOf(component.bio) }
    var imageSize by remember(component) { mutableStateOf(component.imageSize.toFloat()) }
    var isCircular by remember(component) { mutableStateOf(component.isCircular) }
    var backgroundColor by remember(component) { mutableStateOf("#00000000") } // Default transparente ou cor do tema

    LaunchedEffect(component.imageUrl) {
        imageUrl = component.imageUrl
    }

    val previewComponent = remember(imageUrl, name, bio, imageSize, isCircular) {
        component.copy(
            imageUrl = imageUrl, 
            name = name, 
            bio = bio, 
            imageSize = imageSize.toInt(),
            isCircular = isCircular
        )
    }

     GenesysColumn(usePadding = false) {
        GenesysText(text = GenesysStrings.Preview, style = GenesysTextStyle.Label)
        GenesysSpacer(GenesysSpacing.Small)
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            PageComponentRenderer(component = previewComponent, isEditMode = false)
        }
        
        GenesysSpacer(GenesysSpacing.Large)

        ImageEditControls(
            imageUrl = imageUrl,
            isUploading = isUploading,
            onPickImage = onPickImage
        )
        
        GenesysSpacer(GenesysSpacing.Medium)
        
        GenesysSlider(
            value = imageSize, 
            onValueChange = { imageSize = it }, 
            label = "Tamanho da Foto: ${imageSize.toInt()}px", 
            valueRange = 40f..300f
        )

        GenesysSpacer(GenesysSpacing.Medium)
        
        GenesysRow(verticalAlignment = Alignment.CenterVertically) {
            GenesysText("Foto Circular?", style = GenesysTextStyle.Body, weightValue = 1f)
            Switch(checked = isCircular, onCheckedChange = { isCircular = it })
        }

        GenesysSpacer(GenesysSpacing.Medium)
        GenesysTextField(
            value = backgroundColor,
            onValueChange = { backgroundColor = it },
            label = "Cor de Fundo (Hex)",
            placeholder = "#RRGGBB",
            icon = GenesysIcons.Palette
        )

        GenesysSpacer(GenesysSpacing.Medium)
        GenesysTextField(
            value = name,
            onValueChange = { name = it },
            label = "Seu Nome",
            icon = GenesysIcons.Person
        )
        GenesysSpacer(GenesysSpacing.Medium)
        GenesysTextField(
            value = bio,
            onValueChange = { bio = it },
            label = "Sua Bio",
            icon = GenesysIcons.Edit,
            singleLine = false,
            minLines = 3
        )
        
        GenesysSpacer(GenesysSpacing.Large)
        GenesysLoadingButton(
            text = "Salvar Alterações",
            onClick = { onSave(previewComponent) },
            fillWidth = true
        )
    }
}
