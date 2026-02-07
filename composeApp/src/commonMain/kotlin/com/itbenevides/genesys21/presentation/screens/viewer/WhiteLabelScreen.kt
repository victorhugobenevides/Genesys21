package com.itbenevides.genesys21.presentation.screens.viewer

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.domain.model.*
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
import com.itbenevides.genesys21.util.rememberImagePicker
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

    val pristinePage = remember(page.id) { page }
    var showCategoryManagement by remember { mutableStateOf(false) }

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

    LaunchedEffect(Unit) {
        viewModel.loadPages()
        viewModel.loadCategories()
        viewModel.getDraft(page.id)?.let { state = state.copy(page = it) }
    }

    LaunchedEffect(isLoading, serverProducts, savedCategories, userPages) {
        state = state.copy(isLoading = isLoading, availableProducts = serverProducts, allAvailableCategories = savedCategories, userPages = userPages)
    }

    LaunchedEffect(state.page) {
        viewModel.saveDraft(state.page)
    }

    val imagePicker = rememberImagePicker { bytes ->
        bytes?.let {
            state = state.copy(isUploading = true)
            viewModel.uploadImage(it, "media_${Random.nextInt(10000)}.jpg") { uploadedUrl ->
                state.editingComponentIndex?.let { index ->
                    val component = state.page.components[index]
                    val updated = when(component) {
                        is PageComponent.Media -> component.copy(url = uploadedUrl)
                        is PageComponent.ProfileHeader -> component.copy(imageUrl = uploadedUrl)
                        else -> component
                    }
                    val newList = state.page.components.toMutableList().apply { set(index, updated) }
                    state = state.copy(page = state.page.copy(components = newList), isUploading = false)
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
            is WhiteLabelEvent.OnEditingComponentIndexChanged -> state = state.copy(editingComponentIndex = event.index)
            is WhiteLabelEvent.OnPendingNewComponentChanged -> state = state.copy(pendingNewComponent = event.component)
            is WhiteLabelEvent.OnFilterQueryChanged -> state = state.copy(filterQuery = event.query)
            is WhiteLabelEvent.OnImageUploadStarted -> state = state.copy(isUploading = event.isUploading)
            is WhiteLabelEvent.OnDeleteComponent -> {
                val newList = state.page.components.toMutableList().apply { removeAt(event.index) }
                onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                if (state.editingComponentIndex == event.index) state = state.copy(editingComponentIndex = null)
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
                    if (state.editingComponentIndex == event.index) state = state.copy(editingComponentIndex = event.index - 1)
                    else if (state.editingComponentIndex == event.index - 1) state = state.copy(editingComponentIndex = event.index)
                }
            }
            is WhiteLabelEvent.OnMoveComponentDown -> {
                if (event.index < state.page.components.size - 1) {
                    val newList = state.page.components.toMutableList()
                    val temp = newList[event.index]
                    newList[event.index] = newList[event.index + 1]
                    newList[event.index + 1] = temp
                    onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                    if (state.editingComponentIndex == event.index) state = state.copy(editingComponentIndex = event.index + 1)
                    else if (state.editingComponentIndex == event.index + 1) state = state.copy(editingComponentIndex = event.index)
                }
            }
        }
    }

    AppTheme(themeConfig = state.page.theme) {
        WhiteLabelContent(state = state, viewModel = viewModel, onEvent = ::onEvent, originalPage = pristinePage, onManageCategories = { showCategoryManagement = true }, onPickImage = { imagePicker() })
        if (showCategoryManagement) { CategoryManagementDialog(viewModel = viewModel, onDismiss = { showCategoryManagement = false }) }
    }
}

