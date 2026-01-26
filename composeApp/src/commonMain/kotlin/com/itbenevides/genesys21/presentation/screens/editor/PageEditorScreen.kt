package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.runtime.*
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.components.appbar.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.input.GenesysTextField
import com.itbenevides.genesys21.ui.components.layout.GenesysColumn
import com.itbenevides.genesys21.ui.components.layout.GenesysPage
import com.itbenevides.genesys21.ui.components.layout.GenesysSpacer
import com.itbenevides.genesys21.ui.components.layout.GenesysSpacing
import com.itbenevides.genesys21.ui.theme.GenesysStrings

@Composable
fun PageEditorScreen(
    viewModel: PageViewModel, 
    page: Page?, 
    onBack: () -> Unit
) {
    // 1. Camada de Gestão de Estado (State Holder)
    // Inicializa o estado uma única vez
    var state by remember { mutableStateOf(PageEditorState.initial(page)) }
    val isGlobalLoading by viewModel.isLoading.collectAsState()

    // Sincroniza o carregamento global com o estado da tela
    state = state.copy(isLoading = isGlobalLoading)

    // 2. Orquestrador de Eventos
    val onEvent: (PageEditorEvent) -> Unit = { event ->
        when (event) {
            is PageEditorEvent.OnTitleChanged -> {
                state = state.copy(
                    title = event.newTitle,
                    canSave = event.newTitle.isNotBlank()
                )
            }
            is PageEditorEvent.OnSaveClicked -> {
                val newPage = (page ?: Page(state.id, state.title.trim())).copy(title = state.title.trim())
                viewModel.savePage(newPage, isEditing = state.isEditing) { onBack() }
            }
            is PageEditorEvent.OnBackClicked -> onBack()
        }
    }

    // 3. Renderização Pura (UI)
    PageEditorContent(state, onEvent)
}

/**
 * UI Pura: Não conhece ViewModel, não conhece lógica de Random ou Persistência.
 * Apenas recebe o que mostrar e como avisar que algo aconteceu.
 */
@Composable
private fun PageEditorContent(
    state: PageEditorState,
    onEvent: (PageEditorEvent) -> Unit
) {
    GenesysPage(
        topBar = {
            GenesysTopAppBar(
                title = if (state.isEditing) GenesysStrings.EditPageTitle else GenesysStrings.NewPageTitle,
                onBack = { onEvent(PageEditorEvent.OnBackClicked) }
            )
        }
    ) {
        GenesysColumn {
            GenesysTextField(
                value = state.title, 
                onValueChange = { newValue -> onEvent(PageEditorEvent.OnTitleChanged(newValue)) }, 
                label = GenesysStrings.PageTitleLabel
            )
            
            GenesysSpacer(GenesysSpacing.Large)
            
            GenesysLoadingButton(
                onClick = { onEvent(PageEditorEvent.OnSaveClicked) },
                text = GenesysStrings.SavePageButton,
                isLoading = state.isLoading,
                enabled = state.canSave
            )
        }
    }
}
