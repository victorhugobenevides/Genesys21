package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.runtime.*
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysTextField
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysColumn
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacer
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.theme.GenesysTheme

@Composable
fun ButtonComponentEditor(
    component: PageComponent.Button,
    onSave: (PageComponent.Button) -> Unit,
) {
    var text by remember(component) { mutableStateOf(component.text) }
    var url by remember(component) { mutableStateOf(component.url) }

    GenesysColumn(usePadding = false) {
        GenesysTextField(value = text, onValueChange = { text = it }, label = "Texto do Botão", icon = GenesysIcons.Edit)
        GenesysSpacer(GenesysTheme.spacing.m)
        GenesysTextField(value = url, onValueChange = { url = it }, label = "URL de Destino", icon = GenesysIcons.Web)

        GenesysSpacer(GenesysTheme.spacing.l)
        GenesysLoadingButton(
            text = "Salvar Botão",
            onClick = { onSave(component.copy(text = text, url = url)) },
            fillWidth = true,
        )
    }
}
