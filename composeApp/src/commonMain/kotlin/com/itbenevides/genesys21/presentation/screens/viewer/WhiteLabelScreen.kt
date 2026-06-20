package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.BrandingEffects
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
import com.itbenevides.genesys21.ui.components.feedback.*
import com.itbenevides.genesys21.ui.components.input.GenesysTextField
import com.itbenevides.genesys21.ui.components.layout.*
import com.itbenevides.genesys21.ui.components.text.*
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.AppTheme
import com.itbenevides.genesys21.ui.theme.GenesysDimens
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.util.rememberImagePicker
import kotlin.random.Random

@Composable
fun WhiteLabelScreen(
    viewModel: PageViewModel,
    page: Page,
    onPageChange: (Page) -> Unit,
    onBack: () -> Unit,
    onEditProduct: (Product?, Int?) -> Unit,
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val serverProducts by viewModel.allAvailableProducts.collectAsState()
    val savedCategories by viewModel.allAvailableCategories.collectAsState()
    val userPages by viewModel.pages.collectAsState()

    val pristinePage = remember(page.id) { page }
    var showCategoryManagement by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    var state by remember {
        val draft = viewModel.getDraft(page.id)
        mutableStateOf(
            WhiteLabelState(
                page = draft ?: page,
                isLoading = isLoading,
                availableProducts = serverProducts,
                allAvailableCategories = savedCategories,
                userPages = userPages,
            ),
        )
    }

    LaunchedEffect(Unit) {
        viewModel.loadPages()
        viewModel.loadCategories()

        viewModel.getDraft(page.id)?.let { updatedDraft ->
            state = state.copy(page = updatedDraft)
        }
    }

    LaunchedEffect(isLoading, serverProducts, savedCategories, userPages) {
        state =
            state.copy(
                isLoading = isLoading,
                availableProducts = serverProducts,
                allAvailableCategories = savedCategories,
                userPages = userPages,
            )
    }

    LaunchedEffect(state.page) {
        viewModel.saveDraft(state.page)
    }

    val effectiveCategories =
        remember(savedCategories, state.page) {
            val categoriesInDraft =
                state.page.components
                    .filterIsInstance<PageComponent.ProductList>()
                    .flatMap { it.products }
                    .mapNotNull { it.categoryName }
            (savedCategories + categoriesInDraft).filter { it.isNotBlank() }.distinct().sorted()
        }

    val imagePicker =
        rememberImagePicker { bytes: ByteArray? ->
            bytes?.let {
                state = state.copy(isUploading = true)
                viewModel.uploadImage(it, "profile_${Random.nextInt(10000)}.jpg") { uploadedUrl ->
                    state.editingComponentIndex?.let { index ->
                        val component = state.page.components[index]
                        val updated =
                            when (component) {
                                is PageComponent.Image -> component.copy(url = uploadedUrl)
                                is PageComponent.ProfileHeader -> component.copy(imageUrl = uploadedUrl)
                                else -> component
                            }
                        val newList = state.page.components.toMutableList().apply { set(index, updated) }
                        state =
                            state.copy(
                                page = state.page.copy(components = newList),
                                isUploading = false,
                            )
                    }
                }
            }
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
            is WhiteLabelEvent.OnShowThemeLabChanged -> state = state.copy(showThemeLab = event.show)
            is WhiteLabelEvent.OnEditingComponentIndexChanged -> state = state.copy(editingComponentIndex = event.index)
            is WhiteLabelEvent.OnPendingNewComponentChanged -> state = state.copy(pendingNewComponent = event.component)
            is WhiteLabelEvent.OnFilterQueryChanged -> state = state.copy(filterQuery = event.query)
            is WhiteLabelEvent.OnImageUploadStarted -> state = state.copy(isUploading = event.isUploading)

            is WhiteLabelEvent.OnDeleteComponent -> {
                val newList = state.page.components.toMutableList().apply { removeAt(event.index) }
                onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                if (state.editingComponentIndex == event.index) {
                    state = state.copy(editingComponentIndex = null)
                }
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
                    if (state.editingComponentIndex == event.index) {
                        state = state.copy(editingComponentIndex = event.index - 1)
                    } else if (state.editingComponentIndex == event.index - 1) {
                        state = state.copy(editingComponentIndex = event.index)
                    }
                }
            }
            is WhiteLabelEvent.OnMoveComponentDown -> {
                if (event.index < state.page.components.size - 1) {
                    val newList = state.page.components.toMutableList()
                    val temp = newList[event.index]
                    newList[event.index] = newList[event.index + 1]
                    newList[event.index + 1] = temp
                    onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                    if (state.editingComponentIndex == event.index) {
                        state = state.copy(editingComponentIndex = event.index + 1)
                    } else if (state.editingComponentIndex == event.index + 1) {
                        state = state.copy(editingComponentIndex = event.index)
                    }
                }
            }
        }
    }

    val allProducts by viewModel.allAvailableProducts.collectAsState()

    AppTheme(themeConfig = state.page.theme) {
        BrandingEffects(page = state.page)
        WhiteLabelContent(
            state = state,
            viewModel = viewModel,
            onEvent = ::onEvent,
            originalPage = pristinePage,
            displayCategories = effectiveCategories,
            allProducts = allProducts,
            onManageCategories = { showCategoryManagement = true },
            onPickImage = { imagePicker() },
            onDiscardClicked = { showDiscardDialog = true },
        )

        if (state.showThemeSelector) {
            ThemeSelectorBottomSheet(
                currentTheme = state.page.theme,
                onThemeSelected = { newTheme ->
                    onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(theme = newTheme)))
                    state = state.copy(showThemeSelector = false)
                },
                onDismiss = { state = state.copy(showThemeSelector = false) },
            )
        }

        if (showCategoryManagement) {
            CategoryManagementDialog(
                viewModel = viewModel,
                onDismiss = { showCategoryManagement = false },
            )
        }

        if (state.showThemeLab) {
            ThemeLabDialog(
                initialConfig = state.page.customTheme,
                onSave = { newConfig ->
                    onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(customTheme = newConfig)))
                    onEvent(WhiteLabelEvent.OnShowThemeLabChanged(false))
                },
                onDismiss = { onEvent(WhiteLabelEvent.OnShowThemeLabChanged(false)) },
            )
        }

        if (showDiscardDialog) {
            GenesysConfirmDialog(
                onDismissRequest = { showDiscardDialog = false },
                title = GenesysStrings.DiscardDraft,
                text = "Tem certeza que deseja descartar todas as alterações não publicadas?",
                icon = GenesysIcons.Delete,
                confirmButton = {
                    GenesysLoadingButton(
                        text = "Descartar",
                        onClick = {
                            viewModel.clearDraft(state.page.id)
                            onEvent(WhiteLabelEvent.OnPageUpdated(pristinePage))
                            showDiscardDialog = false
                        },
                        containerColor = MaterialTheme.colorScheme.error,
                    )
                },
                dismissButton = {
                    TextButton(onClick = { showDiscardDialog = false }) {
                        Text("Cancelar")
                    }
                },
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
    allProducts: List<Product>,
    onManageCategories: () -> Unit,
    onPickImage: () -> Unit,
    onDiscardClicked: () -> Unit,
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
                        onClick = { onEvent(WhiteLabelEvent.OnShowThemeSelectorChanged(true)) },
                    )

                    GenesysIconButton(
                        icon = GenesysIcons.Magic,
                        contentDescription = "Theme Lab",
                        onClick = { onEvent(WhiteLabelEvent.OnShowThemeLabChanged(true)) },
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
                GenesysRow(modifier = Modifier.fillMaxSize(), usePadding = false) {
                    GenesysWeightBox(if (isWideScreen) 0.65f else 1f) {
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
                                    // Reduzido para celulares
                                    spacing = GenesysSpacing.Medium,
                                    key = { _, component -> component.hashCode() },
                                ) { index, component ->
                                    val isEditing = state.editingComponentIndex == index
                                    ComponentWrapperUI(component, index, isEditing, displayCategories, allProducts, onEvent)
                                }
                            }
                        }
                    }

                    if (isWideScreen) {
                        GenesysWeightBox(0.35f) {
                            GenesysCard(
                                modifier = Modifier.fillMaxHeight().padding(16.dp),
                                elevation = GenesysDimens.ElevationMedium,
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

            if (!isWideScreen) {
                state.editingComponentIndex?.let { index ->
                    GenesysBottomSheet(
                        onDismiss = { onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null)) },
                        title = GenesysStrings.BlockSettings,
                    ) {
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

    if (state.showPageSettings) PageSettingsUI(state, onEvent)
    if (state.showCatalog) ComponentCatalogUI(state, onEvent)
}

@Composable
private fun ComponentWrapperUI(
    component: PageComponent,
    index: Int,
    isEditing: Boolean,
    allCategories: List<String>,
    allProducts: List<Product>,
    onEvent: (WhiteLabelEvent) -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .then(
                    if (isEditing) {
                        Modifier.border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), MaterialTheme.shapes.medium)
                    } else {
                        Modifier
                    },
                )
                // Reduzido para mobile
                .padding(2.dp)
                .clickable { onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(index)) },
    ) {
        PageComponentRenderer(
            component = component,
            isEditMode = true,
            onEditClick = { onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(index)) },
            allAvailableCategories = allCategories,
            allProducts = allProducts,
            onProductClick = { product ->
                onEvent(WhiteLabelEvent.OnEditProductClicked(product, index))
            },
        )

        // SEMPRE VISÍVEL: Controles de Gerenciamento (Mover e Excluir)
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isMobile = maxWidth < 400.dp

            Surface(
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(if (isMobile) 4.dp else 8.dp),
                shape = CircleShape,
                color = if (isEditing) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
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

    GenesysColumn(
        usePadding = false,
        modifier = Modifier.then(if (isEmbedded) Modifier.verticalScroll(scrollState) else Modifier),
    ) {
        if (isEmbedded) {
            GenesysRow(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
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
                        val newList = state.page.components.toMutableList().apply { set(index, updated.copy(customLabel = customLabel.ifBlank { null })) }
                        onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                        onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null))
                    },
                )
            }
            is PageComponent.Text -> {
                TextComponentEditor(
                    component = component,
                    onSave = { updated ->
                        val newList = state.page.components.toMutableList().apply { set(index, updated.copy(customLabel = customLabel.ifBlank { null })) }
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
                        val newList = state.page.components.toMutableList().apply { set(index, updated.copy(customLabel = customLabel.ifBlank { null })) }
                        onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                        onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null))
                    },
                )
            }
            is PageComponent.ProfileHeader -> {
                ProfileHeaderComponentEditor(
                    component = component,
                    onPickImage = onPickImage,
                    isUploading = state.isUploading,
                    onSave = { updated ->
                        val newList = state.page.components.toMutableList().apply { set(index, updated.copy(customLabel = customLabel.ifBlank { null })) }
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
            is PageComponent.Button -> {
                ButtonComponentEditor(
                    component = component,
                    onSave = { updated ->
                        val newList = state.page.components.toMutableList().apply { set(index, updated.copy(customLabel = customLabel.ifBlank { null })) }
                        onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                        onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null))
                    },
                )
            }
            is PageComponent.SocialLinks -> {
                SocialLinksComponentEditor(
                    component = component,
                    onSave = { updated ->
                        val newList = state.page.components.toMutableList().apply { set(index, updated.copy(customLabel = customLabel.ifBlank { null })) }
                        onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                        onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null))
                    },
                )
            }
            else -> {
                // Outros componentes sem editor específico
                GenesysLoadingButton(
                    text = "Confirmar Nome do Bloco",
                    onClick = {
                        onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null))
                    },
                    fillWidth = true,
                )
            }
        }
    }
}

