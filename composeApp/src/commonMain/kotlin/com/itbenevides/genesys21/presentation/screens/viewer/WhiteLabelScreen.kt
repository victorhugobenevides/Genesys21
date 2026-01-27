package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.domain.model.PageComponent
import com.itbenevides.genesys21.domain.model.PageThemeConfig
import com.itbenevides.genesys21.navigation.Route
import com.itbenevides.genesys21.navigation.Router
import com.itbenevides.genesys21.presentation.PageViewModel
import com.itbenevides.genesys21.ui.theme.AppTheme
import com.itbenevides.genesys21.ui.components.appbar.GenesysTopAppBar
import com.itbenevides.genesys21.ui.components.badge.GenesysBadge
import com.itbenevides.genesys21.ui.components.button.GenesysLoadingButton
import com.itbenevides.genesys21.ui.components.button.GenesysIconButton
import com.itbenevides.genesys21.ui.components.button.GenesysFab
import com.itbenevides.genesys21.ui.components.button.GenesysTextButton
import com.itbenevides.genesys21.ui.components.card.GenesysCard
import com.itbenevides.genesys21.ui.components.feedback.GenesysLoadingOverlay
import com.itbenevides.genesys21.ui.components.feedback.GenesysEmptyState
import com.itbenevides.genesys21.ui.components.feedback.GenesysBottomSheet
import com.itbenevides.genesys21.ui.components.image.GenesysColorCircle
import com.itbenevides.genesys21.ui.components.input.GenesysTextField
import com.itbenevides.genesys21.ui.components.input.GenesysDropdownField
import com.itbenevides.genesys21.ui.components.layout.*
import com.itbenevides.genesys21.ui.components.text.GenesysText
import com.itbenevides.genesys21.ui.components.text.GenesysTextStyle
import com.itbenevides.genesys21.ui.components.text.GenesysFontWeight
import com.itbenevides.genesys21.ui.components.theme.GenesysIcons
import com.itbenevides.genesys21.ui.theme.GenesysStrings
import com.itbenevides.genesys21.ui.theme.GenesysDimens
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
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                GenesysColumn(
                    maxWidth = GenesysDimens.ViewerMaxWidth,
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
                            ComponentWrapperUI(component, index, isEditing, onEvent)
                        }
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
    isEditing: Boolean,
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
            modifier = Modifier.animateContentSize()
        ) {
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
        title = "Configurações do Bloco"
    ) {
        GenesysColumn(usePadding = false) {
            GenesysText(
                text = "Personalize como este conteúdo aparece na sua vitrine.", 
                style = GenesysTextStyle.Label
            )
            
            GenesysSpacer(GenesysSpacing.Medium)

            var customLabel by remember { mutableStateOf(component.customLabel ?: "") }
            
            GenesysTextField(
                value = customLabel,
                onValueChange = { customLabel = it },
                label = "Nome do Bloco (Opcional)",
                placeholder = "Ex: Promoções da Semana",
                icon = GenesysIcons.Edit
            )
            
            GenesysSpacer(GenesysSpacing.Large)
            GenesysDivider()
            GenesysSpacer(GenesysSpacing.Large)

            when (component) {
                is PageComponent.Header -> {
                    var title by remember { mutableStateOf(component.title) }
                    GenesysTextField(value = title, onValueChange = { title = it }, label = "Texto do Título", icon = GenesysIcons.Description)
                    
                    GenesysSpacer(GenesysSpacing.Large)
                    GenesysLoadingButton(
                        text = "Atualizar Título", 
                        fillWidth = true,
                        onClick = {
                            val updated = component.copy(title = title, customLabel = customLabel.ifBlank { null })
                            val newList = state.page.components.toMutableList().apply { set(index, updated) }
                            onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                            onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null))
                        }
                    )
                }
                
                is PageComponent.Text -> {
                    var content by remember { mutableStateOf(component.content) }
                    GenesysTextField(
                        value = content, 
                        onValueChange = { content = it }, 
                        label = "Texto do Bloco", 
                        singleLine = false, 
                        minLines = 5,
                        icon = GenesysIcons.Description
                    )
                    
                    GenesysSpacer(GenesysSpacing.Large)
                    GenesysLoadingButton(
                        text = "Salvar Texto", 
                        fillWidth = true,
                        onClick = {
                            val updated = component.copy(content = content, customLabel = customLabel.ifBlank { null })
                            val newList = state.page.components.toMutableList().apply { set(index, updated) }
                            onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                            onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null))
                        }
                    )
                }

                is PageComponent.Image -> {
                    // MESCLAGEM: Lógica para tratar links internos e externos em um único campo
                    val pageOptions = remember(state.userPages) { state.userPages.map { it.title } }
                    
                    // Estado inicial baseado no que já está salvo no componente
                    var currentLinkValue by remember(component.string, component.destinationPageId, state.userPages) {
                        val internalTitle = state.userPages.find { it.id == component.destinationPageId }?.title
                        mutableStateOf(internalTitle ?: component.string)
                    }

                    GenesysLoadingButton(
                        text = if (isUploading) "Enviando..." else "Trocar Imagem",
                        icon = GenesysIcons.CloudUpload,
                        onClick = { imagePicker() },
                        isLoading = isUploading,
                        fillWidth = true
                    )

                    GenesysSpacer(GenesysSpacing.Medium)

                    // UX: Campo unificado que aceita seleção de página ou digitação de URL
                    GenesysDropdownField(
                        value = currentLinkValue,
                        onValueChange = { currentLinkValue = it },
                        label = "Destino do Clique (Link ou Página)",
                        placeholder = "Selecione uma vitrine ou digite um link (https://...)",
                        options = pageOptions,
                        icon = GenesysIcons.Language
                    )

                    GenesysSpacer(GenesysSpacing.Large)
                    GenesysLoadingButton(text = "Confirmar Alterações", fillWidth = true, onClick = {
                        // Lógica para decidir se é página interna ou link externo
                        val matchingPage = state.userPages.find { it.title == currentLinkValue }
                        
                        val updated = component.copy(
                            string = if (matchingPage == null) currentLinkValue else "",
                            destinationPageId = matchingPage?.id,
                            customLabel = customLabel.ifBlank { null }
                        )
                        val newList = state.page.components.toMutableList().apply { set(index, updated) }
                        onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                        onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null))
                    })
                }
                
                is PageComponent.ProductList -> {
                    GenesysText("Gerenciamento de Produtos", style = GenesysTextStyle.Body, fontWeight = GenesysFontWeight.Bold)
                    GenesysSpacer(GenesysSpacing.Small)
                    
                    GenesysLoadingButton(
                        text = "Cadastrar Novo Produto",
                        icon = GenesysIcons.Add,
                        onClick = { 
                            onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null))
                            onEvent(WhiteLabelEvent.OnEditProductClicked(null, index)) 
                        },
                        fillWidth = true
                    )
                    
                    GenesysSpacer(GenesysSpacing.Medium)
                    
                    GenesysColumn(usePadding = false, modifier = Modifier.heightIn(max = 300.dp), useScroll = true) {
                        component.products.forEach { product ->
                            GenesysCard(
                                modifier = Modifier.padding(bottom = 4.dp),
                                onClick = { 
                                    onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null))
                                    onEvent(WhiteLabelEvent.OnEditProductClicked(product, index)) 
                                }
                            ) {
                                GenesysRow {
                                    GenesysWeightBox(1f) {
                                        GenesysText(product.name)
                                    }
                                    GenesysIconButton(icon = GenesysIcons.Edit, onClick = {})
                                }
                            }
                        }
                    }
                    
                    GenesysSpacer(GenesysSpacing.Large)
                    GenesysLoadingButton(text = "Salvar Rótulo da Lista", fillWidth = true, onClick = {
                        val updated = component.copy(customLabel = customLabel.ifBlank { null })
                        val newList = state.page.components.toMutableList().apply { set(index, updated) }
                        onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                        onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null))
                    })
                }
                else -> {
                    GenesysLoadingButton(text = "Salvar Alterações", fillWidth = true, onClick = {
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
        title = "Personalizar Estilo"
    ) {
        GenesysColumn(usePadding = false, useScroll = true) {
            GenesysText("Selecione a paleta de cores que melhor representa sua marca.", style = GenesysTextStyle.Label)
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
                        GenesysText(
                            text = label, 
                            style = GenesysTextStyle.Body,
                            fontWeight = if (isSelected) GenesysFontWeight.Bold else GenesysFontWeight.Normal
                        )
                        GenesysWeightSpacer(1f)
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
        title = "Adicionar Bloco de Conteúdo"
    ) {
        GenesysColumn(usePadding = false, useScroll = true) {
            GenesysText("Escolha o tipo de conteúdo para adicionar à sua vitrine.", style = GenesysTextStyle.Label)
            GenesysSpacer(GenesysSpacing.Medium)
            
            val catalogItems = listOf(
                Triple("Cabeçalho", "Um título grande para destacar seções.", PageComponent.Header("Novo Título")),
                Triple("Texto", "Conte um pouco mais sobre seus produtos.", PageComponent.Text("Seu texto aqui...")),
                Triple("Lista de Produtos", "Exiba seus itens disponíveis.", PageComponent.ProductList(emptyList())),
                Triple("Imagem", "Destaque banners ou fotos.", PageComponent.Image("", "")),
                Triple("Botão", "Crie links externos ou ações.", PageComponent.Button("Toque Aqui", "")),
                Triple("Filtro", "Facilite a busca para seus clientes.", PageComponent.Filter())
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
