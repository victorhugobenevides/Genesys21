package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.runtime.*
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.ui.components.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.input.GenesysTextField
import com.itbenevides.genesys21.ui.components.layout.GenesysColumn
import com.itbenevides.genesys21.ui.components.layout.GenesysSpacer
import com.itbenevides.genesys21.ui.components.layout.GenesysSpacing
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons

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
