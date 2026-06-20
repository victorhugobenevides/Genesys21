package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.Category
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.components.button.GenesysIconButton
import com.itbenevides.genesys21.ui.components.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.card.GenesysCard
import com.itbenevides.genesys21.ui.components.feedback.GenesysDialog
import com.itbenevides.genesys21.ui.components.input.GenesysTextField
import com.itbenevides.genesys21.ui.components.layout.*
import com.itbenevides.genesys21.ui.components.text.*
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons

@Composable
fun CategoryManagementDialog(
    viewModel: PageViewModel,
    onDismiss: () -> Unit,
) {
    val categories by viewModel.categories.collectAsState()
    var newCategoryName by remember { mutableStateOf("") }
    var editingCategory by remember { mutableStateOf<Category?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadCategories()
    }

    fun handleSave() {
        if (editingCategory != null && editingCategory!!.name.isNotBlank()) {
            viewModel.saveCategory(editingCategory!!)
            editingCategory = null
        } else if (newCategoryName.isNotBlank()) {
            viewModel.saveCategory(Category(ownerId = "", name = newCategoryName))
            newCategoryName = ""
        }
    }

    GenesysDialog(
        onDismissRequest = onDismiss,
        title = "Gerenciar Categorias",
        confirmButton = {
            GenesysLoadingButton(
                text = "Concluir",
                onClick = {
                    handleSave()
                    onDismiss()
                },
            )
        },
    ) {
        GenesysColumn(usePadding = false, modifier = Modifier.heightIn(max = 450.dp)) {
            GenesysRow(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    key(editingCategory?.id) {
                        com.itbenevides.genesys21.ui.components.input.GenesysTextField(
                            value = if (editingCategory != null) editingCategory!!.name else newCategoryName,
                            onValueChange = {
                                if (editingCategory != null) {
                                    editingCategory = editingCategory!!.copy(name = it)
                                } else {
                                    newCategoryName = it
                                }
                            },
                            label = if (editingCategory != null) "Editando categoria" else "Nova categoria",
                            placeholder = "Digite o nome aqui...",
                            icon = GenesysIcons.Category,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                GenesysSpacer(GenesysSpacing.Small)

                GenesysIconButton(
                    icon = if (editingCategory != null) GenesysIcons.Check else GenesysIcons.Add,
                    tint = MaterialTheme.colorScheme.primary,
                    onClick = { handleSave() },
                )

                if (editingCategory != null) {
                    GenesysIconButton(
                        icon = GenesysIcons.Remove,
                        tint = Color.Gray,
                        contentDescription = "Cancelar Edição",
                        onClick = { editingCategory = null },
                    )
                }
            }

            GenesysSpacer(GenesysSpacing.Large)

            // CORREÇÃO: Usando chamada direta para evitar erro de receptor implícito
            com.itbenevides.genesys21.ui.components.text.GenesysText(
                text = "Categorias Salvas",
                style = GenesysTextStyle.Label,
                fontWeight = GenesysFontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )

            GenesysSpacer(GenesysSpacing.Small)
            GenesysDivider()
            GenesysSpacer(GenesysSpacing.Small)

            if (categories.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                    com.itbenevides.genesys21.ui.components.text.GenesysText(
                        text = "Nenhuma categoria cadastrada.",
                        style = GenesysTextStyle.Label,
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(categories, key = { it.id ?: it.name }) { category ->
                        GenesysCard(
                            modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(),
                            backgroundColor =
                                if (editingCategory?.id == category.id) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                } else {
                                    MaterialTheme.colorScheme.surface
                                },
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                // CORREÇÃO: Usando a versão de extensão correta do RowScope
                                val isEditingThis = editingCategory?.id == category.id
                                com.itbenevides.genesys21.ui.components.text.GenesysText(
                                    text = category.name,
                                    modifier = Modifier.weight(1f),
                                    fontWeight = if (isEditingThis) GenesysFontWeight.Bold else GenesysFontWeight.Normal,
                                )

                                GenesysIconButton(
                                    icon = GenesysIcons.Edit,
                                    onClick = { editingCategory = category },
                                )
                                GenesysIconButton(
                                    icon = GenesysIcons.Delete,
                                    tint = Color.Red.copy(alpha = 0.7f),
                                    onClick = { viewModel.deleteCategory(category.id!!) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
