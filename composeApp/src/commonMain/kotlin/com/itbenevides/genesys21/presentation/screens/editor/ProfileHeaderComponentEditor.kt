package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Switch
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.presentation.screens.viewer.PageComponentRenderer
import com.itbenevides.genesys21.ui.components.input.GenesysTextField
import com.itbenevides.genesys21.ui.components.input.GenesysSlider
import com.itbenevides.genesys21.ui.components.layout.*
import com.itbenevides.genesys21.ui.components.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.text.GenesysText
import com.itbenevides.genesys21.ui.components.text.GenesysTextStyle
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons

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
        GenesysText(text = "Pré-visualização", style = GenesysTextStyle.Label)
        GenesysSpacer(GenesysSpacing.Small)
        
        PageComponentRenderer(
            component = previewComponent,
            isEditMode = false,
            allProducts = emptyList()
        )
        
        GenesysSpacer(GenesysSpacing.Large)

        GenesysLoadingButton(
            text = if (isUploading) "Enviando..." else "Trocar Foto de Perfil",
            onClick = onPickImage,
            icon = GenesysIcons.CloudUpload,
            isLoading = isUploading,
            fillWidth = true
        )
        
        GenesysSpacer(GenesysSpacing.Medium)
        
        GenesysSlider(
            value = imageSize, 
            onValueChange = { imageSize = it }, 
            label = "Tamanho da Foto", 
            valueRange = 40f..300f
        )

        GenesysSpacer(GenesysSpacing.Medium)
        
        GenesysRow(fillWidth = true) {
            GenesysWeightBox(1f) {
                GenesysColumn(usePadding = false) {
                    GenesysText("Foto Circular?", style = GenesysTextStyle.Body)
                    Switch(checked = isCircular, onCheckedChange = { isCircular = it })
                }
            }
        }

        GenesysSpacer(GenesysSpacing.Medium)
        
         GenesysTextField(
            value = imageUrl,
            onValueChange = { imageUrl = it },
            label = "URL da Foto",
            icon = GenesysIcons.CloudUpload
        )
        
        GenesysSpacer(GenesysSpacing.Medium)
        GenesysTextField(
            value = name,
            onValueChange = { name = it },
            label = "Nome",
            icon = GenesysIcons.Person
        )
        GenesysSpacer(GenesysSpacing.Medium)
        GenesysTextField(
            value = bio,
            onValueChange = { bio = it },
            label = "Biografia",
            icon = GenesysIcons.Edit
        )
        GenesysSpacer(GenesysSpacing.Large)
        GenesysLoadingButton(
            text = "Salvar Alterações",
            onClick = { 
                onSave(component.copy(
                    imageUrl = imageUrl, 
                    name = name, 
                    bio = bio,
                    imageSize = imageSize.toInt(),
                    isCircular = isCircular
                )) 
            },
            fillWidth = true
        )
    }
}