@Composable
private fun WhiteLabelContent(
    state: WhiteLabelState,
    viewModel: PageViewModel,
    onEvent: (WhiteLabelEvent) -> Unit,
    originalPage: Page,
    onManageCategories: () -> Unit,
    onPickImage: () -> Unit
) {
    GenesysPage(
        topBar = {
            GenesysTopAppBar(
                title = state.page.title,
                onBack = { onEvent(WhiteLabelEvent.OnBackClicked) },
                actions = {
                     GenesysIconButton(icon = GenesysIcons.Palette, onClick = { onEvent(WhiteLabelEvent.OnShowThemeSelectorChanged(true)) })
                    if (state.page != originalPage) {
                        GenesysIconButton(icon = GenesysIcons.Delete, tint = MaterialTheme.colorScheme.error, onClick = {
                            viewModel.clearDraft(state.page.id)
                            onEvent(WhiteLabelEvent.OnPageUpdated(originalPage))
                        })
                    }
                    GenesysLoadingButton(text = GenesysStrings.Publish, onClick = { onEvent(WhiteLabelEvent.OnPublishClicked) }, isLoading = state.isLoading)
                }
            )
        },
        floatingActionButton = {
            if (!state.isLoading) {
                GenesysFab(icon = GenesysIcons.Add, onClick = { onEvent(WhiteLabelEvent.OnShowCatalogChanged(true)) })
            }
        }
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWideScreen = maxWidth > 1024.dp

            if (state.isLoading) {
                GenesysLoadingOverlay()
            } else {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(if (isWideScreen) 0.65f else 1f)) {
                        LazyColumn(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, contentPadding = PaddingValues(16.dp)) {
                            if (state.page.components.isEmpty()) {
                                item {
                                    GenesysEmptyState(icon = GenesysIcons.Magic, title = GenesysStrings.EmptyEditorTitle, description = GenesysStrings.EmptyEditorDescription, action = {
                                        GenesysLoadingButton(text = GenesysStrings.AddBlockAction, onClick = { onEvent(WhiteLabelEvent.OnShowCatalogChanged(true)) })
                                    })
                                }
                            } else {
                                items(state.page.components.size) { index ->
                                    val component = state.page.components[index]
                                    val isEditing = state.editingComponentIndex == index
                                    ComponentWrapperUI(component, index, isEditing, onEvent)
                                    Spacer(Modifier.height(16.dp))
                                }
                            }
                            item { Spacer(Modifier.height(100.dp)) }
                        }
                    }

                    if (isWideScreen) {
                        Box(modifier = Modifier.width(400.dp).fillMaxHeight().background(MaterialTheme.colorScheme.surface).border(1.dp, MaterialTheme.colorScheme.outlineVariant).padding(16.dp)) {
                            state.editingComponentIndex?.let { index ->
                                ComponentEditorUI(state = state, index = index, onEvent = onEvent, isEmbedded = true, onManageCategories = onManageCategories, onPickImage = onPickImage)
                            } ?: run {
                                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(GenesysIcons.Edit, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                                    Spacer(Modifier.height(16.dp))
                                    Text(text = GenesysStrings.SelectBlockToEdit, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.outline)
                                }
                            }
                        }
                    }
                }
            }
            if (!isWideScreen && state.editingComponentIndex != null) {
                ComponentEditorUI(state = state, index = state.editingComponentIndex!!, onEvent = onEvent, isEmbedded = false, onManageCategories = onManageCategories, onPickImage = onPickImage)
            }
        }
    }

    if (state.showPageSettings) PageSettingsUI(state, onEvent)
    if (state.showThemeSelector) ThemeSelectorUI(state, onEvent)
    if (state.showCatalog) ComponentCatalogUI(state, onEvent)
}