@Composable
private fun PageSettingsUI(
    state: WhiteLabelState,
    onEvent: (WhiteLabelEvent) -> Unit,
) {
    var title by remember { mutableStateOf(state.page.title) }
    var whatsapp by remember { mutableStateOf(state.page.whatsapp ?: "") }

    GenesysBottomSheet(
        onDismiss = { onEvent(WhiteLabelEvent.OnShowPageSettingsChanged(false)) },
        title = "Configurações da Página",
    ) {
        GenesysColumn(usePadding = true) {
            GenesysTextField(
                value = title,
                onValueChange = { title = it },
                label = "Título da Página",
                icon = GenesysIcons.Web,
            )

            GenesysSpacer(GenesysSpacing.Medium)

            GenesysTextField(
                value = whatsapp,
                onValueChange = { whatsapp = it },
                label = "WhatsApp de Contato",
                icon = GenesysIcons.WhatsApp,
            )

            GenesysSpacer(GenesysSpacing.Large)

            GenesysLoadingButton(
                text = "Salvar Configurações",
                onClick = {
                    onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(title = title, whatsapp = whatsapp.ifBlank { null })))
                    onEvent(WhiteLabelEvent.OnShowPageSettingsChanged(false))
                },
                fillWidth = true,
            )
        }
    }
}

