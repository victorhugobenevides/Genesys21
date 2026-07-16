package com.itbenevides.genesys21.presentation.screens.editor

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysTextField
import com.itbenevides.genesys21.ui.components.atoms.primitives.*
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysTextStyle
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.components.organisms.navigation.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.templates.pages.GenesysPage
import com.itbenevides.genesys21.ui.theme.GenesysDimens
import com.itbenevides.genesys21.ui.theme.GenesysStrings

@Composable
fun PageEditorScreen(
    viewModel: PageViewModel,
    page: Page?,
    onBack: () -> Unit,
) {
    var state by remember { mutableStateOf(PageEditorState.initial(page)) }
    val isGlobalLoading by viewModel.isLoading.collectAsState()

    state = state.copy(isLoading = isGlobalLoading)

    val onEvent: (PageEditorEvent) -> Unit = { event ->
        when (event) {
            is PageEditorEvent.OnTitleChanged -> {
                state =
                    state.copy(
                        title = event.newTitle,
                        canSave = event.newTitle.isNotBlank(),
                    )
            }
            is PageEditorEvent.OnSaveClicked -> {
                val newPage = (page ?: Page(state.id, state.title.trim())).copy(title = state.title.trim())
                viewModel.savePage(newPage, isDraft = false) { onBack() }
            }
            is PageEditorEvent.OnBackClicked -> onBack()
        }
    }

    PageEditorContent(state, onEvent)
}

@Composable
private fun PageEditorContent(
    state: PageEditorState,
    onEvent: (PageEditorEvent) -> Unit,
) {
    GenesysPage(
        topBar = {
            GenesysTopAppBar(
                title = if (state.isEditing) GenesysStrings.EditPageTitle else GenesysStrings.NewPageTitle,
                onBack = { onEvent(PageEditorEvent.OnBackClicked) },
            )
        },
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            GenesysColumn(
                maxWidth = GenesysDimens.EditorMaxWidth,
                horizontalAlignment = GenesysAlignment.Center,
                useScroll = true,
            ) {
                GenesysCard {
                    GenesysColumn(usePadding = false) {
                        GenesysText(
                            text = GenesysStrings.EditPageTitle,
                            style = GenesysTextStyle.Title,
                        )
                        GenesysSpacer(GenesysSpacing.Medium)

                        GenesysTextField(
                            value = state.title,
                            onValueChange = { newValue -> onEvent(PageEditorEvent.OnTitleChanged(newValue)) },
                            label = GenesysStrings.PageTitleLabel,
                            placeholder = GenesysStrings.PageTitlePlaceholder,
                        )
                    }
                }

                GenesysSpacer(GenesysSpacing.Large)

                GenesysLoadingButton(
                    onClick = { onEvent(PageEditorEvent.OnSaveClicked) },
                    text = GenesysStrings.SavePageButton,
                    isLoading = state.isLoading,
                    enabled = state.canSave,
                    fillWidth = true,
                )

                GenesysSpacer(GenesysSpacing.Huge)
            }
        }
    }
}
