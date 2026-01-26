package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.domain.model.PageThemeConfig
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.navigation.Route
import com.itbenevides.genesys21.navigation.Router
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.presentation.screens.list.PageListEvent
import com.itbenevides.genesys21.ui.components.appbar.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.badge.GenesysBadge
import com.itbenevides.genesys21.ui.components.button.GenesysFab
import com.itbenevides.genesys21.ui.components.button.GenesysIconButton
import com.itbenevides.genesys21.ui.components.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.button.GenesysTextButton
import com.itbenevides.genesys21.ui.components.card.GenesysCard
import com.itbenevides.genesys21.ui.components.feedback.GenesysBottomSheet
import com.itbenevides.genesys21.ui.components.feedback.GenesysEmptyState
import com.itbenevides.genesys21.ui.components.feedback.GenesysLoadingOverlay
import com.itbenevides.genesys21.ui.components.image.GenesysColorCircle
import com.itbenevides.genesys21.ui.components.input.GenesysTextField
import com.itbenevides.genesys21.ui.components.layout.*
import com.itbenevides.genesys21.ui.components.text.GenesysText
import com.itbenevides.genesys21.ui.components.text.GenesysTextStyle
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.AppTheme
import com.itbenevides.genesys21.ui.theme.GenesysDimens
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.util.rememberImagePicker
import org.koin.compose.koinInject
import kotlin.random.Random

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

    var state by remember { 
        mutableStateOf(
            WhiteLabelState(
                page = page,
                isLoading = isLoading,
                availableProducts = serverProducts,
                allAvailableCategories = savedCategories,
                userPages = userPages
            )
        )
    }

    state = state.copy(
        isLoading = isLoading,
        availableProducts = serverProducts,
        allAvailableCategories = savedCategories,
        userPages = userPages
    )

    fun onEvent(event: WhiteLabelEvent) {
        when (event) {
            is WhiteLabelEvent.OnPageUpdated -> {
                state = state.copy(page = event.newPage)
                onPageChange(event.newPage)
            }
            is WhiteLabelEvent.OnPublishClicked -> {
                viewModel.savePage(state.page, true) { onBack() }
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
        WhiteLabelContent(state, viewModel, ::onEvent)
    }
}

@Composable
private fun WhiteLabelContent(
    state: WhiteLabelState,
    viewModel: PageViewModel,
    onEvent: (WhiteLabelEvent) -> Unit
) {
    val router: Router = koinInject()

    GenesysPage(
        topBar = {
            GenesysTopAppBar(
                title = state.page.title,
                onBack = { onEvent(WhiteLabelEvent.OnBackClicked) },
                actions = {
                    GenesysIconButton(
                        icon = GenesysIcons.Settings, 
                        contentDescription = GenesysStrings.AdminTitle,
                        onClick = { router.navigateTo(Route.PageList) }
                    )
                    GenesysIconButton(
                        icon = GenesysIcons.Palette, 
                        contentDescription = GenesysStrings.EditorThemes,
                        onClick = { onEvent(WhiteLabelEvent.OnShowThemeSelectorChanged(true)) }
                    )
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
        if (state.isLoading) {
            GenesysLoadingOverlay()
        } else {
            GenesysColumn(usePadding = false) {
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
                        ComponentWrapperUI(component, index, onEvent)
                    }
                }
            }
        }
    }

    if (state.showPageSettings) PageSettingsUI(state, onEvent)
    if (state.showThemeSelector) ThemeSelectorUI(state, onEvent)
    if (state.showCatalog) ComponentCatalogUI(state, onEvent)
    
    state.editingComponentIndex?.let { index ->
        ComponentEditorUI(state, viewModel, index, onEvent)
    }
}

@Composable
private fun ComponentWrapperUI(
    component: PageComponent,
    index: Int,
    onEvent: (WhiteLabelEvent) -> Unit
) {
    GenesysColumn(usePadding = true) {
        GenesysRow {
            GenesysBadge(
                label = (component.customLabel ?: component::class.simpleName ?: "Bloco").uppercase(),
                color = Color.Unspecified 
            )
            
            GenesysSpacer(GenesysSpacing.Small)
            
            GenesysIconButton(
                icon = GenesysIcons.Edit, 
                onClick = { onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(index)) }
            )
            
            GenesysWeightSpacer(1f)
            
            GenesysRow(fillWidth = false) {
                GenesysIconButton(icon = GenesysIcons.ArrowUp, onClick = { onEvent(WhiteLabelEvent.OnMoveComponentUp(index)) })
                GenesysIconButton(icon = GenesysIcons.ArrowDown, onClick = { onEvent(WhiteLabelEvent.OnMoveComponentDown(index)) })
                GenesysIconButton(icon = GenesysIcons.Copy, onClick = { onEvent(WhiteLabelEvent.OnDuplicateComponent(index)) })
                GenesysIconButton(icon = GenesysIcons.Delete, onClick = { onEvent(WhiteLabelEvent.OnDeleteComponent(index)) }, tint = Color.Red)
            }
        }

        GenesysCard(elevation = GenesysDimens.ElevationLow) {
            PageComponentRenderer(
                component = component,
                isEditMode = true,
                onEditClick = { onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(index)) }
            )
        }
    }
}

