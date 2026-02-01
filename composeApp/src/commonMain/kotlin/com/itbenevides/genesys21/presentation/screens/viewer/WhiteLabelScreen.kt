package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.domain.model.PageThemeConfig
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.presentation.screens.editor.*
import com.itbenevides.genesys21.ui.theme.AppTheme
import com.itbenevides.genesys21.ui.components.appbar.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.badge.GenesysBadge
import com.itbenevides.genesys21.ui.components.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.button.GenesysIconButton
import com.itbenevides.genesys21.ui.components.button.GenesysFab
import com.itbenevides.genesys21.ui.components.card.GenesysCard
import com.itbenevides.genesys21.ui.components.feedback.GenesysLoadingOverlay
import com.itbenevides.genesys21.ui.components.feedback.GenesysEmptyState
import com.itbenevides.genesys21.ui.components.feedback.GenesysBottomSheet
import com.itbenevides.genesys21.ui.components.image.GenesysColorCircle
import com.itbenevides.genesys21.ui.components.input.GenesysTextField
import com.itbenevides.genesys21.ui.components.layout.*
import com.itbenevides.genesys21.ui.components.text.*
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.ui.theme.GenesysDimens

@Composable
fun WhiteLabelScreen(
    viewModel: PageViewModel,
    page: Page,
    onPageChange: (Page) -> Unit,
    onBack: () -> Unit,
    onEditProduct: (Product?, Int?) -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val serverProducts by viewModel.allAvailableProducts.collectAsState()
    val savedCategories by viewModel.allAvailableCategories.collectAsState()
    val userPages by viewModel.pages.collectAsState()

    val pristinePage = remember(page.id) { page }
    var showCategoryManagement by remember { mutableStateOf(false) }

    // Inicializa o estado com o draft se existir
    var state by remember { 
        val draft = viewModel.getDraft(page.id)
        mutableStateOf(
            WhiteLabelState(
                page = draft ?: page,
                isLoading = isLoading,
                availableProducts = serverProducts,
                allAvailableCategories = savedCategories,
                userPages = userPages
            )
        )
    }

    // CORREÇÃO: Força a atualização do estado quando voltamos de outra tela (ex: Editor de Produtos)
    LaunchedEffect(Unit) {
        viewModel.getDraft(page.id)?.let { updatedDraft ->
            state = state.copy(page = updatedDraft)
        }
    }

    LaunchedEffect(isLoading, serverProducts, savedCategories, userPages) {
        state = state.copy(
            isLoading = isLoading,
            availableProducts = serverProducts,
            allAvailableCategories = savedCategories,
            userPages = userPages
        )
    }

    LaunchedEffect(state.page) {
        viewModel.saveDraft(state.page)
    }

    // LÓGICA DE CATEGORIAS: Mescla categorias do banco com as do rascunho atual
    val effectiveCategories = remember(savedCategories, state.page) {
        val categoriesInDraft = state.page.components
            .filterIsInstance<PageComponent.ProductList>()
            .flatMap { it.products }
            .mapNotNull { it.categoryName }
        (savedCategories + categoriesInDraft).filter { it.isNotBlank() }.distinct().sorted()
    }

    fun onEvent(event: WhiteLabelEvent) {
        when (event) {
            is WhiteLabelEvent.OnPageUpdated -> {
                state = state.copy(page = event.newPage)
                onPageChange(event.newPage)
            }
            is WhiteLabelEvent.OnPublishClicked -> {
                viewModel.savePage(state.page, true) { 
                    viewModel.clearDraft(state.page.id)
                    onBack() 
                }
            }
            is WhiteLabelEvent.OnBackClicked -> onBack()
            is WhiteLabelEvent.OnEditProductClicked -> onEditProduct(event.product, event.componentIndex)
            is WhiteLabelEvent.OnShowCatalogChanged -> state = state.copy(showCatalog = event.show)
            is WhiteLabelEvent.OnShowThemeSelectorChanged -> state = state.copy(showThemeSelector = event.show)
            is WhiteLabelEvent.OnShowPageSettingsChanged -> state = state.copy(showPageSettings = event.show)
            is WhiteLabelEvent.OnEditingComponentIndexChanged -> state = state.copy(editingComponentIndex = event.index)
            is WhiteLabelEvent.OnPendingNewComponentChanged -> state = state.copy(pendingNewComponent = event.component)
            is WhiteLabelEvent.OnFilterQueryChanged -> state = state.copy(filterQuery = event.query)
            
            is WhiteLabelEvent.OnDeleteComponent -> {
                val newList = state.page.components.toMutableList().apply { removeAt(event.index) }
                onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
            }
            is WhiteLabelEvent.OnDuplicateComponent -> {
                val component = state.page.components[event.index]
                val newList = state.page.components.toMutableList().apply { add(event.index + 1, component) }
                onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
            }
            is WhiteLabelEvent.OnMoveComponentUp -> {
                if (event.index > 0) {
                    val newList = state.page.components.toMutableList()
                    val temp = newList[event.index]
                    newList[event.index] = newList[event.index - 1]
                    newList[event.index - 1] = temp
                    onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                }
            }
            is WhiteLabelEvent.OnMoveComponentDown -> {
                if (event.index < state.page.components.size - 1) {
                    val newList = state.page.components.toMutableList()
                    val temp = newList[event.index]
                    newList[event.index] = newList[event.index + 1]
                    newList[event.index + 1] = temp
                    onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                }
            }
        }
    }

    AppTheme(themeConfig = state.page.theme) {
        WhiteLabelContent(
            state = state, 
            viewModel = viewModel, 
            onEvent = ::onEvent, 
            originalPage = pristinePage,
            displayCategories = effectiveCategories,
            onManageCategories = { showCategoryManagement = true }
        )

        if (showCategoryManagement) {
            CategoryManagementDialog(
                viewModel = viewModel,
                onDismiss = { showCategoryManagement = false }
            )
        }
    }
}

