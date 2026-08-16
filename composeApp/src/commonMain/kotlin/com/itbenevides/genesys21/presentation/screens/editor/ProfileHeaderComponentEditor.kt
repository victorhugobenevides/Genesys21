package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.material3.Switch
import androidx.compose.runtime.*
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.presentation.screens.viewer.PageComponentRenderer
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysSlider
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysTextField
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysColumn
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysRow
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacer
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysWeightBox
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText
import com.itbenevides.genesys21.ui.theme.*
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton

@Composable
fun ProfileHeaderComponentEditor(
    component: PageComponent.ProfileHeader,
    onSave: (PageComponent.ProfileHeader) -> Unit,
    onPickImage: () -> Unit,
    isUploading: Boolean = false,
) {
    var imageUrl by remember(component) { mutableStateOf(component.imageUrl) }
    var name by remember(component) { mutableStateOf(component.name) }
    var bio by remember(component) { mutableStateOf(component.bio) }
    var imageSize by remember(component) { mutableStateOf(component.imageSize.toFloat()) }
    var isCircular by remember(component) { mutableStateOf(component.isCircular) }

    LaunchedEffect(component.imageUrl) {
        imageUrl = component.imageUrl
    }

    val previewComponent =
        remember(imageUrl, name, bio, imageSize, isCircular) {
            component.copy(
                imageUrl = imageUrl,
                name = name,
                bio = bio,
                imageSize = imageSize.toInt(),
                isCircular = isCircular,
            )
        }

    GenesysColumn(usePadding = false) {
        GenesysText(text = "Pré-visualização", style = GenesysTextStyle.Label)
        GenesysSpacer(GenesysTheme.spacing.s)

        PageComponentRenderer(
            component = previewComponent,
            isEditMode = false,
        )

        GenesysSpacer(GenesysTheme.spacing.l)

        GenesysLoadingButton(
            text = if (isUploading) "Enviando..." else "Trocar Foto de Perfil",
            onClick = onPickImage,
            icon = GenesysIcons.CloudUpload,
            isLoading = isUploading,
            fillWidth = true,
        )

        GenesysSpacer(GenesysTheme.spacing.m)

        GenesysSlider(
            value = imageSize,
            onValueChange = { imageSize = it },
            label = "Tamanho da Foto",
            valueRange = 40f..300f,
        )

        GenesysSpacer(GenesysTheme.spacing.m)

        GenesysRow(fillWidth = true) {
            GenesysWeightBox(1f) {
                GenesysColumn(usePadding = false) {
                    GenesysText("Foto Circular?", style = GenesysTextStyle.Body)
                    Switch(checked = isCircular, onCheckedChange = { isCircular = it })
                }
            }
        }

        GenesysSpacer(GenesysTheme.spacing.m)

        GenesysTextField(
            value = imageUrl,
            onValueChange = { imageUrl = it },
            label = "URL da Foto",
            icon = GenesysIcons.CloudUpload,
        )

        GenesysSpacer(GenesysTheme.spacing.m)
        GenesysTextField(
            value = name,
            onValueChange = { name = it },
            label = "Nome",
            icon = GenesysIcons.Person,
        )
        GenesysSpacer(GenesysTheme.spacing.m)
        GenesysTextField(
            value = bio,
            onValueChange = { bio = it },
            label = "Biografia",
            icon = GenesysIcons.Edit,
        )
        GenesysSpacer(GenesysTheme.spacing.l)
        GenesysLoadingButton(
            text = "Salvar Alterações",
            onClick = {
                onSave(
                    component.copy(
                        imageUrl = imageUrl,
                        name = name,
                        bio = bio,
                        imageSize = imageSize.toInt(),
                        isCircular = isCircular,
                    ),
                )
            },
            fillWidth = true,
        )
    }
}
