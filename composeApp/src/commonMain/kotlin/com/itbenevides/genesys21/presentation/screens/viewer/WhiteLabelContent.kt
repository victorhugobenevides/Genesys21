package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.presentation.screens.editor.*
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysFab
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysIconButton
import com.itbenevides.genesys21.ui.components.atoms.indicators.GenesysLoadingOverlay
import com.itbenevides.genesys21.ui.components.atoms.inputs.GenesysTextField
import com.itbenevides.genesys21.ui.components.atoms.primitives.*
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysFontWeight
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysText
import com.itbenevides.genesys21.ui.components.atoms.typography.GenesysTextStyle
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.molecules.feedback.GenesysEmptyState
import com.itbenevides.genesys21.ui.components.organisms.feedback.GenesysBottomSheet
import com.itbenevides.genesys21.ui.components.organisms.navigation.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.templates.pages.GenesysPage
import com.itbenevides.genesys21.ui.theme.GenesysDimens
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.ui.util.glassmorphic
import com.itbenevides.genesys21.ui.util.pulse
import com.itbenevides.genesys21.ui.util.staggeredEntry

@Composable
fun WhiteLabelContent(
    state: WhiteLabelState,
    viewModel: PageViewModel,
    onEvent: (WhiteLabelEvent) -> Unit,
    originalPage: Page,
    displayCategories: List<String>,
    onManageCategories: () -> Unit,
    onPickImage: () -> Unit,
    onDiscardClicked: () -> Unit,
) {
    GenesysPage(
        topBar = {
            GenesysTopAppBar(
                title = state.page.title,
                onBack = { onEvent(WhiteLabelEvent.OnBackClicked) },
                isTranslucent = true,
                actions = {
                    GenesysIconButton(
                        icon = GenesysIcons.Palette,
                        contentDescription = GenesysStrings.EditorThemes,
                        onClick = { onEvent(WhiteLabelEvent.OnShowThemeSelectorChanged(true)) },
                    )

                    if (state.page != originalPage) {
                        GenesysIconButton(
                            icon = GenesysIcons.Delete,
                            contentDescription = GenesysStrings.DiscardDraft,
                            tint = MaterialTheme.colorScheme.error,
                            onClick = onDiscardClicked,
                        )
                    }

                    GenesysLoadingButton(
                        text = GenesysStrings.Publish,
                        onClick = { onEvent(WhiteLabelEvent.OnPublishClicked) },
                        isLoading = state.isLoading,
                    )
                },
            )
        },
        floatingActionButton = {
            if (!state.isLoading) {
                GenesysFab(
                    icon = GenesysIcons.Add,
                    contentDescription = GenesysStrings.AddBlockAction,
                    onClick = { onEvent(WhiteLabelEvent.OnShowCatalogChanged(true)) },
                )
            }
        },
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWideScreen = maxWidth > 1000.dp

            if (state.isLoading) {
                GenesysLoadingOverlay()
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Pre-renderização da Vitrine (Preview)
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(top = if (isWideScreen) 0.dp else 0.dp),
                        // Ajustado pela TopBar translúcida
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        GenesysColumn(
                            maxWidth = GenesysDimens.ViewerMaxWidth,
                            horizontalAlignment = GenesysAlignment.Center,
                            usePadding = false,
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            if (state.page.components.isEmpty()) {
                                GenesysEmptyState(
                                    icon = GenesysIcons.Magic,
                                    title = GenesysStrings.EmptyEditorTitle,
                                    description = GenesysStrings.EmptyEditorDescription,
                                    action = {
                                        GenesysLoadingButton(
                                            text = GenesysStrings.AddBlockAction,
                                            onClick = { onEvent(WhiteLabelEvent.OnShowCatalogChanged(true)) },
                                        )
                                    },
                                )
                            } else {
                                GenesysLazyColumnIndexed(
                                    items = state.page.components,
                                    maxWidth = GenesysDimens.ViewerMaxWidth,
                                    usePadding = true,
                                    spacing = GenesysSpacing.Medium,
                                    key = { _, component -> component.hashCode() },
                                ) { index, component ->
                                    val isEditing = state.editingComponentIndex == index
                                    ComponentWrapperUI(component, index, isEditing, displayCategories, onEvent)
                                }
                            }
                        }
                    }

                    // Floating Dashboard para Desktop
                    if (isWideScreen) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxHeight()
                                    .width(400.dp)
                                    .align(Alignment.CenterEnd)
                                    .padding(24.dp),
                        ) {
                            Surface(
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .glassmorphic(RoundedCornerShape(32.dp)),
                                shape = RoundedCornerShape(32.dp),
                                tonalElevation = 8.dp,
                                color = Color.Transparent,
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
                                        onPickImage = onPickImage,
                                    )
                                } ?: run {
                                    GenesysEmptyState(
                                        icon = GenesysIcons.Edit,
                                        title = GenesysStrings.SelectBlockToEdit,
                                        description = GenesysStrings.SelectBlockToEditDesc,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Editor em BottomSheet para Mobile
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
                        onPickImage = onPickImage,
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
    onEvent: (WhiteLabelEvent) -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .staggeredEntry(index)
                .then(
                    if (isEditing) {
                        Modifier.border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), MaterialTheme.shapes.medium)
                    } else {
                        Modifier
                    },
                )
                .padding(2.dp)
                .clickable { onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(index)) },
    ) {
        PageComponentRenderer(
            component = component,
            isEditMode = true,
            onEditClick = { onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(index)) },
            allAvailableCategories = allCategories,
            onProductClick = { product ->
                onEvent(WhiteLabelEvent.OnEditProductClicked(product, index))
            },
        )

        // Controles de Gerenciamento
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isMobile = maxWidth < 400.dp

            Surface(
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(if (isMobile) 4.dp else 8.dp)
                        .then(if (isEditing) Modifier.pulse() else Modifier),
                shape = CircleShape,
                color =
                    if (isEditing) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(
                            alpha = 0.8f,
                        )
                    },
                tonalElevation = 6.dp,
            ) {
                Row(modifier = Modifier.padding(horizontal = if (isMobile) 2.dp else 4.dp)) {
                    GenesysIconButton(
                        icon = GenesysIcons.ArrowUp,
                        onClick = { onEvent(WhiteLabelEvent.OnMoveComponentUp(index)) },
                        tint = if (isEditing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = if (isMobile) Modifier.size(32.dp) else Modifier,
                    )
                    GenesysIconButton(
                        icon = GenesysIcons.ArrowDown,
                        onClick = { onEvent(WhiteLabelEvent.OnMoveComponentDown(index)) },
                        tint = if (isEditing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = if (isMobile) Modifier.size(32.dp) else Modifier,
                    )
                    GenesysIconButton(
                        icon = GenesysIcons.Delete,
                        onClick = { onEvent(WhiteLabelEvent.OnDeleteComponent(index)) },
                        tint = MaterialTheme.colorScheme.error,
                        modifier = if (isMobile) Modifier.size(32.dp) else Modifier,
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
    onPickImage: () -> Unit,
) {
    val component = state.page.components.getOrNull(index) ?: return
    val scrollState = rememberScrollState()

    @Composable
    fun EditorContent() {
        GenesysColumn(
            usePadding = false,
            modifier = Modifier.then(if (isEmbedded) Modifier.verticalScroll(scrollState) else Modifier),
        ) {
            if (isEmbedded) {
                GenesysRow(verticalAlignment = Alignment.CenterVertically) {
                    GenesysWeightBox(1f) {
                        GenesysText(
                            text = GenesysStrings.BlockSettings,
                            style = GenesysTextStyle.Title,
                            fontWeight = GenesysFontWeight.Bold,
                        )
                    }

                    GenesysIconButton(
                        icon = Icons.Default.Close,
                        onClick = { onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null)) },
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
                icon = GenesysIcons.Edit,
            )

            GenesysSpacer(GenesysSpacing.Large)
            GenesysDivider()
            GenesysSpacer(GenesysSpacing.Large)

            when (component) {
                is PageComponent.Header -> {
                    HeaderComponentEditor(
                        component = component,
                        onSave = { updated ->
                            val newList =
                                state.page.components.toMutableList().apply {
                                    set(
                                        index,
                                        updated.copy(customLabel = customLabel.ifBlank { null }),
                                    )
                                }
                            onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                            onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null))
                        },
                    )
                }
                is PageComponent.Text -> {
                    TextComponentEditor(
                        component = component,
                        onSave = { updated ->
                            val newList =
                                state.page.components.toMutableList().apply {
                                    set(
                                        index,
                                        updated.copy(customLabel = customLabel.ifBlank { null }),
                                    )
                                }
                            onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                            onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null))
                        },
                    )
                }
                is PageComponent.Image -> {
                    ImageComponentEditor(
                        component = component,
                        userPages = state.userPages,
                        isUploading = state.isUploading,
                        onPickImage = onPickImage,
                        onSave = { updated ->
                            val newList =
                                state.page.components.toMutableList().apply {
                                    set(
                                        index,
                                        updated.copy(customLabel = customLabel.ifBlank { null }),
                                    )
                                }
                            onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                            onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null))
                        },
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
                            onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null))
                        },
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
                            fillWidth = true,
                        )
                    }
                }
                is PageComponent.ProfileHeader -> {
                    ProfileHeaderComponentEditor(
                        component = component,
                        onSave = { updated: PageComponent.ProfileHeader ->
                            val newList =
                                state.page.components.toMutableList().apply {
                                    set(
                                        index,
                                        updated.copy(customLabel = customLabel.ifBlank { null }),
                                    )
                                }
                            onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                            onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null))
                        },
                        onPickImage = onPickImage,
                        isUploading = state.isUploading,
                    )
                }
                is PageComponent.SocialLinks -> {
                    SocialLinksComponentEditor(
                        component = component,
                        onSave = { updated: PageComponent.SocialLinks ->
                            val newList =
                                state.page.components.toMutableList().apply {
                                    set(
                                        index,
                                        updated.copy(customLabel = customLabel.ifBlank { null }),
                                    )
                                }
                            onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                            onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null))
                        },
                    )
                }
                is PageComponent.Button -> {
                    ButtonComponentEditor(
                        component = component,
                        onSave = { updated: PageComponent.Button ->
                            val newList =
                                state.page.components.toMutableList().apply {
                                    set(
                                        index,
                                        updated.copy(customLabel = customLabel.ifBlank { null }),
                                    )
                                }
                            onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                            onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null))
                        },
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
            title = GenesysStrings.BlockSettings,
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(bottom = 32.dp)) {
                EditorContent()
            }
        }
    }
}
