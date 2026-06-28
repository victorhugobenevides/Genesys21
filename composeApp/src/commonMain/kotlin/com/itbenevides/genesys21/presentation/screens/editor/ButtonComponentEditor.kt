package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.runtime.*
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysTextField
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysColumn
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacer
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacing
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton

@Composable
fun ButtonComponentEditor(
    component: PageComponent.Button,
    onSave: (PageComponent.Button) -> Unit,
) {
    var text by remember(component) { mutableStateOf(component.text) }
    var url by remember(component) { mutableStateOf(component.url) }

    GenesysColumn(usePadding = false) {
        GenesysTextField(value = text, onValueChange = { text = it }, label = "Texto do Botão", icon = GenesysIcons.Edit)
        GenesysSpacer(GenesysSpacing.Medium)
        GenesysTextField(value = url, onValueChange = { url = it }, label = "URL de Destino", icon = GenesysIcons.Web)

        GenesysSpacer(GenesysSpacing.Large)
        GenesysLoadingButton(
            text = "Salvar Botão",
            onClick = { onSave(component.copy(text = text, url = url)) },
            fillWidth = true,
        )
    }
}