@Composable
private fun ComponentEditorUI(
    state: WhiteLabelState,
    viewModel: PageViewModel,
    index: Int,
    onEvent: (WhiteLabelEvent) -> Unit
) {
    val component = state.page.components.getOrNull(index) ?: return
    var isUploading by remember { mutableStateOf(false) }

    // CORREÇÃO: Chama a função diretamente sem o .launch()
    val imagePicker = rememberImagePicker { bytes ->
        bytes?.let {
            isUploading = true
            viewModel.uploadImage(it, "component_${Random.nextInt(10000)}.jpg") { uploadedUrl ->
                val updatedComponent = when (component) {
                    is PageComponent.Image -> component.copy(url = uploadedUrl)
                    else -> component
                }
                val newList = state.page.components.toMutableList().apply { set(index, updatedComponent) }
                onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                isUploading = false
            }
        }
    }
    
    GenesysBottomSheet(
        onDismiss = { onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null)) },
        title = "Editar Bloco"
    ) {
        GenesysColumn(usePadding = false) {
            var customLabel by remember { mutableStateOf(component.customLabel ?: "") }
            
            GenesysTextField(
                value = customLabel,
                onValueChange = { customLabel = it },
                label = "Nome do Bloco",
                placeholder = "Nome interno"
            )
            
            GenesysSpacer(GenesysSpacing.Medium)

            when (component) {
                is PageComponent.Header -> {
                    var title by remember { mutableStateOf(component.title) }
                    GenesysTextField(value = title, onValueChange = { title = it }, label = "Título do Cabeçalho")
                    
                    GenesysSpacer(GenesysSpacing.Large)
                    GenesysLoadingButton(text = "Salvar", onClick = {
                        val updated = component.copy(title = title, customLabel = customLabel.ifBlank { null })
                        val newList = state.page.components.toMutableList().apply { set(index, updated) }
                        onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                        onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null))
                    })
                }
                
                is PageComponent.Text -> {
                    var content by remember { mutableStateOf(component.content) }
                    GenesysTextField(value = content, onValueChange = { content = it }, label = "Conteúdo do Texto", singleLine = false)
                    
                    GenesysSpacer(GenesysSpacing.Large)
                    GenesysLoadingButton(text = "Salvar", onClick = {
                        val updated = component.copy(content = content, customLabel = customLabel.ifBlank { null })
                        val newList = state.page.components.toMutableList().apply { set(index, updated) }
                        onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                        onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null))
                    })
                }

                is PageComponent.Image -> {
                    GenesysText("Imagem atual:", style = GenesysTextStyle.Label)
                    GenesysSpacer(GenesysSpacing.Small)
                    
                    GenesysLoadingButton(
                        text = if (isUploading) "Enviando..." else "Trocar Imagem",
                        icon = GenesysIcons.CloudUpload,
                        onClick = { imagePicker() }, // Chamada direta da função () -> Unit
                        isLoading = isUploading
                    )

                    GenesysSpacer(GenesysSpacing.Large)
                    GenesysLoadingButton(text = "Salvar Rótulo", onClick = {
                        val updated = component.copy(customLabel = customLabel.ifBlank { null })
                        val newList = state.page.components.toMutableList().apply { set(index, updated) }
                        onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                        onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null))
                    })
                }
                
                is PageComponent.ProductList -> {
                    GenesysText("Gerencie os produtos deste bloco:", style = GenesysTextStyle.Label)
                    GenesysSpacer(GenesysSpacing.Medium)
                    
                    GenesysLoadingButton(
                        text = "Adicionar Produto",
                        icon = GenesysIcons.Add,
                        onClick = { 
                            onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null))
                            onEvent(WhiteLabelEvent.OnEditProductClicked(null, index)) 
                        }
                    )
                    
                    GenesysSpacer(GenesysSpacing.Small)
                    
                    component.products.forEach { product ->
                        GenesysCard(onClick = { 
                            onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null))
                            onEvent(WhiteLabelEvent.OnEditProductClicked(product, index)) 
                        }) {
                            GenesysRow {
                                GenesysText(product.name, modifier = Modifier.weight(1f))
                                GenesysIconButton(icon = GenesysIcons.Edit, onClick = {
                                    onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null))
                                    onEvent(WhiteLabelEvent.OnEditProductClicked(product, index))
                                })
                            }
                        }
                        GenesysSpacer(GenesysSpacing.Small)
                    }
                    
                    GenesysLoadingButton(text = "Salvar Rótulo", onClick = {
                        val updated = component.copy(customLabel = customLabel.ifBlank { null })
                        val newList = state.page.components.toMutableList().apply { set(index, updated) }
                        onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                        onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null))
                    })
                }
                else -> {
                    GenesysText("Edição básica disponível.")
                    GenesysSpacer(GenesysSpacing.Large)
                    GenesysLoadingButton(text = "Salvar Rótulo", onClick = {
                        val updated = when(component) {
                            is PageComponent.Button -> component.copy(customLabel = customLabel.ifBlank { null })
                            is PageComponent.Filter -> component.copy(customLabel = customLabel.ifBlank { null })
                            is PageComponent.CategoryFilter -> component.copy(customLabel = customLabel.ifBlank { null })
                            else -> component
                        }
                        val newList = state.page.components.toMutableList().apply { set(index, updated) }
                        onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                        onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null))
                    })
                }
            }
        }
    }
}

