package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.domain.model.StepItem
import com.itbenevides.genesys21.presentation.screens.viewer.PageComponentRenderer
import com.itbenevides.genesys21.ui.components.button.GenesysIconButton
import com.itbenevides.genesys21.ui.components.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.card.GenesysCard
import com.itbenevides.genesys21.ui.components.input.GenesysTextField
import com.itbenevides.genesys21.ui.components.layout.*
import com.itbenevides.genesys21.ui.components.text.*
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.GenesysStrings

@Composable
fun StepProcessComponentEditor(
    component: PageComponent.StepProcess,
    onSave: (PageComponent.StepProcess) -> Unit
) {
    var steps by remember { mutableStateOf(component.steps) }

    val previewComponent = remember(steps) {
        component.copy(steps = steps)
    }

    GenesysColumn(usePadding = false) {
        GenesysText(text = GenesysStrings.Preview, style = GenesysTextStyle.Label)
        GenesysSpacer(GenesysSpacing.Small)
        PageComponentRenderer(component = previewComponent, isEditMode = false)

        GenesysSpacer(GenesysSpacing.Large)
        
        steps.forEachIndexed { index, step ->
            GenesysCard {
                GenesysColumn(usePadding = false) {
                    GenesysRow {
                        GenesysWeightBox(1f) {
                            GenesysText(text = "Passo ${index + 1}", style = GenesysTextStyle.Label, fontWeight = GenesysFontWeight.Bold)
                        }
                        GenesysIconButton(icon = GenesysIcons.Delete, onClick = {
                            steps = steps.filterIndexed { i, _ -> i != index }
                        })
                    }
                    GenesysTextField(
                        value = step.title,
                        onValueChange = { newTitle ->
                            steps = steps.toMutableList().apply { set(index, step.copy(title = newTitle)) }
                        },
                        label = "Título do Passo"
                    )
                    GenesysSpacer(GenesysSpacing.Small)
                    GenesysTextField(
                        value = step.description,
                        onValueChange = { newDesc ->
                            steps = steps.toMutableList().apply { set(index, step.copy(description = newDesc)) }
                        },
                        label = "Descrição"
                    )
                }
            }
            GenesysSpacer(GenesysSpacing.Medium)
        }

        if (steps.size < 5) {
            GenesysLoadingButton(
                text = "Adicionar Passo",
                icon = GenesysIcons.Add,
                onClick = { steps = steps + StepItem("Novo Passo", "Descrição aqui...") },
                fillWidth = true
            )
        }

        GenesysSpacer(GenesysSpacing.Large)
        GenesysLoadingButton(
            text = "Salvar Processo", 
            fillWidth = true,
            onClick = { onSave(previewComponent) }
        )
    }
}
