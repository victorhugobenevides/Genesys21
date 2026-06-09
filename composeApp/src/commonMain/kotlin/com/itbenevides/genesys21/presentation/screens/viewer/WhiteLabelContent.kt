package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.presentation.screens.editor.*
import com.itbenevides.genesys21.ui.components.appbar.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.button.GenesysFab
import com.itbenevides.genesys21.ui.components.button.GenesysIconButton
import com.itbenevides.genesys21.ui.components.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.card.GenesysCard
import com.itbenevides.genesys21.ui.components.feedback.GenesysBottomSheet
import com.itbenevides.genesys21.ui.components.feedback.GenesysEmptyState
import com.itbenevides.genesys21.ui.components.feedback.GenesysLoadingOverlay
import com.itbenevides.genesys21.ui.components.input.GenesysTextField
import com.itbenevides.genesys21.ui.components.layout.*
import com.itbenevides.genesys21.ui.components.text.GenesysFontWeight
import com.itbenevides.genesys21.ui.components.text.GenesysText
import com.itbenevides.genesys21.ui.components.text.GenesysTextStyle
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.GenesysDimens
import com.itbenevides.genesys21.ui.theme.GenesysStrings

@Composable
fun WhiteLabelContent(
    state: WhiteLabelState,
    viewModel: PageViewModel,
    onEvent: (WhiteLabelEvent) -> Unit,
    originalPage: Page,
    displayCategories: List<String>,
    onManageCategories: () -> Unit,
    onPickImage: () -> Unit,
    onDiscardClicked: () -> Unit
) {
    GenesysPage(
        topBar = {
            GenesysTopAppBar(
                title = state.page.title,
                onBack = { onEvent(WhiteLabelEvent.OnBackClicked) },
                actions = {
                     GenesysIconButton(
                        icon = GenesysIcons.Palette, 
                        contentDescription = GenesysStrings.EditorThemes,
                        onClick = { onEvent(WhiteLabelEvent.OnShowThemeSelectorChanged(true)) }
                    )
                    
                    if (state.page != originalPage) {
                        GenesysIconButton(
                            icon = GenesysIcons.Delete,
                            contentDescription = GenesysStrings.DiscardDraft,
                            tint = MaterialTheme.colorScheme.error,
                            onClick = onDiscardClicked
                        )
                    }

                    GenesysLoadingButton(
                        text = GenesysStrings.Publish,
                        onClick = { onEvent(WhiteLabelEvent.OnPublishClicked) },
                        isLoading = state.isLoading
                    )
                }
            )
        },
        floatingActionButton = {
            if (!state.isLoading) {
                GenesysFab(
                    icon = GenesysIcons.Add,
                    contentDescription = GenesysStrings.AddBlockAction,
                    onClick = { onEvent(WhiteLabelEvent.OnShowCatalogChanged(true)) }
                )
            }
        }
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWideScreen = maxWidth > 1000.dp

            if (state.isLoading) {
                GenesysLoadingOverlay()
            } else {
                // Debug info (Remove later)
                // Text("Debug: CompCount=${state.page.components.size}, isWide=$isWideScreen", modifier = Modifier.padding(8.dp).align(Alignment.BottomStart), color = androidx.compose.ui.graphics.Color.Red)

                GenesysRow(modifier = Modifier.fillMaxSize(), usePadding = false) {
                    GenesysWeightBox(if (isWideScreen) 0.65f else 1f) {
                        GenesysColumn(
                            maxWidth = GenesysDimens.ViewerMaxWidth,
                            horizontalAlignment = GenesysAlignment.Center,
                            usePadding = false,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (state.page.components.isEmpty()) {
                                GenesysEmptyState(
                                    icon = GenesysIcons.Magic,
                                    title = GenesysStrings.EmptyEditorTitle,
                                    description = GenesysStrings.EmptyEditorDescription,
                                    action = {
                                        GenesysLoadingButton(
                                            text = GenesysStrings.AddBlockAction, 
                                            onClick = { onEvent(WhiteLabelEvent.OnShowCatalogChanged(true)) }
                                        )
                                    }
                                )
                            } else {
                                GenesysLazyColumnIndexed(
                                    items = state.page.components,
                                    maxWidth = GenesysDimens.ViewerMaxWidth,
                                    usePadding = true,
                                    spacing = GenesysSpacing.Medium, // Reduzido para celulares
                                    key = { _, component -> component.hashCode() }
                                ) { index, component ->
                                    val isEditing = state.editingComponentIndex == index
                                    ComponentWrapperUI(component, index, isEditing, displayCategories, onEvent)
                                }
                            }
                        }
                    }

                    if (isWideScreen) {
                        GenesysWeightBox(0.35f) {
                            GenesysCard(
                                modifier = Modifier.fillMaxHeight().padding(16.dp),
                                elevation = GenesysDimens.ElevationMedium
                            ) {
                                state.editingComponentIndex?.let { index ->
                                    ComponentEditorUI(
                                        state = state, 
                                        viewModel = viewModel, 
                                        index = index, 
                                        onEvent = onEvent, 
                                        isEmbedded = true, 
                                        originalPage = originalPage,
                                        onManageCategories = onManageCategories,
                                        onPickImage = onPickImage
                                    )
                                } ?: run {
                                    GenesysEmptyState(
                                        icon = GenesysIcons.Edit,
                                        title = GenesysStrings.SelectBlockToEdit,
                                        description = GenesysStrings.SelectBlockToEditDesc
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            if (!isWideScreen) {
                state.editingComponentIndex?.let { index ->
                    ComponentEditorUI(
                        state = state, 
                        viewModel = viewModel, 
                        index = index, 
                        onEvent = onEvent, 
                        isEmbedded = false, 
                        originalPage = originalPage,
                        onManageCategories = onManageCategories,
                        onPickImage = onPickImage
                    )
                }
            }
        }
    }
}

@Composable
private fun ComponentWrapperUI(
    component: PageComponent,
    index: Int,
    isEditing: Boolean,
    allCategories: List<String>,
    onEvent: (WhiteLabelEvent) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isEditing) Modifier.border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), MaterialTheme.shapes.medium)
                else Modifier
            )
            .padding(2.dp) // Reduzido para mobile
            .clickable { onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(index)) }
    ) {
        PageComponentRenderer(
            component = component,
            isEditMode = true,
            onEditClick = { onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(index)) },
            allAvailableCategories = allCategories,
            onProductClick = { product ->
                onEvent(WhiteLabelEvent.OnEditProductClicked(product, index))
            }
        )

        // SEMPRE VISÍVEL: Controles de Gerenciamento (Mover e Excluir)
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isMobile = maxWidth < 400.dp
            
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(if (isMobile) 4.dp else 8.dp),
                shape = CircleShape,
                color = if (isEditing) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                tonalElevation = 6.dp
            ) {
                Row(modifier = Modifier.padding(horizontal = if (isMobile) 2.dp else 4.dp)) {
                    GenesysIconButton(
                        icon = GenesysIcons.ArrowUp, 
                        onClick = { onEvent(WhiteLabelEvent.OnMoveComponentUp(index)) },
                        tint = if (isEditing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = if (isMobile) Modifier.size(32.dp) else Modifier
                    )
                    GenesysIconButton(
                        icon = GenesysIcons.ArrowDown, 
                        onClick = { onEvent(WhiteLabelEvent.OnMoveComponentDown(index)) },
                        tint = if (isEditing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = if (isMobile) Modifier.size(32.dp) else Modifier
                    )
                    GenesysIconButton(
                        icon = GenesysIcons.Delete, 
                        onClick = { onEvent(WhiteLabelEvent.OnDeleteComponent(index)) }, 
                        tint = MaterialTheme.colorScheme.error,
                        modifier = if (isMobile) Modifier.size(32.dp) else Modifier
                    )
                }
            }
        }
    }
}