@Composable
private fun PageSettingsUI(state: WhiteLabelState, onEvent: (WhiteLabelEvent) -> Unit) {
    var title by remember { mutableStateOf(state.page.title) }
    GenesysBottomSheet(
        onDismiss = { onEvent(WhiteLabelEvent.OnShowPageSettingsChanged(false)) },
        title = GenesysStrings.PageTitleLabel,
        actions = {
            GenesysTextButton(
                text = GenesysStrings.Save, 
                onClick = { 
                    onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(title = title)))
                    onEvent(WhiteLabelEvent.OnShowPageSettingsChanged(false))
                }
            )
        }
    ) {
        GenesysTextField(
            value = title,
            onValueChange = { newValue -> title = newValue },
            label = GenesysStrings.PageTitleLabel
        )
    }
}

@Composable
private fun ThemeSelectorUI(state: WhiteLabelState, onEvent: (WhiteLabelEvent) -> Unit) {
    val themes = listOf(
        Triple(PageThemeConfig.ROYAL, "Royal", Color(0xFF14213D)),
        Triple(PageThemeConfig.OCEAN, "Ocean", Color(0xFF00ADB5)),
        Triple(PageThemeConfig.FOREST, "Forest", Color(0xFF283618))
    )
    GenesysBottomSheet(
        onDismiss = { onEvent(WhiteLabelEvent.OnShowThemeSelectorChanged(false)) },
        title = GenesysStrings.EditorThemes
    ) {
        GenesysColumn(usePadding = false, useScroll = true) {
            themes.forEach { (config, label, color) ->
                GenesysCard(
                    onClick = { 
                        onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(theme = config)))
                        onEvent(WhiteLabelEvent.OnShowThemeSelectorChanged(false))
                    }
                ) {
                    GenesysRow {
                        GenesysColorCircle(color = color)
                        GenesysSpacer(GenesysSpacing.Medium)
                        GenesysText(label, style = GenesysTextStyle.Body)
                    }
                }
                GenesysSpacer(GenesysSpacing.Small)
            }
        }
    }
}

@Composable
private fun ComponentCatalogUI(state: WhiteLabelState, onEvent: (WhiteLabelEvent) -> Unit) {
     GenesysBottomSheet(
        onDismiss = { onEvent(WhiteLabelEvent.OnShowCatalogChanged(false)) },
        title = GenesysStrings.AddBlockAction
    ) {
        GenesysColumn(usePadding = false, useScroll = true) {
            val catalogItems = listOf(
                "Título" to PageComponent.Header("Novo Título"),
                "Texto" to PageComponent.Text("Seu texto aqui"),
                "Lista de Produtos" to PageComponent.ProductList(emptyList()),
                "Imagem" to PageComponent.Image("", ""),
                "Botão" to PageComponent.Button("Clique Aqui", ""),
                "Barra de Pesquisa" to PageComponent.Filter()
            )
            
            catalogItems.forEach { (label, component) ->
                GenesysCard(onClick = {
                    val newList = state.page.components.toMutableList().apply { add(component) }
                    onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                    onEvent(WhiteLabelEvent.OnShowCatalogChanged(false))
                }) {
                    GenesysRow {
                        GenesysText(label, style = GenesysTextStyle.Body)
                        GenesysWeightSpacer(1f)
                        GenesysIconButton(icon = GenesysIcons.Add, onClick = {})
                    }
                }
                GenesysSpacer(GenesysSpacing.Small)
            }
        }
    }
}
