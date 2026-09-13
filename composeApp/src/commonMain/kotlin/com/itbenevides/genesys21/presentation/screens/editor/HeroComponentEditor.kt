package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.runtime.*
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysSlider
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysTextField
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysColumn
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacer
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.theme.GenesysTheme

@Composable
fun HeroComponentEditor(
    component: PageComponent.Hero,
    onSave: (PageComponent.Hero) -> Unit,
    onPickImage: () -> Unit,
    isUploading: Boolean = false,
) {
    var title by remember(component) { mutableStateOf(component.title) }
    var subtitle by remember(component) { mutableStateOf(component.subtitle ?: "") }
    var imageUrl by remember(component) { mutableStateOf(component.imageUrl) }
    var buttonText by remember(component) { mutableStateOf(component.buttonText ?: "") }
    var buttonUrl by remember(component) { mutableStateOf(component.buttonUrl ?: "") }
    var height by remember(component) { mutableStateOf(component.height.toFloat()) }

    LaunchedEffect(title, subtitle, imageUrl, buttonText, buttonUrl, height) {
        onSave(component.copy(
            title = title,
            subtitle = subtitle.ifBlank { null },
            imageUrl = imageUrl,
            buttonText = buttonText.ifBlank { null },
            buttonUrl = buttonUrl.ifBlank { null },
            height = height.toInt()
        ))
    }

    GenesysColumn(usePadding = false) {
        GenesysLoadingButton(
            text = if (isUploading) "Enviando..." else "Trocar Imagem de Fundo",
            onClick = onPickImage,
            icon = GenesysIcons.Image,
            isLoading = isUploading,
            fillWidth = true,
        )

        GenesysSpacer(GenesysTheme.spacing.m)

        GenesysTextField(value = title, onValueChange = { title = it }, label = "Título Principal", icon = GenesysIcons.Edit)
        GenesysSpacer(GenesysTheme.spacing.s)
        GenesysTextField(value = subtitle, onValueChange = { subtitle = it }, label = "Subtítulo (Opcional)", icon = GenesysIcons.Description)

        GenesysSpacer(GenesysTheme.spacing.m)

        GenesysSlider(
            value = height,
            onValueChange = { height = it },
            label = "Altura do Banner",
            valueRange = 200f..800f
        )

        GenesysSpacer(GenesysTheme.spacing.m)

        GenesysTextField(value = buttonText, onValueChange = { buttonText = it }, label = "Texto do Botão", icon = GenesysIcons.Language)
        GenesysSpacer(GenesysTheme.spacing.s)
        GenesysTextField(value = buttonUrl, onValueChange = { buttonUrl = it }, label = "Link do Botão (URL)", icon = GenesysIcons.Web)
    }
}
