package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.presentation.screens.editor.*
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysFab
import com.itbenevides.genesys21.ui.components.atoms.buttons.GenesysIconButton
import com.itbenevides.genesys21.ui.components.atoms.indicators.*
import com.itbenevides.genesys21.ui.components.atoms.primitives.*
import com.itbenevides.genesys21.ui.components.atoms.tokens.GenesysIcons
import com.itbenevides.genesys21.ui.components.atoms.typography.*
import com.itbenevides.genesys21.ui.components.molecules.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.molecules.card.GenesysCard
import com.itbenevides.genesys21.ui.components.molecules.feedback.GenesysEmptyState
import com.itbenevides.genesys21.ui.components.organisms.feedback.GenesysBottomSheet
import com.itbenevides.genesys21.ui.components.organisms.navigation.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.templates.pages.GenesysPage
import com.itbenevides.genesys21.ui.theme.GenesysDimens
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.ui.util.GenesysWindowSizeClass
import com.itbenevides.genesys21.ui.util.LocalWindowSizeClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhiteLabelContent(
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
    val allServices by viewModel.services.collectAsState()
    val windowSizeClass = LocalWindowSizeClass.current
    val isExpanded = windowSizeClass == GenesysWindowSizeClass.EXPANDED
    val isCompact = windowSizeClass == GenesysWindowSizeClass.COMPACT

    GenesysPage(
        topBar = {
            GenesysTopAppBar(
                title = if (isCompact) state.page.title.take(15).let { if (it.length < state.page.title.length) "$it..." else it } else state.page.title,
                onBack = { onEvent(WhiteLabelEvent.OnBackClicked) },
                actions = {
                    if (!isCompact) {
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
                    } else {
                        // Overflow for compact
                        var showMenu by remember { mutableStateOf(false) }
                        Box {
                            GenesysIconButton(icon = GenesysIcons.MoreVert, onClick = { showMenu = true })
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(
                                    text = { Text(GenesysStrings.EditorThemes) },
                                    onClick = {
                                        showMenu = false
                                        onEvent(WhiteLabelEvent.OnShowThemeSelectorChanged(true))
                                    },
                                    leadingIcon = { Icon(GenesysIcons.Palette, null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Theme Lab") },
                                    onClick = {
                                        showMenu = false
                                        onEvent(WhiteLabelEvent.OnShowThemeLabChanged(true))
                                    },
                                    leadingIcon = { Icon(GenesysIcons.Magic, null) }
                                )
                                if (state.page != originalPage) {
                                    DropdownMenuItem(
                                        text = { Text(GenesysStrings.DiscardDraft) },
                                        onClick = {
                                            showMenu = false
                                            onDiscardClicked()
                                        },
                                        leadingIcon = { Icon(GenesysIcons.Delete, null, tint = MaterialTheme.colorScheme.error) }
                                    )
                                }
                            }
                        }
                    }

                    GenesysLoadingButton(
                        text = if (isCompact) "" else GenesysStrings.Publish,
                        icon = if (isCompact) GenesysIcons.Check else null,
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
        if (state.isLoading) {
            GenesysLoadingOverlay()
        } else {
            GenesysRow(modifier = Modifier.fillMaxSize(), usePadding = false) {
                GenesysWeightBox(if (isExpanded) 0.65f else 1f) {
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
                                // CORREÇÃO: Usar o index como chave para evitar crash com componentes idênticos (ex: divisores)
                                key = { index, _ -> index },
                            ) { index, component ->
                                val isEditing = state.editingComponentIndex == index
                                ComponentWrapperUI(
                                    component = component,
                                    index = index,
                                    isEditing = isEditing,
                                    allCategories = displayCategories,
                                    allProducts = allProducts,
                                    allServices = allServices,
                                    onEvent = onEvent,
                                )
                            }
                        }
                    }
                }

                if (isExpanded) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .widthIn(min = 350.dp, max = 500.dp)
                            .padding(vertical = 16.dp, horizontal = 8.dp)
                    ) {
                        GenesysCard(
                            modifier = Modifier.fillMaxSize(),
                            elevation = GenesysDimens.ElevationHigh,
                            backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            state.editingComponentIndex?.let { index ->
                                ComponentEditorUI(
                                    state = state,
                                    viewModel = viewModel,
                                    index = index,
                                    onEvent = onEvent,
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

        if (!isExpanded) {
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
    allProducts: List<Product>,
    allServices: List<BookingService>,
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
                .padding(2.dp)
                .clickable { onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(index)) },
    ) {
        PageComponentRenderer(
            component = component,
            isEditMode = true,
            onEditClick = { onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(index)) },
            allAvailableCategories = allCategories,
            allProducts = allProducts,
            allServices = allServices,
            onProductClick = { product ->
                onEvent(WhiteLabelEvent.OnEditProductClicked(product, index))
            },
            onServiceClick = { service ->
                onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(index))
            },
        )

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
                        icon = GenesysIcons.KeyboardDoubleArrowUp,
                        onClick = { onEvent(WhiteLabelEvent.OnMoveComponentToTop(index)) },
                        tint = if (isEditing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    GenesysIconButton(
                        icon = GenesysIcons.ArrowUp,
                        onClick = { onEvent(WhiteLabelEvent.OnMoveComponentUp(index)) },
                        tint = if (isEditing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    GenesysIconButton(
                        icon = GenesysIcons.ArrowDown,
                        onClick = { onEvent(WhiteLabelEvent.OnMoveComponentDown(index)) },
                        tint = if (isEditing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    GenesysIconButton(
                        icon = GenesysIcons.KeyboardDoubleArrowDown,
                        onClick = { onEvent(WhiteLabelEvent.OnMoveComponentToBottom(index)) },
                        tint = if (isEditing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    GenesysIconButton(
                        icon = GenesysIcons.Copy,
                        onClick = { onEvent(WhiteLabelEvent.OnDuplicateComponent(index)) },
                        tint = if (isEditing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    GenesysIconButton(
                        icon = GenesysIcons.Delete,
                        onClick = { onEvent(WhiteLabelEvent.OnDeleteComponent(index)) },
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
internal fun ComponentEditorUI(
    state: WhiteLabelState,
    viewModel: PageViewModel,
    index: Int,
    onEvent: (WhiteLabelEvent) -> Unit,
    onPickImage: () -> Unit,
) {
    val component = state.page.components[index]
    val allProducts by viewModel.allAvailableProducts.collectAsState()
    val allServices by viewModel.services.collectAsState()
    val userPages by viewModel.pages.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // 1. CABEÇALHO DO EDITOR (Fixo)
        GenesysRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            usePadding = false
        ) {
            val icon = when (component) {
                is PageComponent.Header -> GenesysIcons.Description
                is PageComponent.Text -> GenesysIcons.Edit
                is PageComponent.Image -> GenesysIcons.Image
                is PageComponent.Button -> GenesysIcons.Language
                is PageComponent.Grid -> GenesysIcons.GridView
                is PageComponent.ProductList -> GenesysIcons.Inventory
                is PageComponent.ServiceList -> GenesysIcons.Schedule
                else -> GenesysIcons.Magic
            }
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            GenesysSpacer(GenesysSpacing.Medium)
            GenesysText(
                text = "Editar: ${component::class.simpleName ?: "Bloco"}",
                style = GenesysTextStyle.Label,
                fontWeight = GenesysFontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            GenesysIconButton(
                icon = GenesysIcons.Close,
                onClick = { onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null)) }
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        // 2. CONTEÚDO DO EDITOR (Scrollable)
        GenesysColumn(
            usePadding = true,
            useScroll = true,
            modifier = Modifier.weight(1f)
        ) {
            when (component) {
                is PageComponent.Header -> {
                    HeaderComponentEditor(
                        component = component,
                        onSave = { updated ->
                            val newList = state.page.components.toMutableList().apply { set(index, updated) }
                            onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                        },
                    )
                }
                is PageComponent.Text -> {
                    TextComponentEditor(
                        component = component,
                        onSave = { updated ->
                            val newList = state.page.components.toMutableList().apply { set(index, updated) }
                            onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                        },
                    )
                }
                is PageComponent.Image -> {
                    ImageComponentEditor(
                        component = component,
                        userPages = userPages,
                        isUploading = state.isUploading,
                        onPickImage = onPickImage,
                        onSave = { updated ->
                            val newList = state.page.components.toMutableList().apply { set(index, updated) }
                            onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                        },
                    )
                }
                is PageComponent.Button -> {
                    ButtonComponentEditor(
                        component = component,
                        onSave = { updated ->
                            val newList = state.page.components.toMutableList().apply { set(index, updated) }
                            onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                        },
                    )
                }
                is PageComponent.ProductList -> {
                    ProductListComponentEditor(
                        component = component,
                        allAvailableProducts = allProducts,
                        onEditProduct = { product ->
                            onEvent(WhiteLabelEvent.OnEditProductClicked(product, index))
                        },
                        onProductsUpdated = { updatedList ->
                            val updatedComp = component.copy(products = updatedList)
                            val newList = state.page.components.toMutableList().apply { set(index, updatedComp) }
                            onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                        },
                        onSaveLabel = { label, isHorizontal ->
                            val updatedComp = component.copy(customLabel = label, isHorizontal = isHorizontal)
                            val newList = state.page.components.toMutableList().apply { set(index, updatedComp) }
                            onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                        },
                    )
                }
                is PageComponent.ServiceList -> {
                    ServiceListComponentEditor(
                        component = component,
                        allAvailableServices = allServices,
                        onEditService = { service ->
                            onEvent(WhiteLabelEvent.OnEditServiceClicked(service, index))
                        },
                        onServicesUpdated = { updatedList ->
                            val updatedComp = component.copy(services = updatedList)
                            val newList = state.page.components.toMutableList().apply { set(index, updatedComp) }
                            onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                        },
                        onSaveTitle = { title ->
                            val updatedComp = component.copy(title = title)
                            val newList = state.page.components.toMutableList().apply { set(index, updatedComp) }
                            onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                        }
                    )
                }
                is PageComponent.Grid -> {
                    GridComponentEditor(
                        component = component,
                        allPageComponents = state.page.components,
                        allProducts = allProducts,
                        allServices = allServices,
                        onSave = { updated ->
                            val newList = state.page.components.toMutableList().apply { set(index, updated) }
                            onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                        }
                    )
                }
                else -> {
                    GenesysText("Este componente ainda não possui editor avançado.", style = GenesysTextStyle.Body)
                }
            }
        }
    }
}