@Composable
private fun ComponentWrapperUI(component: PageComponent, index: Int, isEditing: Boolean, onEvent: (WhiteLabelEvent) -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().widthIn(max = 800.dp).clip(MaterialTheme.shapes.medium).then(if (isEditing) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium) else Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)).clickable { onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(index)) }.padding(4.dp)) {
        PageComponentRenderer(component = component, isEditMode = true, onEditClick = { onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(index)) })
        Surface(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp), shape = CircleShape, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), tonalElevation = 4.dp, shadowElevation = 2.dp) {
            Row(modifier = Modifier.padding(2.dp)) {
                IconButton(onClick = { onEvent(WhiteLabelEvent.OnMoveComponentUp(index)) }, modifier = Modifier.size(32.dp)) { Icon(GenesysIcons.ArrowUp, null) }
                IconButton(onClick = { onEvent(WhiteLabelEvent.OnMoveComponentDown(index)) }, modifier = Modifier.size(32.dp)) { Icon(GenesysIcons.ArrowDown, null) }
                IconButton(onClick = { onEvent(WhiteLabelEvent.OnDeleteComponent(index)) }, modifier = Modifier.size(32.dp)) { Icon(GenesysIcons.Delete, null, tint = MaterialTheme.colorScheme.error) }
            }
        }
    }
}

@Composable
private fun ComponentEditorUI(state: WhiteLabelState, index: Int, onEvent: (WhiteLabelEvent) -> Unit, isEmbedded: Boolean = false, onManageCategories: () -> Unit, onPickImage: () -> Unit) {
    val component = state.page.components.getOrNull(index) ?: return
    
    @Composable
    fun EditorContent() {
        Column {
            if (isEmbedded) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = GenesysStrings.BlockSettings, style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, modifier = Modifier.weight(1f))
                    IconButton(onClick = { onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null)) }) { Icon(Icons.Default.Close, null) }
                }
                Spacer(Modifier.height(16.dp))
            }

            var customLabel by remember(component) { mutableStateOf(component.customLabel ?: "") }
            GenesysTextField(value = customLabel, onValueChange = { customLabel = it }, label = GenesysStrings.BlockNameLabel, icon = GenesysIcons.Edit)
            Spacer(Modifier.height(24.dp)); GenesysDivider(); Spacer(Modifier.height(24.dp))

            when (component) {
                is PageComponent.Typography -> TypographyComponentEditor(component, onSave = { updated ->
                    val newList = state.page.components.toMutableList().apply { set(index, updated.copy(customLabel = customLabel.ifBlank { null })) }
                    onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                    if (!isEmbedded) onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null))
                })
                is PageComponent.Media -> MediaComponentEditor(component, isUploading = state.isUploading, onPickImage = onPickImage, onSave = { updated ->
                    val newList = state.page.components.toMutableList().apply { set(index, updated.copy(customLabel = customLabel.ifBlank { null })) }
                    onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                    if (!isEmbedded) onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null))
                })
                is PageComponent.Highlight -> HighlightComponentEditor(component, onSave = { updated ->
                    val newList = state.page.components.toMutableList().apply { set(index, updated.copy(customLabel = customLabel.ifBlank { null })) }
                    onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                    if (!isEmbedded) onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null))
                })
                is PageComponent.Testimonial -> TestimonialComponentEditor(component, onSave = { updated ->
                    val newList = state.page.components.toMutableList().apply { set(index, updated.copy(customLabel = customLabel.ifBlank { null })) }
                    onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                    if (!isEmbedded) onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null))
                })
                is PageComponent.StepProcess -> StepProcessComponentEditor(component, onSave = { updated ->
                    val newList = state.page.components.toMutableList().apply { set(index, updated.copy(customLabel = customLabel.ifBlank { null })) }
                    onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                    if (!isEmbedded) onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null))
                })
                is PageComponent.ProductList -> ProductListComponentEditor(component, allAvailableProducts = state.availableProducts, onEditProduct = { onEvent(WhiteLabelEvent.OnEditProductClicked(it, index)) }, onProductsUpdated = { newProducts ->
                    val newList = state.page.components.toMutableList().apply { set(index, component.copy(products = newProducts)) }
                    onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                }, onSaveLabel = { newLabel, isHorizontal ->
                    val newList = state.page.components.toMutableList().apply { set(index, component.copy(customLabel = newLabel.ifBlank { null }, isHorizontal = isHorizontal)) }
                    onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                    if (!isEmbedded) onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null))
                })
                is PageComponent.SocialLinks -> SocialLinksComponentEditor(component, onSave = { updated ->
                    val newList = state.page.components.toMutableList().apply { set(index, updated.copy(customLabel = customLabel.ifBlank { null })) }
                    onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                    if (!isEmbedded) onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null))
                })
                is PageComponent.ProfileHeader -> ProfileHeaderComponentEditor(component, isUploading = state.isUploading, onPickImage = onPickImage, onSave = { updated ->
                    val newList = state.page.components.toMutableList().apply { set(index, updated.copy(customLabel = customLabel.ifBlank { null })) }
                    onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                    if (!isEmbedded) onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null))
                })
                else -> { }
            }
            Spacer(Modifier.height(100.dp))
        }
    }

    if (isEmbedded) {
        val scrollState = rememberScrollState()
        Column(modifier = Modifier.verticalScroll(scrollState)) { EditorContent() }
    } else {
        GenesysBottomSheet(onDismiss = { onEvent(WhiteLabelEvent.OnEditingComponentIndexChanged(null)) }, title = GenesysStrings.BlockSettings) { EditorContent() }
    }
}