@Composable
private fun ComponentCatalogUI(
    state: WhiteLabelState,
    onEvent: (WhiteLabelEvent) -> Unit,
) {
    GenesysBottomSheet(
        onDismiss = { onEvent(WhiteLabelEvent.OnShowCatalogChanged(false)) },
        title = "Adicionar Novo Bloco",
    ) {
        GenesysColumn(usePadding = true) {
            val catalogItems =
                listOf(
                    CatalogItem("Cabeçalho", GenesysIcons.Description) { PageComponent.Header(title = "Novo Cabeçalho") },
                    CatalogItem("Texto", GenesysIcons.Edit) { PageComponent.Text(content = "Seu texto aqui...") },
                    CatalogItem("Imagem", GenesysIcons.Image) { PageComponent.Image(url = "https://picsum.photos/800/400") },
                    CatalogItem("Botão", GenesysIcons.Language) { PageComponent.Button(text = "Clique Aqui", url = "https://") },
                    CatalogItem("Lista de Produtos", GenesysIcons.Inventory) { PageComponent.ProductList(products = emptyList()) },
                    CatalogItem("Grade de Produtos", GenesysIcons.Inventory) { PageComponent.ProductGrid(productIds = emptyList()) },
                    CatalogItem("Barra de Busca", GenesysIcons.Search) { PageComponent.Filter() },
                    CatalogItem("Filtro de Categorias", GenesysIcons.Category) { PageComponent.CategoryFilter() },
                    CatalogItem("Perfil", GenesysIcons.Person) { PageComponent.ProfileHeader(imageUrl = "", name = "Seu Nome") },
                    CatalogItem("Links Sociais", GenesysIcons.Share) { PageComponent.SocialLinks() },
                    CatalogItem("Carrinho", GenesysIcons.ShoppingBag) { PageComponent.CartComponent() },
                    CatalogItem("Rastreio", GenesysIcons.List) { PageComponent.OrderTrackingComponent() },
                )

            catalogItems.chunked(2).forEach { rowItems ->
                GenesysRow(modifier = Modifier.fillMaxWidth()) {
                    rowItems.forEach { item ->
                        GenesysWeightBox(1f) {
                            GenesysCard(
                                onClick = {
                                    val newList = state.page.components + item.createComponent()
                                    onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                                    onEvent(WhiteLabelEvent.OnShowCatalogChanged(false))
                                },
                                modifier = Modifier.height(100.dp),
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    Icon(item.icon, null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.height(8.dp))
                                    Text(item.name, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                    if (rowItems.size < 2) Spacer(Modifier.weight(1f))
                }
                GenesysSpacer(GenesysSpacing.Small)
            }
        }
    }
}

data class CatalogItem(
    val name: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val createComponent: () -> PageComponent,
)