@Composable
private fun WhiteLabelContent(
    state: WhiteLabelState,
    viewModel: PageViewModel,
    onEvent: (WhiteLabelEvent) -> Unit,
    originalPage: Page,
    displayCategories: List<String>,
    onManageCategories: () -> Unit
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
                            onClick = {
                                viewModel.clearDraft(state.page.id)
                                onEvent(WhiteLabelEvent.OnPageUpdated(originalPage))
                            }
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
                                    maxWidth = GenesysDimens.ViewerMaxWidth
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
                                        onManageCategories = onManageCategories
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
                        onManageCategories = onManageCategories
                    )
                }
            }
        }
    }

    if (state.showPageSettings) PageSettingsUI(state, onEvent)
    if (state.showThemeSelector) ThemeSelectorUI(state, onEvent)
    if (state.showCatalog) ComponentCatalogUI(state, onEvent)
}

@Composable
private fun ComponentWrapperUI(
    component: PageComponent,
    index: Int,
    isEditing: Boolean,
    allCategories: List<String>,
    onEvent: (WhiteLabelEvent) -> Unit
) {
     GenesysColumn(
        usePadding = true,
        modifier = Modifier.padding(bottom = GenesysDimens.SpacingSmall)
    ) {
        GenesysRow {
            GenesysBadge(
                label = (component.customLabel ?: component::class.simpleName ?: "Bloco").uppercase(),
                color = if (isEditing) MaterialTheme.colorScheme.primary else Color.Unspecified
            )
            
            GenesysSpacer(GenesysSpacing.Small)
            
            GenesysIconButton(
                icon = GenesysIcons.Edit, 
                onClick = { onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(index)) },
                tint = if (isEditing) MaterialTheme.colorScheme.primary else Color.Unspecified
            )
            
            GenesysWeightSpacer(1f)
            
            GenesysRow(fillWidth = false) {
                GenesysIconButton(
                    icon = GenesysIcons.ArrowUp, 
                    onClick = { onEvent(WhiteLabelEvent.OnMoveComponentUp(index)) }
                )
                GenesysIconButton(
                    icon = GenesysIcons.ArrowDown, 
                    onClick = { onEvent(WhiteLabelEvent.OnMoveComponentDown(index)) }
                )
                GenesysIconButton(icon = GenesysIcons.Copy, onClick = { onEvent(WhiteLabelEvent.OnDuplicateComponent(index)) })
                GenesysIconButton(
                    icon = GenesysIcons.Delete, 
                    onClick = { onEvent(WhiteLabelEvent.OnDeleteComponent(index)) }, 
                    tint = Color.Red.copy(alpha = 0.7f)
                )
            }
        }

        GenesysCard(
            elevation = if (isEditing) GenesysDimens.ElevationHigh else GenesysDimens.ElevationLow,
            modifier = Modifier.animateContentSize(),
            backgroundColor = if (isEditing) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
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
    onManageCategories: () -> Unit
) {
    val component = state.page.components.getOrNull(index) ?: return
    
    val editorContent = @Composable {
        GenesysColumn(usePadding = isEmbedded, useScroll = isEmbedded) {
            GenesysRow(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                GenesysWeightBox(1f) {
                    GenesysText(text = GenesysStrings.BlockSettings, style = GenesysTextStyle.Title, fontWeight = GenesysFontWeight.Bold)
                }
                if (state.page != originalPage) {
                    GenesysIconButton(
                        icon = GenesysIcons.Delete,
                        contentDescription = GenesysStrings.DiscardDraft,
                        tint = MaterialTheme.colorScheme.error,
                        onClick = {
                            viewModel.clearDraft(state.page.id)
                            onEvent(WhiteLabelEvent.OnPageUpdated(originalPage))
                            onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null))
                        }
                    )
                }
            }
            
            GenesysSpacer(GenesysSpacing.Medium)

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
                            onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null))
                        }
                    )
                }
                is PageComponent.Text -> {
                    TextComponentEditor(
                        component = component,
                        onSave = { updated ->
                            val newList = state.page.components.toMutableList().apply { set(index, updated.copy(customLabel = customLabel.ifBlank { null })) }
                            onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                            onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null))
                        }
                    )
                }
                is PageComponent.Image -> {
                    ImageComponentEditor(
                        component = component,
                        userPages = state.userPages,
                        isUploading = false,
                        onPickImage = { /* Picker */ },
                        onSave = { updated ->
                            val newList = state.page.components.toMutableList().apply { set(index, updated.copy(customLabel = customLabel.ifBlank { null })) }
                            onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                            onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null))
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
                            onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null))
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
                else -> { }
            }
        }
    }

    if (isEmbedded) {
        editorContent()
    } else {
        GenesysBottomSheet(
            onDismiss = { onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null)) },
            title = GenesysStrings.BlockSettings
        ) {
            editorContent()
        }
    }
}