@Composable
private fun PageSettingsUI(state: WhiteLabelState, onEvent: (WhiteLabelEvent) -> Unit) {
    var title by remember(state.page.title) { mutableStateOf(state.page.title) }
    GenesysBottomSheet(onDismiss = { onEvent(WhiteLabelEvent.OnShowPageSettingsChanged(false)) }, title = GenesysStrings.PageTitleLabel) {
        GenesysColumn(usePadding = false) {
            GenesysTextField(value = title, onValueChange = { title = it }, label = GenesysStrings.PageTitleLabel, icon = GenesysIcons.Edit)
            GenesysSpacer(GenesysSpacing.Large)
            GenesysLoadingButton(text = GenesysStrings.Save, fillWidth = true, onClick = { 
                onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(title = title)))
                onEvent(WhiteLabelEvent.OnShowPageSettingsChanged(false))
            })
        }
    }
}

@Composable
private fun ThemeSelectorUI(state: WhiteLabelState, onEvent: (WhiteLabelEvent) -> Unit) {
    val themes = listOf(
        Triple(PageThemeConfig.ROYAL, "Royal Night", Color(0xFF14213D)),
        Triple(PageThemeConfig.OCEAN, "Ocean Blue", Color(0xFF00ADB5)),
        Triple(PageThemeConfig.FOREST, "Deep Forest", Color(0xFF283618)),
        Triple(PageThemeConfig.CANDY, "Sweet Candy", Color(0xFFFF758F)),
        Triple(PageThemeConfig.SUNSET, "Sunset Glow", Color(0xFFE76F51)),
        Triple(PageThemeConfig.BERRY, "Berry Wine", Color(0xFF6A0572)),
        Triple(PageThemeConfig.MINIMAL, "Minimal Black", Color(0xFF000000)),
        Triple(PageThemeConfig.VINTAGE, "Vintage Sepia", Color(0xFF8B5E3C)),
        Triple(PageThemeConfig.NORDIC, "Nordic Ice", Color(0xFF4A90E2)),
        Triple(PageThemeConfig.COFFEE, "Mocha Coffee", Color(0xFF6F4E37)),
        Triple(PageThemeConfig.SOFT_LAVENDER, "Soft Lavender", Color(0xFF967BB6)),
        Triple(PageThemeConfig.SKY_BLUE, "Sky Blue", Color(0xFF039BE5)),
        Triple(PageThemeConfig.MINT_GREEN, "Mint Green", Color(0xFF00C853)),
        Triple(PageThemeConfig.PEACH, "Peach Coral", Color(0xFFFF8A65)),
        Triple(PageThemeConfig.LEMON, "Lemon Zest", Color(0xFFFBC02D)),
        Triple(PageThemeConfig.RADARANI, "Radarani Blue", Color(0xFF2CB1FF)),
        Triple(PageThemeConfig.DARK_MODE, "Dark Night", Color(0xFFBB86FC)),
        Triple(PageThemeConfig.MIDNIGHT, "Midnight Red", Color(0xFFE94560)),
        Triple(PageThemeConfig.NEON, "Neon Cyber", Color(0xFF39FF14)),
        Triple(PageThemeConfig.DEEP_SPACE, "Deep Space", Color(0xFF00D1FF)),
        Triple(PageThemeConfig.LUXURY_GOLD, "Luxury Gold", Color(0xFFD4AF37)),
        Triple(PageThemeConfig.MARKETING_RED, "Marketing Red", Color(0xFFBC1B1B))
    )
    GenesysBottomSheet(onDismiss = { onEvent(WhiteLabelEvent.OnShowThemeSelectorChanged(false)) }, title = GenesysStrings.CustomizeStyle) {
        GenesysColumn(usePadding = false, useScroll = true) {
            GenesysText(GenesysStrings.ThemeDescription, style = GenesysTextStyle.Label)
            GenesysSpacer(GenesysSpacing.Medium)
            themes.forEach { (config, label, color) ->
                val isSelected = state.page.theme == config
                GenesysCard(modifier = Modifier.padding(bottom = 8.dp), backgroundColor = if (isSelected) color.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface, onClick = { 
                    onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(theme = config)))
                    onEvent(WhiteLabelEvent.OnShowThemeSelectorChanged(false))
                }) {
                    GenesysRow {
                        GenesysColorCircle(color = color)
                        GenesysSpacer(GenesysSpacing.Medium)
                        GenesysWeightBox(1f) { GenesysText(text = label, style = GenesysTextStyle.Body, fontWeight = if (isSelected) GenesysFontWeight.Bold else GenesysFontWeight.Normal) }
                        if (isSelected) GenesysIconButton(icon = GenesysIcons.Check, onClick = {})
                    }
                }
            }
        }
    }
}

