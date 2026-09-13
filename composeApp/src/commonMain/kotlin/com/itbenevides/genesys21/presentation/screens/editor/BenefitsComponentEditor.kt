package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.runtime.*
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysTextField
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysColumn
import com.itbenevides.genesys21.ui.components.atoms.primitives.GenesysSpacer
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.theme.GenesysTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun BenefitsComponentEditor(
    component: PageComponent.Benefits,
    onSave: (PageComponent.Benefits) -> Unit
) {
    var title by remember(component) { mutableStateOf(component.title ?: "") }
    var items by remember(component) { mutableStateOf(component.items) }

    LaunchedEffect(title, items) {
        onSave(component.copy(
            title = title.ifBlank { null },
            items = items
        ))
    }

    GenesysColumn(usePadding = false) {
        GenesysTextField(value = title, onValueChange = { title = it }, label = "Título da Seção", icon = GenesysIcons.Edit)

        GenesysSpacer(GenesysTheme.spacing.l)

        items.forEachIndexed { index, item ->
            GenesysCard(modifier = Modifier.padding(bottom = GenesysTheme.spacing.m)) {
                Column(modifier = Modifier.padding(GenesysTheme.spacing.m)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Vantagem ${index + 1}", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                        IconButton(onClick = {
                            items = items.toMutableList().apply { removeAt(index) }
                        }) {
                            Icon(GenesysIcons.Delete, null, tint = MaterialTheme.colorScheme.error)
                        }
                    }

                    GenesysTextField(value = item.title, onValueChange = {
                        items = items.toMutableList().apply { set(index, item.copy(title = it)) }
                    }, label = "Título", icon = GenesysIcons.Edit)

                    GenesysSpacer(GenesysTheme.spacing.s)

                    GenesysTextField(value = item.description, onValueChange = {
                        items = items.toMutableList().apply { set(index, item.copy(description = it)) }
                    }, label = "Descrição", icon = GenesysIcons.Description)
                }
            }
        }

        GenesysLoadingButton(
            text = "Adicionar Vantagem",
            onClick = {
                items = items + PageComponent.BenefitItem("Nova Vantagem", "Descrição curta aqui", "Check")
            },
            icon = GenesysIcons.Add,
            fillWidth = true,
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    }
}