@Composable
private fun PageSettingsUI(state: WhiteLabelState, onEvent: (WhiteLabelEvent) -> Unit) {
    var title by remember(state.page.title) { mutableStateOf(state.page.title) }
    GenesysBottomSheet(
        onDismiss = { onEvent(WhiteLabelEvent.OnShowPageSettingsChanged(false)) },
        title = GenesysStrings.PageTitleLabel
    ) {
        GenesysColumn(usePadding = false) {
            GenesysTextField(
                value = title,
                onValueChange = { title = it },
                label = GenesysStrings.PageTitleLabel,
                icon = GenesysIcons.Edit
            )
            GenesysSpacer(GenesysSpacing.Large)
            GenesysLoadingButton(
                text = GenesysStrings.Save, 
                fillWidth = true,
                onClick = { 
                    onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(title = title)))
                    onEvent(WhiteLabelEvent.OnShowPageSettingsChanged(false))
                }
            )
        }
    }
}

@Composable
private fun ThemeSelectorUI(state: WhiteLabelState, onEvent: (WhiteLabelEvent) -> Unit) {
    val themes = listOf(
        Triple(PageThemeConfig.ROYAL, "Royal Night", Color(0xFF14213D)),
        Triple(PageThemeConfig.OCEAN, "Ocean Blue", Color(0xFF00ADB5)),
        Triple(PageThemeConfig.FOREST, "Deep Forest", Color(0xFF283618)),
        Triple(PageThemeConfig.CANDY, "Sweet Candy", Color(0xFFFF758F))
    )
    GenesysBottomSheet(
        onDismiss = { onEvent(WhiteLabelEvent.OnShowThemeSelectorChanged(false)) },
        title = GenesysStrings.CustomizeStyle
    ) {
        GenesysColumn(usePadding = false, useScroll = true) {
            GenesysText(GenesysStrings.ThemeDescription, style = GenesysTextStyle.Label)
            GenesysSpacer(GenesysSpacing.Medium)
            themes.forEach { (config, label, color) ->
                val isSelected = state.page.theme == config
                GenesysCard(
                    modifier = Modifier.padding(bottom = 8.dp),
                    backgroundColor = if (isSelected) color.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                    onClick = { 
                        onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(theme = config)))
                        onEvent(WhiteLabelEvent.OnShowThemeSelectorChanged(false))
                    }
                ) {
                    GenesysRow {
                        GenesysColorCircle(color = color)
                        GenesysSpacer(GenesysSpacing.Medium)
                        GenesysWeightBox(1f) {
                            GenesysText(text = label, style = GenesysTextStyle.Body, fontWeight = if (isSelected) GenesysFontWeight.Bold else GenesysFontWeight.Normal)
                        }
                        if (isSelected) GenesysIconButton(icon = GenesysIcons.Check, onClick = {})
                    }
                }
            }
        }
    }
}