@Composable
private fun ComponentCatalogUI(state: WhiteLabelState, onEvent: (WhiteLabelEvent) -> Unit) {
     GenesysBottomSheet(onDismiss = { onEvent(WhiteLabelEvent.OnShowCatalogChanged(false)) }, title = GenesysStrings.AddBlockTitle) {
        GenesysColumn(usePadding = false, useScroll = true) {
            GenesysText(GenesysStrings.AddBlockDescription, style = GenesysTextStyle.Label)
            GenesysSpacer(GenesysSpacing.Medium)
            
            val catalogItems = listOf(
                Triple("Texto", "Parágrafo, título ou destaque 3D.", PageComponent.Typography("Seu texto aqui...")),
                Triple("Mídia", "Imagem cheia, lado a lado ou avatar.", PageComponent.Media("https://picsum.photos/800/400")),
                Triple("Destaque", "Botão, faixa rotativa ou pílula.", PageComponent.Highlight("CLIQUE AQUI")),
                Triple("Processo", "Etapas numeradas do seu serviço.", PageComponent.StepProcess(listOf(StepItem("PASSO 1", "Descrição do passo...")))),
                Triple("Depoimento", "Frase de impacto de um cliente.", PageComponent.Testimonial("Adorei o serviço!", "Cliente Satisfeito")),
                Triple(GenesysStrings.ComponentTypeProductList, "Itens do seu catálogo.", PageComponent.ProductList(emptyList())),
                Triple("Filtro", "Filtro de categorias automático.", PageComponent.CategoryFilter()),
                Triple("Perfil", "Sua foto e mini-bio.", PageComponent.ProfileHeader("", "Seu Nome", "Sua Bio")),
                Triple("Links Sociais", "Instagram, WhatsApp e E-mail.", PageComponent.SocialLinks())
            )
            
            catalogItems.forEach { (title, desc, component) ->
                GenesysCard(modifier = Modifier.padding(bottom = 8.dp), onClick = {
                    val newList = state.page.components.toMutableList().apply { add(component) }
                    onEvent(WhiteLabelEvent.OnPageUpdated(state.page.copy(components = newList)))
                    onEvent(WhiteLabelEvent.OnShowCatalogChanged(false))
                }) {
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
