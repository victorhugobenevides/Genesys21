package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.runtime.*
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysSlider
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysColumn
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacer
import com.itbenevides.genesys21.ui.theme.GenesysTheme

@Composable
fun SpacerComponentEditor(
    component: PageComponent.Spacer,
    onSave: (PageComponent.Spacer) -> Unit,
) {
    var height by remember(component) { mutableStateOf(component.height.toFloat()) }

    LaunchedEffect(height) {
        onSave(component.copy(height = height.toInt()))
    }

    GenesysColumn(usePadding = false) {
        GenesysSlider(
            value = height,
            onValueChange = { height = it },
            label = "Altura do Espaço (px)",
            valueRange = 4f..200f,
        )

        GenesysSpacer(GenesysTheme.spacing.m)
    }
}