@Composable
private fun ComponentCatalogUI(state: WhiteLabelState, onEvent: (WhiteLabelEvent) -> Unit) {
     GenesysBottomSheet(
        onDismiss = { onEvent(WhiteLabelEvent.OnShowCatalogChanged(false)) },
        title = GenesysStrings.AddBlockTitle
    ) {
        GenesysColumn(usePadding = false, useScroll = true) {
            GenesysText(GenesysStrings.AddBlockDescription, style = GenesysTextStyle.Label)
            GenesysSpacer(GenesysSpacing.Medium)
            
            val catalogItems = listOf(
                Triple(GenesysStrings.ComponentTypeHeader, GenesysStrings.ComponentTypeHeaderText, PageComponent.Header("Novo Título")),
                Triple(GenesysStrings.ComponentTypeText, GenesysStrings.ComponentTypeTextDesc, PageComponent.Text("Seu texto aqui...")),
                Triple(GenesysStrings.ComponentTypeProductList, GenesysStrings.ComponentTypeProductListDesc, PageComponent.ProductList(emptyList())),
                Triple(GenesysStrings.ComponentTypeImage, GenesysStrings.ComponentTypeImageDesc, PageComponent.Image("", "")),
                Triple(GenesysStrings.ComponentTypeButton, GenesysStrings.ComponentTypeButtonDesc, PageComponent.Button("Toque Aqui", "")),
                Triple(GenesysStrings.ComponentTypeFilter, GenesysStrings.ComponentTypeFilterDesc, PageComponent.Filter()),
                Triple(GenesysStrings.ProductCategory, "Filtro de categorias.", PageComponent.CategoryFilter())
            )
            
            catalogItems.forEach { (title, desc, component) ->
                GenesysCard(
                    modifier = Modifier.padding(bottom = 8.dp),
                    onClick = {
                        val newList = state.page.components.toMutableList().apply { add(component) }
                        onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                        onEvent(WhiteLabelEvent.OnShowCatalogChanged(false))
                    }
                ) {
                    GenesysRow {
                        GenesysWeightBox(1f) {
                            GenesysColumn(usePadding = false) {
                                GenesysText(title, style = GenesysTextStyle.Body, fontWeight = GenesysFontWeight.Bold)
                                GenesysText(desc, style = GenesysTextStyle.Label)
                            }
                        }
                        GenesysIconButton(icon = GenesysIcons.Add, onClick = {})
                    }
                }
            }
        }
    }
}