@Composable
private fun ComponentEditorUI(
    state: WhiteLabelState,
    viewModel: PageViewModel,
    index: Int,
    onEvent: (WhiteLabelEvent) -> Unit,
    isEmbedded: Boolean = false,
    originalPage: Page,
    onManageCategories: () -> Unit,
    onPickImage: () -> Unit
) {
    val component = state.page.components.getOrNull(index) ?: return
    val scrollState = rememberScrollState()
    
    @Composable
    fun EditorContent() {
        GenesysColumn(
            usePadding = false, 
            modifier = Modifier.then(if (isEmbedded) Modifier.verticalScroll(scrollState) else Modifier)
        ) {
            if (isEmbedded) {
                GenesysRow(verticalAlignment = Alignment.CenterVertically) {
                    GenesysWeightBox(1f) {
                        GenesysText(
                            text = GenesysStrings.BlockSettings, 
                            style = GenesysTextStyle.Title, 
                            fontWeight = GenesysFontWeight.Bold
                        )
                    }
                    
                    GenesysIconButton(
                        icon = Icons.Default.Close,
                        onClick = { onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null)) }
                    )
                }
                GenesysSpacer(GenesysSpacing.Medium)
            }

            var customLabel by remember(component) { mutableStateOf(component.customLabel ?: "") }
            
            GenesysTextField(
                value = customLabel,
                onValueChange = { customLabel = it },
                label = GenesysStrings.BlockNameLabel,
                placeholder = GenesysStrings.BlockNamePlaceholder,
                icon = GenesysIcons.Edit
            )
            
            GenesysSpacer(GenesysSpacing.Large)
            GenesysDivider()
            GenesysSpacer(GenesysSpacing.Large)

            when (component) {
                is PageComponent.Header -> {
                    HeaderComponentEditor(
                        component = component,
                        onSave = { updated ->
                            val newList = state.page.components.toMutableList().apply { set(index, updated.copy(customLabel = customLabel.ifBlank { null })) }
                            onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                            onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null)) // FECHA O BOTTOM SHEET
                        }
                    )
                }
                is PageComponent.Text -> {
                    TextComponentEditor(
                        component = component,
                        onSave = { updated ->
                            val newList = state.page.components.toMutableList().apply { set(index, updated.copy(customLabel = customLabel.ifBlank { null })) }
                            onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                            onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null)) // FECHA O BOTTOM SHEET
                        }
                    )
                }
                is PageComponent.Image -> {
                    ImageComponentEditor(
                        component = component,
                        userPages = state.userPages,
                        isUploading = state.isUploading,
                        onPickImage = onPickImage,
                        onSave = { updated ->
                            val newList = state.page.components.toMutableList().apply { set(index, updated.copy(customLabel = customLabel.ifBlank { null })) }
                            onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                            onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null)) // FECHA O BOTTOM SHEET
                        }
                    )
                }
                is PageComponent.ProductList -> {
                    ProductListComponentEditor(
                        component = component,
                        allAvailableProducts = state.availableProducts,
                        onEditProduct = { product ->
                            onEvent(WhiteLabelEvent.OnEditProductClicked(product, index)) 
                        },
                        onProductsUpdated = { newProducts ->
                             val updatedComponent = component.copy(products = newProducts)
                             val newList = state.page.components.toMutableList().apply { set(index, updatedComponent) }
                             onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                        },
                        onSaveLabel = { newLabel, isHorizontal ->
                            val updated = component.copy(customLabel = newLabel.ifBlank { null }, isHorizontal = isHorizontal)
                            val newList = state.page.components.toMutableList().apply { set(index, updated) }
                            onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                            onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null)) // FECHA O BOTTOM SHEET
                        }
                    )
                }
                is PageComponent.CategoryFilter -> {
                    GenesysColumn(usePadding = false) {
                        GenesysText("Este bloco exibe suas categorias automaticamente.", style = GenesysTextStyle.Body)
                        GenesysSpacer(GenesysSpacing.Medium)
                        GenesysLoadingButton(
                            text = "Gerenciar Categorias",
                            icon = GenesysIcons.Category,
                            onClick = onManageCategories,
                            fillWidth = true
                        )
                    }
                }
                is PageComponent.ProfileHeader -> {
                    ProfileHeaderComponentEditor(
                        component = component,
                        onSave = { updated: PageComponent.ProfileHeader ->
                            val newList = state.page.components.toMutableList().apply { set(index, updated.copy(customLabel = customLabel.ifBlank { null })) }
                            onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                            onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null)) // FECHA O BOTTOM SHEET
                        },
                        onPickImage = onPickImage,
                        isUploading = state.isUploading
                    )
                }
                is PageComponent.SocialLinks -> {
                    SocialLinksComponentEditor(
                        component = component,
                        onSave = { updated: PageComponent.SocialLinks ->
                            val newList = state.page.components.toMutableList().apply { set(index, updated.copy(customLabel = customLabel.ifBlank { null })) }
                            onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                            onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null)) // FECHA O BOTTOM SHEET
                        }
                    )
                }
                is PageComponent.Button -> {
                    ButtonComponentEditor(
                        component = component,
                        onSave = { updated: PageComponent.Button ->
                            val newList = state.page.components.toMutableList().apply { set(index, updated.copy(customLabel = customLabel.ifBlank { null })) }
                            onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                            onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null)) // FECHA O BOTTOM SHEET
                        }
                    )
                }
                else -> { }
            }

            GenesysSpacer(GenesysSpacing.Huge)
        }
    }

    if (isEmbedded) {
        EditorContent()
    } else {
        GenesysBottomSheet(
            onDismiss = { onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null)) },
            title = GenesysStrings.BlockSettings
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(bottom = 32.dp)) {
                EditorContent()
            }
        }
    }
}
