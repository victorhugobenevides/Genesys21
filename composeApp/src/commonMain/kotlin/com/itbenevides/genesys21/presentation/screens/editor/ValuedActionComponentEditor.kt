package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.runtime.*
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysTextField
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysColumn
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacer
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.theme.GenesysTheme
import androidx.compose.material3.Switch
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysRow
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText
import com.itbenevides.genesys21.ui.theme.GenesysTextStyle

@Composable
fun ValuedActionComponentEditor(
    component: PageComponent.ValuedAction,
    onSave: (PageComponent.ValuedAction) -> Unit
) {
    var title by remember(component) { mutableStateOf(component.title) }
    var description by remember(component) { mutableStateOf(component.description ?: "") }
    var buttonText by remember(component) { mutableStateOf(component.buttonText) }
    var allowCustomValue by remember(component) { mutableStateOf(component.allowCustomValue) }

    LaunchedEffect(title, description, buttonText, allowCustomValue) {
        onSave(component.copy(
            title = title,
            description = description.ifBlank { null },
            buttonText = buttonText,
            allowCustomValue = allowCustomValue
        ))
    }

    GenesysColumn(usePadding = false) {
        GenesysTextField(value = title, onValueChange = { title = it }, label = "Título", icon = GenesysIcons.Edit)
        GenesysSpacer(GenesysTheme.spacing.s)
        GenesysTextField(value = description, onValueChange = { description = it }, label = "Descrição", icon = GenesysIcons.Description)

        GenesysSpacer(GenesysTheme.spacing.m)

        GenesysRow(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            GenesysText("Permitir Valor Personalizado", style = GenesysTextStyle.Body, modifier = androidx.compose.ui.Modifier.weight(1f))
            Switch(checked = allowCustomValue, onCheckedChange = { allowCustomValue = it })
        }

        GenesysSpacer(GenesysTheme.spacing.m)

        GenesysTextField(value = buttonText, onValueChange = { buttonText = it }, label = "Texto do Botão", icon = GenesysIcons.Language)
    }
}
